package org.udesc.trajectory.TODS;

import java.util.*;

public class IndexManager {

	private Double minLat;
	
	private Double maxLat;
	
	private Double minLng;
	
	private Double maxLng;
	
	private Double dist;
	
	private Grid[][] grids;

	private int[][] pointTrajectory;
	
	private List<Trajectory> trajectories = new ArrayList<>();

//	public IndexManager(List<Trajectory> trajectories) {
//		this.trajectories.addAll(trajectories);
//	}

	public IndexManager(List<Trajectory> trajectories, double distance) {
        this.trajectories = trajectories;
        int pointSize = 0;
        int trajectorySize = 0;
        for(Trajectory t : trajectories) {
            t.setId(trajectorySize++);
            for(Point p : t.getPoints()) {
                p.setId(pointSize++);
                if(minLat == null || p.getLat() < minLat) {
                    minLat = p.getLat();
                }
                if(maxLat == null || p.getLat() > maxLat) {
                    maxLat = p.getLat();
                }
                if(minLng == null || p.getLng() < minLng) {
                    minLng = p.getLng();
                }
                if(maxLng == null || p.getLng() > maxLng) {
                    maxLng = p.getLng();
                }
            }
        }
        this.pointTrajectory = new int[trajectorySize + 1][trajectorySize + 1];
        dist = distance;
        int deltaLat = (int) (((maxLat - minLat) / dist) + 1.0);
        int deltaLng = (int) (((maxLng - minLng) / dist) + 1.0);
        grids = new Grid[deltaLat][deltaLng];
        for(Trajectory t : trajectories) {
            for (Point p : t.getPoints()) {
                int latIdx = (int) ((p.getLat() - minLat) / dist);
                int lngIdx = (int) ((p.getLng() - minLng) / dist);
                if (grids[latIdx][lngIdx] == null) grids[latIdx][lngIdx] = new Grid();
                grids[latIdx][lngIdx].getPoints().add(p);
                p.setGrid(grids[latIdx][lngIdx]);
            }
        }
        for(Trajectory t : trajectories) {
            for(int i = 0; i < t.getPoints().size(); i++) {
                processCluster(t.getPoints().get(i), i + 1);
            }
        }
    }

	public void defineLimits() {
		for(Trajectory t : trajectories) {
			for(Point p : t.getPoints()) {
				if(minLat == null || p.getLat() < minLat) {
					minLat = p.getLat();
				}
				if(maxLat == null || p.getLat() > maxLat) {
					maxLat = p.getLat();
				}
				if(minLng == null || p.getLng() < minLng) {
					minLng = p.getLng();
				}
				if(maxLng == null || p.getLng() > maxLng) {
					maxLng = p.getLng();
				}
			}
		}
	}
	
	public void createIndex(double distance) {
		dist = distance;
		int deltaLat = (int) (((maxLat - minLat) / dist) + 1.0);
		int deltaLng = (int) (((maxLng - minLng) / dist) + 1.0);
		grids = new Grid[deltaLat][deltaLng];
		for(int lat = 0; lat < deltaLat; lat++) {
			for(int lng = 0; lng < deltaLng; lng++) {
				Grid grid = new Grid();
    			grid.setLatMin(minLat + (dist * lat));
    			grid.setLatMax(minLat + (dist * (lat + 1)));
    			grid.setLngMin(minLng + (dist * lng));
    			grid.setLngMax(minLng + (dist * (lng + 1)));
    			grids[lat][lng] = grid;
			}
		}
		for(Trajectory t : trajectories) {
			for(Point p : t.getPoints()) {
				int latIdx = (int) ((p.getLat() - minLat) / dist);
				int lngIdx = (int) ((p.getLng() - minLng) / dist);
				grids[latIdx][lngIdx].getPoints().add(p);
				p.setGrid(grids[latIdx][lngIdx]);
			}
		}
		for(Trajectory t : trajectories) {
		    for(int i = 0; i < t.getPoints().size(); i++) {
		        processCluster(t.getPoints().get(i), i + 1);
            }
        }
	}

