package org.udesc.trajectory.TODS;

import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneId;
import java.util.*;

import org.udesc.database.IPoint;

/**
 * @author root
 *
 */
public class Point implements Serializable, IPoint<Trajectory> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int id;
	
	private double lat;
	
	private double lng;
	
	private long timestamp;
	
	private boolean standard;
	
	private Trajectory trajectory;
	
	private Grid grid;
	
	private List<Point> cluster;
	
	private boolean processed = false;

	public Point() {
		super();
	}
	
	public Point(double lat, double lng, Trajectory trajectory) {
		super();
		this.lat = lat;
		this.lng = lng;
	}
	
	public Point(double lat, double lng, Trajectory trajectory, long timestamp, boolean standard) {
		super();
		this.lat = lat;
		this.lng = lng;
		this.timestamp = timestamp;
		this.standard = standard;
	}

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
	public double getLat() {
		return lat;
	}

	@Override
	public void setLat(double lat) {
		this.lat = lat;
	}

	@Override
	public double getLng() {
		return lng;
	}

	@Override
	public void setLng(double lng) {
		this.lng = lng;
	}

	@Override
	public long getTimestamp() {
		return timestamp;
	}

	@Override
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public boolean isStandard() {
		return standard;
	}

	public void setStandard(boolean standard) {
		this.standard = standard;
	}
	
	@Override
	public Trajectory getTrajectory() {
		return trajectory;
	}

	@Override
	public void setTrajectory(Trajectory trajectory) {
		this.trajectory = trajectory;
	}
	
	public Grid getGrid() {
		return grid;
	}

	public void setGrid(Grid grid) {
		this.grid = grid;
	}

    public int getHour() {
		return Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDateTime().getHour();
	}
	
	public List<Point> getCluster() {
		if(cluster == null) cluster = new ArrayList<>();
		return cluster;
	}

	public boolean isProcessed() {
		return processed;
	}

	public void setProcessed(boolean processed) {
		this.processed = processed;
	}


    /**
	 * Calculate euclidian distance 
	 * 
	 * @param point
	 * @return distance
	 */
	public double calculateDistance(Point point) {
		return Math.sqrt(Math.pow(this.getLat() - point.getLat(), 2) + Math.pow(this.getLng() - point.getLng(), 2));
	}

	public boolean isLessAngleDifferenceIndex(Point p2Out, double distance, double angle) {
		Point p1In = null;
		double minDist = distance;
		for (Point p : this.getCluster()) {
			if(p.calculateDistance(this) <= minDist && p.getTrajectory().getGroup().isStandard()) {
				minDist = p.calculateDistance(this);
				p1In = p;
			}
		}
		Point p2In = null;
		minDist = distance;
		for (Point p : p2Out.getCluster()) {
			if(!p.equals(p1In) && p.calculateDistance(p2Out) <= minDist && p.getTrajectory().getGroup().isStandard()) {
				minDist = p.calculateDistance(p2Out);
				p2In = p;
			}
		}
		if(p1In == p2In) {
			return true;
		} else if(p1In != null && p2In != null) {
			double a1 = Math.toDegrees(Math.atan2(this.getLat() - p2Out.getLat(), this.getLng() - p2Out.getLng()));
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

	public void drawCircle() {
		System.out.println("drawCircle("+this.getLat()+", "+this.getLng()+", 0.0004)");
		for(Point p : this.getCluster()) {
			//System.out.println("drawCircle("+p.getLat()+", "+p.getLng()+", 0.0004)");
		}
	}

}
