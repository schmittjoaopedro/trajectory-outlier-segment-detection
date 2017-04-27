package org.udesc.trajectory;

import java.io.Serializable;

public class Grid implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private double latMin;
	
	private double latMax;
	
	private double lngMin;
	
	private double lngMax;
	
	public Grid() {
		super();
	}

	public double getLatMin() {
		return latMin;
	}

	public void setLatMin(double latMin) {
		this.latMin = latMin;
	}

	public double getLatMax() {
		return latMax;
	}

	public void setLatMax(double latMax) {
		this.latMax = latMax;
	}

	public double getLngMin() {
		return lngMin;
	}

	public void setLngMin(double lngMin) {
		this.lngMin = lngMin;
	}

	public double getLngMax() {
		return lngMax;
	}

	public void setLngMax(double lngMax) {
		this.lngMax = lngMax;
	}
	
	public boolean betweenLat(Point p) {
		return this.getLatMin() <= p.getLat() && p.getLat() <= this.getLatMax();
	}
	
	public boolean betweenLng(Point p) {
		return this.getLngMin() <= p.getLng() && p.getLng() <= this.getLngMax();
	}
	
}