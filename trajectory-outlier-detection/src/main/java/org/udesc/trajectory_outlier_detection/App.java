package org.udesc.trajectory_outlier_detection;

import java.util.ArrayList;
import java.util.List;

/**
 * Hello world!
 *
 */
public class App {
	
	//To be configured
	double distance = 0.0;
	double timeStart = 0.0;
	double timeEnd = 24.0;
	double stdQtde = 1;
	
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
    	int count = 0;
    	for(Grid gridStart : regions) {
    		for(Grid gridEnd : regions) {
        		if(gridStart != gridEnd) {
        			candidates = this.getCandidatesTrajectories(trajectories, gridStart, gridEnd, timeStart, timeEnd);
        			System.out.println("Region[" + count++ + "] = " + candidates.size());
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
    				subTrajectory.getPoints().get(0).getHour() >= tStart &&
    				subTrajectory.getPoints().get(subTrajectory.getPoints().size() - 1).getHour() >= tEnd) {
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
    		if (gS.getLatMin() <= p.getLat() && p.getLat() <= gS.getLatMax() && gS.getLngMin() <= p.getLng() && p.getLng() <= gS.getLngMax()) {
    			start = trajectory.getPoints().indexOf(p);
    			found = true;
    			break;
    		} else if (gE.getLatMin() <= p.getLat() && p.getLat() <= gE.getLatMax() && gE.getLngMin() <= p.getLng() && p.getLng() <= gE.getLngMax()) {
    			end = trajectory.getPoints().indexOf(p);
    			found = true;
    			break;
    		}
    	}
    	if(found) {
	    	Point p = null;
	    	for(int i = trajectory.getPoints().size() - 1; i >= 0; i--) {
	    		p = trajectory.getPoints().get(i);
	    		if (gS.getLatMin() <= p.getLat() && p.getLat() <= gS.getLatMax() && gS.getLngMin() <= p.getLng() && p.getLng() <= gS.getLngMax()) {
	    			start = i;
	    			break;
	    		} else if (gE.getLatMin() <= p.getLat() && p.getLat() <= gE.getLatMax() && gE.getLngMin() <= p.getLng() && p.getLng() <= gE.getLngMax()) {
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
    			if  ((gS.getLatMin() <= p.getLat() && p.getLat() <= gS.getLatMax() && gS.getLngMin() <= p.getLng() && p.getLng() <= gS.getLngMax()) ||
    				(gE.getLatMin() <= p.getLat() && p.getLat() <= gE.getLatMax() && gE.getLngMin() <= p.getLng() && p.getLng() <= gE.getLngMax())) {
    				subT.getPoints().remove(p);
    			}	
    		}
    	} else {
    		return null;
    	} 
    	
    	return subT;
    }
    
}
