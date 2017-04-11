package org.udesc.trajectory_outlier_detection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Hello world!
 *
 */
public class App {
	
	//To be configured
	double distance = 0.0003;
	double timeStart = 0.0;
	double timeEnd = 24.0;
	int stdQtde = 1;
	
	double latSt = -26.370924;
	double latEn = -26.237597;
	double lngSt = -48.944078;
	double lngEn = -48.775826;
	double L = 30.0;
	
	//References
	List<Trajectory> trajectories;
	List<Grid> regions;
	List<Trajectory> candidates;
	List<Group> trajectoriesGroup = new ArrayList<Group>();
	List<Group> standardTrajectoriesGroup = new ArrayList<Group>();
	List<Group> notStandardTrajectoriesGroup = new ArrayList<Group>();
	
	Database database = new Database("/home/joao/Área de Trabalho/Mestrado/Extracted/tidy/LGE Nexus 4");
	
    public static void main( String[] args ) {
    	new App().run();
    }
    
    public void run() {
    	database.initialize();
    	trajectories = database.getTrajectories();
    	regions = this.createGrid();
    	
    	for(int i = 0; i < regions.size(); i++) {
    		for(int j = i + 1; j < regions.size(); j++) {
    			candidates = this.getCandidatesTrajectories(trajectories, regions.get(i), regions.get(j), timeStart, timeEnd);
    			if(!candidates.isEmpty()) {
    				List<Group> groups = this.getGroupTrajectories(candidates, distance);
    				if(!groups.isEmpty()) {
    					List<Group> standard = this.getStandardTrajectories(groups, stdQtde);
    				}
    			}
        	}
    	}
    }
    
    public List<Grid> createGrid() {
    	List<Grid> grids = new ArrayList<Grid>();
    	double W = (latEn - latSt) / L;
    	double H = (lngEn - lngSt) / L;
    	for(double w = 0; w < L; w++) {
    		for(double h = 0; h < L; h++) {
    			Grid grid = new Grid();
    			grid.setLatMin(latSt + (W * w));
    			grid.setLatMax(latSt + (W * (w + 1)));
    			grid.setLngMin(lngSt + (H * h));
    			grid.setLngMax(lngSt + (H * (h + 1)));
    			grids.add(grid);
    		}
    	}
    	return grids;
    }
    
    public List<Trajectory> getCandidatesTrajectories(List<Trajectory> trajectories, Grid gridStart, Grid gridEnd, double tStart, double tEnd) {
    	List<Trajectory> subTrajectories = new ArrayList<Trajectory>();
    	for(Trajectory trajectory : trajectories) {
    		Trajectory subTrajectory = this.getSubTrajectory(trajectory, gridStart, gridEnd);
    		if(subTrajectory != null && !subTrajectory.getPoints().isEmpty() && 
    				tStart <= subTrajectory.getPoints().get(0).getHour() &&
    				subTrajectory.getPoints().get(subTrajectory.getPoints().size() - 1).getHour() <= tEnd) {
    			subTrajectories.add(subTrajectory);
    		}
    	}
    	return subTrajectories;
    }
    
    public Trajectory getSubTrajectory(Trajectory trajectory, Grid gS, Grid gE) {
    	boolean found = false;
    	int start = 0;
    	int end = 0;
    	Trajectory subT = new Trajectory();
    	subT.setName(trajectory.getName());
    	for(Point p : trajectory.getPoints()) {
    		if (gS.betweenLat(p) && gS.betweenLng(p)) {
    			start = trajectory.getPoints().indexOf(p);
    			found = true;
    			break;
    		} else if (gE.betweenLat(p) && gE.betweenLng(p)) {
    			end = trajectory.getPoints().indexOf(p);
    			found = true;
    			break;
    		}
    	}
    	if(found) {
	    	Point p = null;
	    	for(int i = trajectory.getPoints().size() - 1; i >= 0; i--) {
	    		p = trajectory.getPoints().get(i);
	    		if (gS.betweenLat(p) && gS.betweenLng(p) && start == 0) {
	    			start = i;
	    			break;
	    		} else if (gE.betweenLat(p) && gE.betweenLng(p) && end == 0) {
	    			end = i;
	    			break;
	    		}
	    	}
    	}
    	if (start > end) {
    		int temp = start;
    		start = end;
    		end = temp;
    	}
    	if (start < end) {
    		List<Point> points = trajectory.getPoints().subList(start, end);
    		subT.getPoints().addAll(points);
    		for(Point p : points) {
    			if  ((gS.betweenLat(p) && gS.betweenLng(p)) ||
    				(gE.betweenLat(p) && gE.betweenLng(p))) {
    				subT.getPoints().remove(p);
    			}
    		}
    	} else {
    		return null;
    	} 
    	return subT;
    }
   
    public List<Group> getGroupTrajectories(List<Trajectory> candidates, double d) {
    	List<Group> groups = new ArrayList<Group>();
    	for(Trajectory candidate : candidates) {
    		Group currentGroup = null;
    		for(Group g : groups) {
    			for(Point pc : candidate.getPoints()) {
    				for(Trajectory tg : g.getTrajectories()) {
    					for(Point pg : tg.getPoints()) {
    						if(Math.sqrt(Math.pow(pg.getLat() - pc.getLat(), 2) + Math.pow(pg.getLng() - pc.getLng(), 2)) <= d) {
    							currentGroup = g;
    							break;
    						} else {
    							currentGroup = null;
    						}
    					}
    					if(currentGroup == null) break;
    				}
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
				return (int) (o1.getTrajectories().size() - o2.getTrajectories().size());
			}
		});
    	return groups.subList(0, k);
    }
    
}
