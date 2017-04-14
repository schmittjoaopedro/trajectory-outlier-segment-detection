package org.udesc.trajectory_outlier_detection;

import java.io.Serializable;

public class Point implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int hour;
	
	private int minutes;
	
	private double lat;
	
	private double lng;
	
	private long timestamp;
	
	public Point() {
		super();
	}
	
	public Point(int hour, int minutes, double lat, double lng, long timestamp) {
		super();
		this.hour = hour;
		this.minutes = minutes;
		this.lat = lat;
		this.lng = lng;
		this.timestamp = timestamp;
	}

	public int getHour() {
		return hour;
	}

	public void setHour(int hour) {
		this.hour = hour;
	}

	public int getMinutes() {
		return minutes;
	}

	public void setMinutes(int minutes) {
		this.minutes = minutes;
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
	
	public double calculateDistance(Point p) {
		return Math.sqrt(Math.pow(this.getLat() - p.getLat(), 2) + Math.pow(this.getLng() - p.getLng(), 2));
	}
	
}
