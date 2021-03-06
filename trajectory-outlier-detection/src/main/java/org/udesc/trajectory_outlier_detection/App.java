package org.udesc.trajectory_outlier_detection;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.io.FileUtils;

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
	
//	Joinville
	double latSt = -26.370924;
	double latEn = -26.237597;
	double lngSt = -48.944078;
	double lngEn = -48.775826;
//	San Francisco
//	double latSt = 37.711624;
//	double latEn = 37.813405;
//	double lngSt = -122.495955;
//	double lngEn = -122.390732;
	double L = 30.0;
	String outputFolder = "/home/joao/Área de Trabalho/Mestrado/trajectory-outlier-segment-detection/maps/output";
	
	//References
	List<Grid> regions;
	List<Trajectory> trajectories;
	List<Trajectory> candidates;
	List<Group> trajectoriesGroup = new ArrayList<Group>();
	List<Group> standardTrajectoriesGroup = new ArrayList<Group>();
	List<Group> notStandardTrajectoriesGroup = new ArrayList<Group>();
	
	PostgreSQL postgreSQL = new PostgreSQL("trajectory_test");
//	PostgreSQL postgreSQL = new PostgreSQL("trajectory_geolife");
	
	Database database = new Database("/home/joao/Área de Trabalho/Mestrado/Extracted/tidy/LGE Nexus 4");
	
    public static void main( String[] args ) throws Exception {
    	new App().run();
    }
    
    public void run() throws Exception {
    	long start = System.currentTimeMillis();
    	System.out.println("Loading db...");
    	
    	trajectories = postgreSQL.loadAllTrajectories();

//     	UberExtractor extractor = new UberExtractor();
//		extractor.loadTrajectories("/home/joao/Área de Trabalho/Mestrado/Extracted/uber/all.tsv");
//		trajectories = extractor.getTrajectories();
    	
//    	this.getBounds(trajectories);
    	
    	regions = this.createGrid();
    	System.out.println("DB load in: "  + (System.currentTimeMillis() - start));
    	System.out.println("Trajectories loaded: " + trajectories.size());
    	
    	start = System.currentTimeMillis();
    	System.out.println("Starting calculations...");
    	
//    	for(int i = 0; i < regions.size(); i++) {
//    		long tStart = System.currentTimeMillis();
//    		for(int j = i + 1; j < regions.size(); j++) {
//    			calculateRegions(i, j);
//        	}
//    		System.out.println(i + " in " + (System.currentTimeMillis() - tStart));
//    	}
    	
//    	for(int n = 0; n < 10000; n++) {
//    		int i = (int) (Math.random() * regions.size());
//    		int j = (int) (Math.random() * regions.size());
//    		calculateRegions(i, j);
//    		if(n % 100 == 0) {
//    			System.out.println(n);
//    		}
//    	}
    	
    	calculateRegions(283,438);
    	
//    	Joinville
//    	calculateRegions(554,526);
    	
//    	Uber
//    	calculateRegions(176, 560);
    	
    	System.out.println("End in: " + (System.currentTimeMillis() - start));
    }
    
    public void calculateRegionsTime(int i, int j) throws Exception {
    	for(int t = 1; t <= 24; t++) {
    		if(t != 24) {
    			timeStart = t - 1;
    			timeEnd = t;
    		} else {
    			timeStart = 23;
    			timeEnd = 0;
    		}
    		calculateRegions(i, j);
    	}
    }

	private void calculateRegions(int i, int j) throws Exception {
		candidates = this.getCandidatesTrajectories(trajectories, regions.get(i), regions.get(j), timeStart, timeEnd);
		if(!candidates.isEmpty()) {
			List<Group> groups = this.getGroupTrajectories(candidates, distance);
			if(!groups.isEmpty()) {
				List<Group> standard = this.getStandardTrajectories(groups, stdQtde);
				if(!standard.isEmpty()) {
					List<Route> routes = this.getNotStandardTrajectories(groups, standard, distance);
					boolean hasOutlier = false;
					for(Route r : routes) {
						if(!r.getNotStandards().isEmpty()) {
							hasOutlier = true;
							break;
						}
					}
					if(hasOutlier)
						printData(regions.get(i), regions.get(j), standard, routes, i, j);
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
    
    public List<Trajectory> getCandidatesTrajectories(List<Trajectory> trajectories, Grid gridStart, Grid gridEnd, double tStart, double tEnd) throws Exception {
    	List<Trajectory> subTrajectories = new ArrayList<Trajectory>();
    	for(Trajectory trajectory : trajectories) {
    		Trajectory subTrajectory = this.getSubTrajectory(trajectory, gridStart, gridEnd);
    		if(subTrajectory != null && !subTrajectory.getPoints().isEmpty() && 
    				tStart <= subTrajectory.getPoints().get(0).getHour() &&
    				subTrajectory.getPoints().get(subTrajectory.getPoints().size() - 1).getHour() <= tEnd) {
    			subTrajectories.add(subTrajectory);
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
    	subT.initialize();
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
    
    public List<Route> getNotStandardTrajectories(List<Group> GT, List<Group> STG, double d) {
    	List<Group> NSG = new ArrayList<Group>();
    	List<Route> routes = new ArrayList<Route>();
    	for(Group g : GT) {
    		if(!STG.contains(g)) {
    			NSG.add(g);
    		}
    	}
    	for(Group ns : NSG) {
    		for(Trajectory t : ns.getTrajectories()) {
    			Route route = this.getNotStandardTrajectoriesSegments(t, STG, d);
    			if(route != null) {
    				routes.add(route);
    			}
    		}
    	}
    	return routes;
    }
    
    public Route getNotStandardTrajectoriesSegments(Trajectory ns, List<Group> GT, double distance) {
    	Route route = new Route();
    	for(Point p : ns.getPoints()) {
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
    	    				if(!st.isLowerBoundAngle(current, prevP, distance, 30.0)) {
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
    	    				if(!st.isLowerBoundAngle(current, nextP, distance, 30.0)) {
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
    			Point temp = new Point(prev.getHour(), prev.getMinutes(), prev.getLat(), prev.getLng(), prev.getTimestamp());
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
    
    public void printData(Grid SR, Grid ER, List<Group> ST, List<Route> NST, int i, int j) throws Exception {
    	
    	String file = outputFolder + "/_" + i + "_" + j + "_" + timeStart + ".txt";
    	StringBuilder data = new StringBuilder();
    	
    	data.append("//Regions\n");
    	data.append(SR.toStringStart() + "\n");
    	data.append(ER.toStringEnd() + "\n");
    	
    	data.append("\n//Default full trajectories\n");
    	for(int n = 0; n < ST.size(); n++) {
    		data.append("\t//Group " + n + " = " + ST.get(n).getTrajectories().size() + "\n");
    		for(Trajectory t : ST.get(n).getTrajectories()) {
    			data.append(t.toStringDefault() + "\n");
    		}
    	}
    	data.append("\n//Default segment trajectories\n");
    	for(int n = 0; n < NST.size(); n++) {
    		data.append("\t//Default segment " + n + " = " + NST.get(n).getStandards().size() + "\n");
    		for(Trajectory ts : NST.get(n).getStandards()) {
    			data.append(ts.toStringDefault() + "\n");
    		}
    	}
    	data.append("\n//Outlier segment trajectories\n");
    	for(int n = 0; n < NST.size(); n++) {
    		data.append("\t//Outlier segment " + n + " = " + NST.get(n).getNotStandards().size() + "\n");
    		for(Trajectory nts : NST.get(n).getNotStandards()) {
    			data.append(nts.toStringOutlier() + "\n");
    		}
    	}
    	FileUtils.write(new File(file), data.toString(), "UTF-8");
    	
    }
    
    public void getBounds(List<Trajectory> trajectories) {
    	double minLat = 0, maxLat = 0, minLng = 0, maxLng = 0;
    	for(Trajectory trajectory : trajectories) {
    		for(Point p : trajectory.getPoints()) {
    			if(p.getLat() < minLat || minLat == 0) {
    				minLat = p.getLat();
    			}
    			if(p.getLat() > maxLat || maxLat == 0) {
    				maxLat = p.getLat();
    			}
    			if(p.getLng() < minLng || minLng == 0) {
    				minLng = p.getLng();
    			}
    			if(p.getLng() > maxLng || maxLng == 0) {
    				maxLng = p.getLng();
    			}
    		}
    	}
    	System.out.println("Min lat: " + minLat);
    	System.out.println("Max lat: " + maxLat);
    	System.out.println("Min lng: " + minLng);
    	System.out.println("Max lng: " + maxLng);
    }
    
}
