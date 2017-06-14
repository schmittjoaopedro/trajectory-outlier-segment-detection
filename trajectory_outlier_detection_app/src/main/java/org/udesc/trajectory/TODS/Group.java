package org.udesc.trajectory.TODS;

import java.io.Serializable;
import java.util.*;

public class Group implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Set<Trajectory> trajectories;
	
	private List<Route> routes;
	
	private String name;

	private boolean standard = false;

    private Point[] pointsLat = null;
	
	public Group() {
		super();
	}

	public Set<Trajectory> getTrajectories() {
		if(trajectories == null) trajectories = new HashSet<Trajectory>();
		return trajectories;
	}

	public void setTrajectories(Set<Trajectory> trajectories) {
		this.trajectories = trajectories;
	}

	public List<Route> getRoutes() {
		if(routes == null) routes = new ArrayList<>();
		return routes;
	}

	public void setRoutes(List<Route> routes) {
		this.routes = routes;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isStandard() {
		return standard;
	}

	public void setStandard(boolean standard) {
		this.standard = standard;
	}

	public int trajectoriesSize() {
	    return this.getTrajectories().size();
    }

    public void createIndex() {
        if(pointsLat == null) {
            List<Point> temp = new ArrayList<Point>();
            for (Trajectory t : this.getTrajectories()) {
                temp.addAll(t.getPoints());
            }
            Collections.sort(temp, new Comparator<Point>() {
                public int compare(Point o1, Point o2) {
                    double dif = o2.getLat() - o1.getLat();
                    if(dif < 0.0) return 1;
                    else if(dif > 0.0) return -1;
                    else return 0;
                }
            });
            this.pointsLat = new Point[temp.size()];
            this.pointsLat = temp.toArray(new Point[] {});
        }
    }

    public Point binarySearch(Point p, double distance, Point toIgnore) {
        int low = 0;
        int middle = 0;
        int high = this.pointsLat.length - 1;
        while(high >= low) {
            middle = (low + high) / 2;
            if(this.pointsLat[middle].calculateDistance(p) <= distance) {
                if(this.pointsLat[middle] != toIgnore) {
                    return this.pointsLat[middle];
                } else {
                    break;
                }
            }
            if(this.pointsLat[middle].getLat() < p.getLat()) {
                low = middle + 1;
            } else {
                high = middle - 1;
            }
        }
        if(middle >= 0) {
            int min = middle, max = middle;
            double dist = 0;
            while(dist < distance && min >= 0) {
                dist = Math.abs(this.pointsLat[min].getLat() - p.getLat());
                if(dist <= distance && this.pointsLat[min].calculateDistance(p) <= distance && this.pointsLat[min] != toIgnore) {
                    return this.pointsLat[min];
                }
                min--;
            }
            dist = 0;
            while(dist < distance && max < this.pointsLat.length) {
                dist = Math.abs(this.pointsLat[max].getLat() - p.getLat());
                if(dist <= distance && this.pointsLat[max].calculateDistance(p) <= distance && this.pointsLat[max] != toIgnore) {
                    return this.pointsLat[max];
                }
                max++;
            }
        }
        return null;
    }

    public boolean isLessAngleDifferenceIndex(Point p1Out, Point p2Out, double distance, double angle) {
        Point p1In = this.binarySearch(p1Out, distance, null);
        Point p2In = this.binarySearch(p2Out, distance, p1In);
        if(p1In == p2In) {
            return true;
        } else if(p1In != null && p2In != null) {
            double a1 = Math.toDegrees(Math.atan2(p1Out.getLat() - p2Out.getLat(), p1Out.getLng() - p2Out.getLng()));
            double a2 = Math.toDegrees(Math.atan2(p1In.getLat() - p2In.getLat(), p1In.getLng() - p2In.getLng()));
            if(a1  < 0) a1 = 180 + a1;
            if(a2  < 0) a2 = 180 + a2;
            double diff = Math.abs(a1 - a2);
            if(diff > 90) diff = 180 - diff;
            return diff <= angle;
        } else if((p1In == null && p2In != null) || (p1In != null && p2In == null)) {
            return false;
        } else {
            return true;
        }
    }

}
