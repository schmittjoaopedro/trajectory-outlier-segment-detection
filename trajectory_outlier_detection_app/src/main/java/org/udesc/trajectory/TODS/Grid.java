package org.udesc.trajectory.TODS;

import java.io.Serializable;

import org.udesc.database.IGrid;

public class Grid implements Serializable, IGrid {

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

	@Override
	public double getLatMin() {
		return latMin;
	}

	@Override
	public void setLatMin(double latMin) {
		this.latMin = latMin;
	}

	@Override
	public double getLatMax() {
		return latMax;
	}

	@Override
	public void setLatMax(double latMax) {
		this.latMax = latMax;
	}

	@Override
	public double getLngMin() {
		return lngMin;
	}

	@Override
	public void setLngMin(double lngMin) {
		this.lngMin = lngMin;
	}

	@Override
	public double getLngMax() {
		return lngMax;
	}

	@Override
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
