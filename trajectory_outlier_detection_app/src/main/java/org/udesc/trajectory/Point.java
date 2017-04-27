package org.udesc.trajectory;

import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneId;

public class Point implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private double lat;
	
	private double lng;
	
	private long timestamp;
	
	private boolean standard; 
	
	public Point() {
		super();
	}

	public Point(double lat, double lng, long timestamp, boolean standard) {
		super();
		this.lat = lat;
		this.lng = lng;
		this.timestamp = timestamp;
		this.standard = standard;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public double getLng() {
		return lng;
	}

	public void setLng(double lng) {
		this.lng = lng;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public boolean isStandard() {
		return standard;
	}

	public void setStandard(boolean standard) {
		this.standard = standard;
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
	
}