	public void print() {
		for(int x = 0; x < grids.length; x++) {
			for(int y = 0; y < grids[x].length; y++) {
				if(grids[x][y].getPoints().size() > 0) {
					//System.out.print(grids[x][y].getPoints().size());
					System.out.print("x");
				} else {
					System.out.print(" ");
				}
			}
			System.out.print("\n");
		}
	}

	public long getTotalPoints() {
		long total = 0l;
		for(Trajectory t : trajectories) {
			total += t.getPoints().size();
		}
		return total;
	}
	
	public static String drawPolyline(List<Point> points, String color) {
		StringBuilder data = new StringBuilder();
		data.append("\ndrawPoints([");
		for(int i = 0; i < points.size(); i++) {
			data.append("{lat: " + points.get(i).getLat() + ",lng:" + points.get(i).getLng() + "}");
			if(i < points.size() - 1) {
				data.append(",");
			}
		}
		data.append("], '#" + color + "');");
		return data.toString();
	}
	
	public void processCluster(Point p, int sum) {
        Set<Point> points = getPoints(p);
        for(Point pY : points) {
			if(!p.getTrajectory().equals(pY.getTrajectory()) &&
					pY.calculateDistance(p) <= dist) {
				p.getCluster().add(pY);
				if(this.pointTrajectory[p.getTrajectory().getId()][pY.getTrajectory().getId()] + 1 == sum)
				    this.pointTrajectory[p.getTrajectory().getId()][pY.getTrajectory().getId()] = sum;
			}
		}
	}

    public Set<Point> getPoints(Point p) {
        //Center
        int latIdx = (int) ((p.getLat() - minLat) / dist);
        int lngIdx = (int) ((p.getLng() - minLng) / dist);

        Set<Point> points = new HashSet<>();

        if(latIdx >= 0 && lngIdx >= 0 && latIdx < grids.length && lngIdx < grids[latIdx].length && grids[latIdx][lngIdx] != null) {
            points.addAll(grids[latIdx][lngIdx].getPoints());
            //Right
            if(grids.length > latIdx + 1 && grids[latIdx + 1][lngIdx] != null) {
                points.addAll(grids[latIdx + 1][lngIdx].getPoints());
            }
            //Left
            if(latIdx - 1 > 0 && grids[latIdx - 1][lngIdx] != null) {
                points.addAll(grids[latIdx - 1][lngIdx].getPoints());
            }
            //Top
            if(grids[0].length > lngIdx + 1 && grids[latIdx][lngIdx + 1] != null) {
                points.addAll(grids[latIdx][lngIdx + 1].getPoints());
            }
            //Bottom
            if(lngIdx - 1 > 0 && grids[latIdx][lngIdx - 1] != null) {
                points.addAll(grids[latIdx][lngIdx - 1].getPoints());
            }
            //Right Top
            if(grids.length > latIdx + 1 && grids[0].length > lngIdx + 1 && grids[latIdx + 1][lngIdx + 1] != null) {
                points.addAll(grids[latIdx + 1][lngIdx + 1].getPoints());
            }
            //Left Bottom
            if(latIdx - 1 > 0 && lngIdx - 1 > 0 && grids[latIdx - 1][lngIdx - 1] != null) {
                points.addAll(grids[latIdx - 1][lngIdx - 1].getPoints());
            }
            //Left Top
            if(grids[0].length > lngIdx + 1 && latIdx - 1 > 0 && grids[latIdx - 1][lngIdx + 1] != null) {
                points.addAll(grids[latIdx - 1][lngIdx + 1].getPoints());
            }
            //Right Bottom
            if(lngIdx - 1 > 0 && grids.length > latIdx + 1 && grids[latIdx + 1][lngIdx - 1] != null) {
                points.addAll(grids[latIdx + 1][lngIdx - 1].getPoints());
            }
        }

        return points;
    }


    public List<Trajectory> getTrajectories() {
		if(trajectories == null) trajectories = new ArrayList<>();
		return trajectories;
	}

	public int[] calculateTrajectories(int trajectoryId) {
	    return pointTrajectory[trajectoryId];
    }

}
