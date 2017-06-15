package org.udesc.trajectory.TODS;

import java.util.*;

import com.sun.xml.internal.bind.api.impl.NameConverter;
import org.udesc.database.TrajectoryMongoDB;

public class TODSv2 {

	private TrajectoryMongoDB trajectoryBase;

	public TODSv2() {
		super();
		trajectoryBase = new TrajectoryMongoDB();
	}


    public TODSResult loadTest(TODSRequest request, int rep) throws Exception {

        //######### Database search ##########
        TODSResult calculationResult = new TODSResult();
        Long time = System.nanoTime();
        List<Trajectory> candidates = (List<Trajectory>) trajectoryBase.findTrajectories(request.getCountry(), request.getState(), request.getCity(), request.getStartHour(), request.getEndHour(), request.getStartGrid(), request.getEndGrid(), Trajectory.class, Point.class);
        List<Trajectory> newTraj = new ArrayList<>();
        for(int i = 0; i < rep; i++) {
            newTraj.addAll(this.reply(candidates));
        }
        candidates.addAll(newTraj);
        calculationResult.setQueryTime(System.nanoTime() - time);
        calculationResult.setRawResult(candidates);
        //######### Filter processing and interpolation #########
        time = System.nanoTime();
        for(Trajectory trajectory : candidates) {
            trajectory.filterNoise(request.getSigma(), request.getSd());
            trajectory.interpolate(request.getInterpolation());
        }
        candidates = processCandidates(candidates, request.getStartGrid(), request.getEndGrid(), request.getStartHour(), request.getEndHour());
        Collections.sort(candidates, new Comparator<Trajectory>() {
            public int compare(Trajectory o1, Trajectory o2) {
                return (int) (o1.getPoints().size() - o2.getPoints().size());
            }
        });
        IndexManager indexManager = new IndexManager(candidates, request.getDistance());
        calculationResult.setCandidateTime(System.nanoTime() - time);
        //######### Group procession ##########
        time = System.nanoTime();
        List<Group> groups = processGroups(indexManager, candidates);
        calculationResult.setGroupTime(System.nanoTime() - time);
        //######### Standard procession ##########
        time = System.nanoTime();
        List<Group> standard = processStandardTrajectories(groups, request.getkStandard());
        calculationResult.setStandards(standard);
        calculationResult.setStandardTime(System.nanoTime() - time);
        //######### Segmentation procession ##########
        time = System.nanoTime();
        calculationResult.setNotStandards(this.processNotStandardTrajectories(groups, standard, request.getDistance(), request.getAngle()));
        calculationResult.setSegmentationTime(System.nanoTime() - time);
        return calculationResult;
    }
    private List<Trajectory> reply(List<Trajectory> trajectories) {
        List<Trajectory> trajs = new ArrayList<>();
        for(Trajectory t : trajectories) {
            Trajectory n = new Trajectory();
            n.setCity(t.getCity());
            n.setCountry(t.getCountry());
            n.setGroup(t.getGroup());
            n.setName(t.getName());
            n.setStartHour(t.getStartHour());
            n.setState(t.getState());
            n.setStartMinute(t.getStartMinute());
            for(Point p : t.getPoints()) {
                Point pn = new Point();
                pn.setStandard(p.isStandard());
                pn.setProcessed(p.isProcessed());
                pn.setTimestamp(p.getTimestamp());
                pn.setLng(p.getLng() + (Math.random() / 1000));
                pn.setLat(p.getLat() + (Math.random() / 1000));
                n.getPoints().add(pn);
                pn.setTrajectory(n);
            }
            trajs.add(n);
        }
        return trajs;
    }


	
	@SuppressWarnings("unchecked")
	public TODSResult run(TODSRequest request) throws Exception {

        //######### Database search ##########
        Long time = System.nanoTime();
        List<Trajectory> candidates = (List<Trajectory>) trajectoryBase.findTrajectories(request.getCountry(), request.getState(), request.getCity(), request.getStartHour(), request.getEndHour(), request.getStartGrid(), request.getEndGrid(), Trajectory.class, Point.class);
        long endTime = System.nanoTime() - time;
        TODSResult result = this.run(request, candidates);
        result.setQueryTime(endTime);
        return result;
    }

