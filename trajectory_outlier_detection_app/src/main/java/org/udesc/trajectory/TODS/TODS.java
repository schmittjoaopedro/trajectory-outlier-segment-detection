package org.udesc.trajectory.TODS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.udesc.database.TrajectoryMongoDB;

/**
 * Trajectory outlier detection and segmentation - TODS
 * 
 * @author root
 *
 */
public class TODS {

	private TrajectoryMongoDB trajectoryBase;
	
	public TODS() {
		super();
		trajectoryBase = new TrajectoryMongoDB();
	}
	
	@SuppressWarnings("unchecked")
	public TODSResult run(TODSRequest request) throws Exception {
		Long startTime = System.nanoTime();
		List<Trajectory> candidates = (List<Trajectory>) trajectoryBase.findTrajectories(request.getCountry(), request.getState(), request.getCity(), request.getStartHour(), request.getEndHour(), request.getStartGrid(), request.getEndGrid(), Trajectory.class, Point.class);
		startTime = System.nanoTime() - startTime;
		TODSResult calculationResult = this.run(request, candidates);
		calculationResult.setQueryTime(startTime);
		return calculationResult;
	}
	
	public TODSResult runExternal(TODSRequest request, List<Trajectory> candidates) throws Exception {
		Long startTime = System.nanoTime();
		startTime = System.nanoTime() - startTime;
		TODSResult calculationResult = this.run(request, candidates);
		calculationResult.setQueryTime(startTime);
		return calculationResult;
	}
	
	public TODSResult run(TODSRequest request, List<Trajectory> trajectories) throws Exception {
		TODSResult calculationResult = new TODSResult();
		
		Long time = System.nanoTime();
		for(Trajectory trajectory : trajectories) {
			trajectory.filterNoise(request.getSigma(), request.getSd());
			trajectory.interpolate(request.getInterpolation());
		}
		List<Trajectory> candidates = this.getCandidatesTrajectories(trajectories, request.getStartGrid(), request.getEndGrid(), request.getStartHour(), request.getEndHour(), calculationResult);
		calculationResult.setTrajectoriesAnalysed(candidates.size());
		Collections.sort(candidates, new Comparator<Trajectory>() {
			public int compare(Trajectory o1, Trajectory o2) {
				return (int) (o1.getPoints().size() - o2.getPoints().size());
			}
		});
		calculationResult.setCandidateTime(System.nanoTime() - time);
		time = System.nanoTime();
		
		if(!candidates.isEmpty()) {
			List<Group> groups = this.getGroupTrajectories(candidates, request.getDistance());
			calculationResult.setGroupTime(System.nanoTime() - time);
			time = System.nanoTime();
		
			if(!groups.isEmpty()) {
				List<Group> standard = this.getStandardTrajectories(groups, request.getkStandard());
				calculationResult.setStandards(standard);
				calculationResult.setStandardTime(System.nanoTime() - time);
				time = System.nanoTime();
			
				if(!standard.isEmpty()) {
					List<Group> notStandards = this.getNotStandardTrajectories(groups, standard, request.getDistance(), request.getAngle());
					if(!notStandards.isEmpty()) {
						Collections.sort(notStandards, new Comparator<Group>() {
							public int compare(Group o1, Group o2) {
								return (int) (o2.getTrajectories().size() - o1.getTrajectories().size());
							}
						});
						calculationResult.setNotStandards(notStandards);
						calculationResult.setSegmentationTime(System.nanoTime() - time);
						time = System.nanoTime();
					}
				}
			}
		}
		return calculationResult;
	}
	
	public List<Trajectory> getCandidatesTrajectories(List<Trajectory> trajectories, Grid gridStart, Grid gridEnd, double tStart, double tEnd, TODSResult result) throws Exception {
    	List<Trajectory> subTrajectories = new ArrayList<Trajectory>();
    	for(Trajectory trajectory : trajectories) {
    		Trajectory subTrajectory = this.getSubTrajectory(trajectory, gridStart, gridEnd);
    		if(subTrajectory != null && !subTrajectory.getPoints().isEmpty() && 
    				tStart <= subTrajectory.getPoints().get(0).getHour() &&
    				subTrajectory.getPoints().get(subTrajectory.getPoints().size() - 1).getHour() <= tEnd) {
    			subTrajectories.add(subTrajectory);
    			result.getRawResult().add(trajectory);
    			for(Point p : subTrajectory.getPoints()) {
    				p.setStandard(false);
    			}
    		}
    	}
    	return subTrajectories;
    }
	
	public Trajectory getSubTrajectory(Trajectory trajectory, Grid gS, Grid gE) {
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
    	} else {
    		return null;
    	}
    	subT.sortPoints();
    	return subT;
    }
	
	public List<Group> getGroupTrajectories(List<Trajectory> candidates, double d) {
    	List<Group> groups = new ArrayList<Group>();
    	for(Trajectory candidate : candidates) {
    		Group currentGroup = null;
    		for(Group g : groups) {
    			for(Point pc : candidate.getPoints()) {
    				currentGroup = null;
    				for(Trajectory tg : g.getTrajectories()) {
    					if(tg.binarySearch(pc, d) != null) {
							currentGroup = g;
							break;
    					}
    				}
    				if(currentGroup == null) break;
    			}
    			if(currentGroup != null) break;
    		}
    		if(currentGroup != null) {
    			currentGroup.getTrajectories().add(candidate);
    		} else {
    			Group group = new Group();
    			group.getTrajectories().add(candidate);
    			groups.add(group);
    		}
    	}
    	return groups;
    }
	
	public List<Group> getStandardTrajectories(List<Group> groups, int k) {
    	Collections.sort(groups, new Comparator<Group>() {
			public int compare(Group o1, Group o2) {
				return (int) (o2.getTrajectories().size() - o1.getTrajectories().size());
			}
		});
    	return groups.subList(0, k);
    }
	
	public List<Group> getNotStandardTrajectories(List<Group> GT, List<Group> STG, double distance, double angle) {
    	List<Group> NSG = new ArrayList<Group>();
    	for(Group g : GT) {
    		if(!STG.contains(g)) {
    			NSG.add(g);
    		}
    	}
    	for(Group ns : NSG) {
    		List<Route> routes = new ArrayList<>();
    		for(Trajectory t : ns.getTrajectories()) {
    			Route route = this.getNotStandardTrajectoriesSegments(t, STG, distance, angle);
    			if(route != null) {
    				routes.add(route);
    			}
    		}
    		ns.setRoutes(routes);
    	}
    	return NSG;
    }
	
	public Route getNotStandardTrajectoriesSegments(Trajectory ns, List<Group> GT, double distance, double angle) {
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
