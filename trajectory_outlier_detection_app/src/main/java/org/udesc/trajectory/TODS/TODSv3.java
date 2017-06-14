package org.udesc.trajectory.TODS;

import org.udesc.database.TrajectoryMongoDB;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by root on 11/06/17.
 */
public class TODSv3 {

    private TrajectoryMongoDB trajectoryBase;

    public TODSv3() {
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

    public TODSResult loadTest(TODSRequest request, int rep) throws Exception {
        Long startTime = System.nanoTime();
        List<Trajectory> candidates = (List<Trajectory>) trajectoryBase.findTrajectories(request.getCountry(), request.getState(), request.getCity(), request.getStartHour(), request.getEndHour(), request.getStartGrid(), request.getEndGrid(), Trajectory.class, Point.class);
        List<Trajectory> newTraj = new ArrayList<>();
        for(int i = 0; i < rep; i++) {
            newTraj.addAll(this.reply(candidates));
        }
        candidates.addAll(newTraj);
        startTime = System.nanoTime() - startTime;
        TODSResult calculationResult = this.run(request, candidates);
        calculationResult.setQueryTime(startTime);
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

    public TODSResult runExternal(TODSRequest request, List<Trajectory> candidates) throws Exception {
        Long startTime = System.nanoTime();
        startTime = System.nanoTime() - startTime;
        TODSResult calculationResult = this.run(request, candidates);
        calculationResult.setQueryTime(startTime);
        return calculationResult;
    }

    public TODSResult run(TODSRequest request, List<Trajectory> trajectories) throws Exception {
        TODSResult calculationResult = new TODSResult();
        //####### FILTER TRAJECTORIES #######
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

        //####### GROUP TRAJECTORIES #######
        time = System.nanoTime();
        List<Group> groups = this.getGroupTrajectories(candidates, request.getDistance());
        calculationResult.setGroupTime(System.nanoTime() - time);
        //####### STANDARD TRAJECTORIES #######
        time = System.nanoTime();
        List<Group> standard = this.getStandardTrajectories(groups, request.getkStandard());
        calculationResult.setStandards(standard);
        calculationResult.setStandardTime(System.nanoTime() - time);
        time = System.nanoTime();
        //####### SEGMENT TRAJECTORIES #######
        List<Group> notStandards = this.getNotStandardTrajectories(groups, standard, request.getDistance(), request.getAngle());
        Collections.sort(notStandards, new Comparator<Group>() {
            public int compare(Group o1, Group o2) {
                return (int) (o2.getTrajectories().size() - o1.getTrajectories().size());
            }
        });
        calculationResult.setNotStandards(notStandards);
        calculationResult.setSegmentationTime(System.nanoTime() - time);
        time = System.nanoTime();
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
            public int compare(Group o1, Group o2) { return (int) (o2.getTrajectories().size() - o1.getTrajectories().size()); }
        });
        List<Group> STD = groups.subList(0, k);
        for (Group group : STD) {
            group.createIndex();
        }
        return STD;
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
        Boolean lastStatus = null;
        List<Trajectory> trajectories = new ArrayList<>();
        Trajectory currTrajectory = new Trajectory();
        trajectories.add(currTrajectory);
        for(int j = 0; j < ns.getPoints().size(); j++) {
            Point p = ns.getPoints().get(j);
            p.setStandard(false);
            for(Group g : GT) {
                if(g.binarySearch(p, distance, null) != null) {
                    p.setStandard(true);
                    break;
                }
            }
            if(j == 0) {
                currTrajectory.getPoints().add(p);
                if(p.isStandard()) {
                    route.getStandards().add(currTrajectory);
                } else {
                    route.getNotStandards().add(currTrajectory);
                }
            }
            if(j > 0 && lastStatus != p.isStandard()) {
                int prev = j - 1;
                Point current = p;
                Point prevP = ns.getPoints().get(prev);
                if(!p.isStandard()) {
                    Trajectory newTrajectory = new Trajectory();
                    trajectories.add(newTrajectory);
                    route.getNotStandards().add(newTrajectory);
                    newTrajectory.getPoints().add(p);
                    while (prevP.isStandard() && prev >= 0) {
                        for(Group g : GT) {
                            if(!g.isLessAngleDifferenceIndex(current, prevP, distance, angle)) {
                                prevP.setStandard(false);
                                currTrajectory.getPoints().remove(prevP);
                                newTrajectory.getPoints().add(0, prevP);
                            }
                        }
                        if (prevP.isStandard() || prev == 0) break;
                        current = prevP;
                        prev--;
                        prevP = ns.getPoints().get(prev);
                        if(currTrajectory.getPoints().isEmpty()) {
                            route.getStandards().remove(currTrajectory);
                        }
                    }
                    currTrajectory = newTrajectory;
                } else if(p.isStandard() && !prevP.isStandard()) {
                    for(Group g : GT) {
                        if(!g.isLessAngleDifferenceIndex(current, prevP, distance, angle)) {
                            p.setStandard(false);
                        }
                    }
                    if(p.isStandard()) {
                        Trajectory newTrajectory = new Trajectory();
                        trajectories.add(newTrajectory);
                        route.getStandards().add(newTrajectory);
                        newTrajectory.getPoints().add(p);
                        currTrajectory = newTrajectory;
                    }
                }
            } else {
                currTrajectory.getPoints().add(p);
            }
            lastStatus = p.isStandard();
        }

        if(trajectories.size() > 1) {
            for (int i = 1; i < trajectories.size(); i++) {
                if(!trajectories.get(i).getPoints().isEmpty())
                    trajectories.get(i - 1).getPoints().add(trajectories.get(i).getPoints().get(0));
            }
        }
        return route;
    }

}