    public TODSResult run(TODSRequest request, List<Trajectory> candidates) throws Exception {
        TODSResult calculationResult = new TODSResult();
        //######### Filter processing and interpolation #########
		Long time = System.nanoTime();
		long pts = 0;
		for(Trajectory trajectory : candidates) {
		    pts += trajectory.getPoints().size();
			trajectory.filterNoise(request.getSigma(), request.getSd());
			trajectory.interpolate(request.getInterpolation());
		}
		candidates = processCandidates(candidates, request.getStartGrid(), request.getEndGrid(), request.getStartHour(), request.getEndHour());
		Collections.sort(candidates, new Comparator<Trajectory>() {
			public int compare(Trajectory o1, Trajectory o2) {
				return (int) (o1.getPoints().size() - o2.getPoints().size());
			}
		});
		if(candidates.isEmpty()) return calculationResult;
        IndexManager indexManager = new IndexManager(candidates, request.getDistance());
        calculationResult.setPointsTotal(pts);
		calculationResult.setCandidateTime(System.nanoTime() - time);
		//######### Group procession ##########
        time = System.nanoTime();
        List<Group> groups = processGroups(indexManager, candidates);
		calculationResult.setGroupTime(System.nanoTime() - time);
		//######### Standard procession ##########
        time = System.nanoTime();
        if(!groups.isEmpty()) {
            List<Group> standard = processStandardTrajectories(groups, request.getkStandard());
            calculationResult.setStandards(standard);
            calculationResult.setStandardTime(System.nanoTime() - time);
            //######### Segmentation procession ##########
            time = System.nanoTime();
            calculationResult.setNotStandards(this.processNotStandardTrajectories(groups, standard, request.getDistance(), request.getAngle()));
            calculationResult.setSegmentationTime(System.nanoTime() - time);
        }
		return calculationResult;
	}
	
	public List<Trajectory> processCandidates(List<Trajectory> candidates, Grid startGrid, Grid endGrid, int startHour, int endHour) {
		List<Trajectory> subTrajectories = new ArrayList<Trajectory>();
    	for(Trajectory trajectory : candidates) {
    		Trajectory subTrajectory = this.processSubCandidates(trajectory, startGrid, endGrid);
    		if(subTrajectory != null && !subTrajectory.getPoints().isEmpty() && 
    				startHour <= subTrajectory.getPoints().get(0).getHour() &&
    				subTrajectory.getPoints().get(subTrajectory.getPoints().size() - 1).getHour() <= endHour) {
    			subTrajectories.add(subTrajectory);
    		}
    	}
    	return subTrajectories;
	}
	
	public Trajectory processSubCandidates(Trajectory trajectory, Grid gS, Grid gE) {
		int start = -1;
    	int end = -1;
    	double timeDifference = -1;
    	Trajectory subT = new Trajectory();
    	subT.setName(trajectory.getName());
    	
    	for(int i = 0; i < trajectory.getPoints().size(); i++) {
    		Point pS = trajectory.getPoints().get(i);
    		if (gS.betweenLat(pS) && gS.betweenLng(pS)) {
    			for(int j = i; j < trajectory.getPoints().size(); j++) {
    				Point pE = trajectory.getPoints().get(j);
        			if(pE != pS && gE.betweenLat(pE) && gE.betweenLng(pE)) {
        				if(Math.abs(Math.abs(pE.getTimestamp()) - Math.abs(pS.getTimestamp())) <= timeDifference || timeDifference == -1) {
        					start = i;
        					end = j;
        					timeDifference = Math.abs(Math.abs(pE.getTimestamp()) - Math.abs(pS.getTimestamp()));
        				}
        			}
        		}
    		}
    	}
    	if (start < end && start != -1 && end != -1) {
    		subT.getPoints().addAll(trajectory.getPoints().subList(start, end + 1));
    		for(Point p : subT.getPoints()) {
    			p.setTrajectory(subT);
			}
    	} else {
    		return null;
    	}
    	subT.sortPoints();
    	return subT;
	}
	
	public List<Group> processGroups(IndexManager indexManager, List<Trajectory> trajectories) {
        List<Group> groups = new ArrayList<Group>();
		int groupCounter = 0;
		Trajectory temp = null;
        Group currentGroup = null;
		for(Trajectory trajectory : trajectories) {
		    if(trajectory.getPoints().size() > 2) {
                currentGroup = null;
                temp = null;
                int[] trajectoriesSum = indexManager.calculateTrajectories(trajectory.getId());
                for(int i = 0; i < trajectories.size(); i++) {
                    temp = trajectories.get(i);
                    if(!trajectory.equals(temp) &&
                        temp.getGroup() != null &&
                        trajectory.getPoints().size() == trajectoriesSum[i] &&
                        (currentGroup == null || temp.getGroup().trajectoriesSize() > currentGroup.trajectoriesSize())) {
                        currentGroup = temp.getGroup();
                    }
                }
                if(currentGroup == null) {
                    currentGroup = new Group();
                    currentGroup.setName("Group " + groupCounter++);
                    groups.add(currentGroup);
                }
                currentGroup.getTrajectories().add(trajectory);
                trajectory.setGroup(currentGroup);
            }
		}
		return groups;
	}
	
