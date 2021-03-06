package org.udesc.trajectory.TRASOD;

import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneId;

import org.udesc.database.IPoint;

public class Point implements Serializable, IPoint<Trajectory> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int id;

	private double lat;
	
	private double lng;
	
	private long timestamp;
	
	private Trajectory trajectory;
	
	public Point() {
		super();
	}

	public Point(double lat, double lng) {
		super();
		this.lat = lat;
		this.lng = lng;
	}
	
	public Point(double lat, double lng, long timestamp) {
		super();
		this.lat = lat;
		this.lng = lng;
		this.timestamp = timestamp;
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

	public int getHour() {
		return Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDateTime().getHour();
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

	@Override
	public Trajectory getTrajectory() {
		return trajectory;
	}

	@Override
	public void setTrajectory(Trajectory trajectory) {
		this.trajectory = trajectory;
	}
	
}
