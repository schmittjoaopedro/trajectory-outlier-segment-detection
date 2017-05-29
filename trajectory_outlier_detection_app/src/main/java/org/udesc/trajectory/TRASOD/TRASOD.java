package org.udesc.trajectory.TRASOD;

import java.util.ArrayList;
import java.util.List;

import org.udesc.database.TrajectoryMongoDB;

public class TRASOD {

	private TrajectoryMongoDB trajectoryBase;
	
	public TRASOD() {
		super();
		trajectoryBase = new TrajectoryMongoDB();
	}
	
	@SuppressWarnings("unchecked")
	public TRASODResult run(TRASODRequest request) throws Exception {
		Long startTime = System.currentTimeMillis();
		List<Trajectory> trajectories = trajectoryBase.findTrajectories(request.getCountry(), request.getState(), request.getCity(), 0, 23, request.getStartGrid(), request.getEndGrid(), Trajectory.class, Point.class);
		startTime = System.currentTimeMillis() - startTime;
		TRASODResult calculationResult = this.run(request, trajectories);
		return calculationResult;
	}
	
	public TRASODResult run(TRASODRequest request, List<Trajectory> trajectories) throws Exception {
		TRASODResult calculationResult = new TRASODResult();
		Long startTime = System.currentTimeMillis();
		calculationResult.setRawResult(trajectories);
		calculationResult.setQueryTime(System.currentTimeMillis() - startTime);
		startTime = System.currentTimeMillis();
		List<Trajectory> candidates = this.getCandidatesTrajectories(trajectories, request.getStartGrid(), request.getEndGrid());
		List<Trajectory> standards = this.getStandards(candidates, request.getMaxDist(), request.getMinSup());
		List<Trajectory> spatialOutSet = this.getSpatialOutset(candidates, standards);
		calculationResult.setStandards(standards);
		calculationResult.setNotStandards(spatialOutSet);
		calculationResult.setProgramTime(System.currentTimeMillis() - startTime);
		return calculationResult;
	}
	
	private List<Trajectory> getCandidatesTrajectories(List<Trajectory> trajectories, Grid gridStart, Grid gridEnd) throws Exception {
    	List<Trajectory> subTrajectories = new ArrayList<Trajectory>();
    	for(Trajectory trajectory : trajectories) {
    		Trajectory subTrajectory = this.getSubTrajectory(trajectory, gridStart, gridEnd);
    		if(subTrajectory != null && !subTrajectory.getPoints().isEmpty()) {
    			subTrajectories.add(subTrajectory);
    		}
    	}
    	return subTrajectories;
    }
	
	private Trajectory getSubTrajectory(Trajectory trajectory, Grid gS, Grid gE) {
    	int start = -1;
    	int end = -1;
    	double timeDifference = -1;
    	Trajectory subT = new Trajectory();
    	subT.setName(trajectory.getName());
    	
    	for(int i = 0; i < trajectory.getPoints().size(); i++) {
    		Point pS = trajectory.getPoints().get(i);
    		if (gS.betweenLat(pS) && gS.betweenLng(pS)) {
    			for(int j = 0; j < trajectory.getPoints().size(); j++) {
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
    	if(start > end) {
    		return null;
    		/*int temp = start;
    		start = end;
    		end = temp;*/
    	}
    	if (start < end && start != -1 && end != -1) {
    		subT.getPoints().addAll(trajectory.getPoints().subList(start, end + 1));
    	} else {
    		return null;
    	}
    	subT.sortPoints();
    	return subT;
    }
	
	private List<Trajectory> getStandards(List<Trajectory> candidates, double maxDist, double minSup) {
		List<Trajectory> standards = new ArrayList<>();
		for(Trajectory candidate : candidates) {
			boolean standard = true;
			for(Point point : candidate.getPoints()) {
				int count = 0;
				for(Trajectory trajectory : candidates) {
					if(candidate != trajectory) {
						for(Point pt : trajectory.getPoints()) {
							if(pt.calculateDistance(point) <= maxDist) {
								count++;
								break;
							}
						}
					}
				}
				if(count < minSup) {
					standard = false;
				}
			}
			if(standard) {
				standards.add(candidate);
			}
		}
		return standards;
	}

	private List<Trajectory> getSpatialOutset(List<Trajectory> candidates, List<Trajectory> standards) {
		List<Trajectory> spatialOutSet = new ArrayList<>();
		for(Trajectory trajectory : candidates) {
			if(!standards.contains(trajectory)) {
				spatialOutSet.add(trajectory);
			}
		}
		return spatialOutSet;
	}
	
}
