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
	
	double latSt = -26.370924;
	double latEn = -26.237597;
	double lngSt = -48.944078;
	double lngEn = -48.775826;
	double L = 30.0;
	String outputFolder = "/home/joao/Área de Trabalho/Mestrado/trajectory-outlier-segment-detection/maps/output";
	
	//References
	List<Trajectory> trajectories;
	List<Grid> regions;
	List<Trajectory> candidates;
	List<Group> trajectoriesGroup = new ArrayList<Group>();
	List<Group> standardTrajectoriesGroup = new ArrayList<Group>();
	List<Group> notStandardTrajectoriesGroup = new ArrayList<Group>();
	
	Database database = new Database("/home/joao/Área de Trabalho/Mestrado/Extracted/tidy/LGE Nexus 4");
	
    public static void main( String[] args ) throws Exception {
    	new App().run();
    }
    
    public void run() throws Exception {
    	database.initialize();
    	trajectories = database.getTrajectories();
    	regions = this.createGrid();
    	
    	long start = System.currentTimeMillis();
    	for(int i = 0; i < regions.size(); i++) {
    		for(int j = i + 1; j < regions.size(); j++) {
    			calculateRegions(i, j);
        	}
    		System.out.println(i);
    	}
//    	calculateRegions(312, 341);
    	System.out.println("End in: " + (System.currentTimeMillis() - start));
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
    	int start = -1;
    	int end = -1;
    	double distance = -1;
    	
    	Trajectory subT = new Trajectory();
    	subT.setName(trajectory.getName());
    	
    	for(int i = 0; i < trajectory.getPoints().size(); i++) {
    		Point pS = trajectory.getPoints().get(i);
    		if (gS.betweenLat(pS) && gS.betweenLng(pS)) {
    			for(int j = i + 1; j < trajectory.getPoints().size(); j++) {
    				Point pE = trajectory.getPoints().get(j);
        			if(pE != pS && gE.betweenLat(pE) && gE.betweenLng(pE)) {
        				if(pE.calculateDistance(pS) <= distance || distance == -1) {
        					start = i;
        					end = j;
        					distance = pE.calculateDistance(pS);
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
    						if(pg.calculateDistance(pc) <= d) {
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
    	
    	boolean isStd = false;
    	Boolean lastStd = null;
    	
    	List<Point> segment = new ArrayList<Point>();
    	for(Point p : ns.getPoints()) {
    		isStd = false;
    		for(Group g : GT) {
    			for(Trajectory st : g.getTrajectories()) {
    				for(Point ps : st.getPoints()) {
    					if(ps.calculateDistance(p) <= distance) {
    						isStd = true;
    						break;
    					}
    				}
    				if(isStd == true) break;
    			}
    			if(isStd == true) break;
    		}
    		if((lastStd == null || lastStd != isStd) && segment.size() > 1) {
    			if(lastStd != null) {
    				Trajectory t = new Trajectory();
    				t.setPoints(segment);
    				if(lastStd)
    					route.getStandards().add(t);
    				else
    					route.getNotStandards().add(t);
        			segment = new ArrayList<Point>();
    			}
    			lastStd = isStd;
    		}
    		segment.add(p);
    	}
    	if(lastStd != null) {
			Trajectory t = new Trajectory();
			t.setPoints(segment);
			if(lastStd)
				route.getStandards().add(t);
			else
				route.getNotStandards().add(t);
		}
    	return route;
    }
    
    public void printData(Grid SR, Grid ER, List<Group> ST, List<Route> NST, int i, int j) throws Exception {
    	
    	String file = outputFolder + "/_" + i + "_" + j + ".txt";
    	StringBuilder data = new StringBuilder();
    	
    	data.append("Regions\n");
    	data.append(SR.toStringStart() + "\n");
    	data.append(ER.toStringEnd() + "\n");
    	
    	data.append("Default full trajectories\n");
    	for(Group g : ST) {
    		for(Trajectory t : g.getTrajectories()) {
    			data.append(t.toStringDefault() + "\n");
    		}
    	}
    	data.append("Default segment trajectories\n");
    	for(Route r : NST) {
    		for(Trajectory ts : r.getStandards()) {
    			data.append(ts.toStringDefault() + "\n");
    		}
    	}
    	data.append("Outelier segment trajectories\n");
    	for(Route r : NST) {
    		for(Trajectory nts : r.getNotStandards()) {
    			data.append(nts.toStringOutlier() + "\n");
    		}
    	}
    	FileUtils.write(new File(file), data.toString(), "UTF-8");
//    	System.out.println(data.toString());
    	
    }
    
    
}
