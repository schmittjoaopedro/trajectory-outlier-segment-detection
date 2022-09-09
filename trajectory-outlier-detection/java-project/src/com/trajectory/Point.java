package com.trajectory;

public class Point {

	private double lat;
	
	private double lng;
	
	private double time;
	
	public Point() {
		super();
	}

	public Point(double lat, double lng, double time) {
		super();
		this.lat = lat;
		this.lng = lng;
		this.time = time;
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

	public double getTime() {
		return time;
	}

	public void setTime(double time) {
		this.time = time;
	}
	
	public double calcDistance(Point target) {
		return Math.sqrt(Math.pow(this.getLat() - target.getLat(), 2) + Math.pow(this.getLng() - target.getLng(), 2));
	}
	
}
