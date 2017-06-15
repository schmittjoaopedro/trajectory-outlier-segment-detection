package org.udesc.trajectory.TODS;

import java.io.Serializable;
import java.util.*;

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

    @Override
    public String toString() {
        return drawPolyline(Arrays.asList(
                new Point(this.getLatMin(), this.getLngMin(), null),
                new Point(this.getLatMin(), this.getLngMax(), null),
                new Point(this.getLatMax(), this.getLngMax(), null),
                new Point(this.getLatMax(), this.getLngMin(), null),
                new Point(this.getLatMin(), this.getLngMin(), null)
        ), "FFFFFF");
    }

    public String drawPolyline(List<Point> points, String color) {
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

}