	public List<Group> processStandardTrajectories(List<Group> groups, int k) {
    	Collections.sort(groups, new Comparator<Group>() {
			public int compare(Group o1, Group o2) {
				return (int) (o2.getTrajectories().size() - o1.getTrajectories().size());
			}
		});
        for (Group group : groups) {
            group.createIndex();
        }
        return groups.subList(0, k);
    }
	
	public List<Group> processNotStandardTrajectories(List<Group> GT, List<Group> STG, double distance, double angle) {
    	List<Group> NSG = new ArrayList<>();
    	for(Group g : GT) {
    		if(!STG.contains(g)) {
    			NSG.add(g);
    		}
    	}
    	for(Group ns : NSG) {
    		List<Route> routes = new ArrayList<>();
    		for(Trajectory t : ns.getTrajectories()) {
    			Route route = this.processNotStandardTrajectoriesSegments(t, STG, distance, angle);
    			if(route != null) {
    				routes.add(route);
    			}
    		}
    		ns.setRoutes(routes);
    	}
    	return NSG;
    }
	
	public Route processNotStandardTrajectoriesSegments(Trajectory ns, List<Group> GT, double distance, double angle) {
        Route route = new Route();
        for(Point p : ns.getPoints()) {
            p.setStandard(false);
            for(Group g : GT) {
                for(Trajectory st : g.getTrajectories()) {
                    if(st.binarySearch(p, distance) != null) {
                        p.setStandard(true);
                        break;
                    }
                    if(p.isStandard()) break;
                }
                if(p.isStandard()) break;
            }

        }

        for(int i = 1; i < ns.getPoints().size() - 1; i++) {
            Point p = ns.getPoints().get(i);
            if(!p.isStandard()) {
                int prev = i - 1;
                int next = i + 1;
                Point current = p;
                Point prevP = ns.getPoints().get(prev);
                Point nextP = ns.getPoints().get(next);
                prev--;
                next++;
                while(prevP.isStandard() && prev >= 0) {
                    for(Group g : GT) {
                        for(Trajectory st : g.getTrajectories()) {
                            if(!st.isLessAngleDifference(current, prevP, distance, angle)) {
                                prevP.setStandard(false);
                                break;
                            }
                            if(!prevP.isStandard()) break;
                        }
                        if(!prevP.isStandard()) break;
                    }
                    if(prevP.isStandard()) break;
                    current = prevP;
                    prevP = ns.getPoints().get(prev);
                    prev--;
                }
                current = p;
                while(nextP.isStandard() && next < ns.getPoints().size()) {
                    for(Group g : GT) {
                        for(Trajectory st : g.getTrajectories()) {
                            if(!st.isLessAngleDifference(current, nextP, distance, angle)) {
                                nextP.setStandard(false);
                                break;
                            }
                            if(!nextP.isStandard()) break;
                        }
                        if(!nextP.isStandard()) break;
                    }
                    if(nextP.isStandard()) break;
                    current = nextP;
                    nextP = ns.getPoints().get(next);
                    next++;
                }
            }
        }

        Trajectory t = new Trajectory();
        t.getPoints().add(ns.getPoints().get(0));
        for(int i = 1; i < ns.getPoints().size(); i++) {
            Point curr = ns.getPoints().get(i);
            Point prev = ns.getPoints().get(i - 1);
            if(curr.isStandard() != prev.isStandard()) {
                if(prev.isStandard()) {
                    route.getStandards().add(t);
                } else {
                    route.getNotStandards().add(t);
                }
                t = new Trajectory();
                Point temp = new Point(prev.getLat(), prev.getLng(), t, prev.getTimestamp(), prev.isStandard());
                temp.setStandard(curr.isStandard());
                t.getPoints().add(temp);
            }
            t.getPoints().add(curr);
        }
        if(!t.getPoints().isEmpty()) {
            if(t.getPoints().get(0).isStandard()) {
                route.getStandards().add(t);
            } else {
                route.getNotStandards().add(t);
            }
        }

        return route;
    }
	
}
