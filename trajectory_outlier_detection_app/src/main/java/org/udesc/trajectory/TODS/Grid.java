package org.udesc.trajectory.TODS;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
	
	private Set<Point> points;

	
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

	public Set<Point> getPoints() {
		if(points == null) points = new HashSet<>();
		return points;
	}

	public void setPoints(Set<Point> points) {
		this.points = points;
	}
	
	public Set<Group> findGroups(Point pX, Double distance) {
		Set<Group> groups = new HashSet<>();
		for(Point pY : this.getPoints()) {
			if( !pX.getTrajectory().equals(pY.getTrajectory()) &&
				pY.getTrajectory().getGroup() != null &&
				pY.calculateDistance(pX) <= distance) {
				groups.add(pY.getTrajectory().getGroup());
			}
		}
		return groups;
	}

}
