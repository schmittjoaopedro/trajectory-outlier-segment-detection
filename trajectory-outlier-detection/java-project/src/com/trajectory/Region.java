package com.trajectory;

import java.util.ArrayList;
import java.util.List;

public class Region {

	private String regionName;
	
	private Point centerRegion;
	
	private Double radius;
	
	public Region() {
		super();
	}
	
	public Region(String regionName, double lat, double lng, double radius) {
		Point point = new Point();
		point.setLat(lat);
		point.setLng(lng);
		this.setRadius(radius);
		this.setCenterRegion(point);
		this.setRegionName(regionName);
	}

	public String getRegionName() {
		return regionName;
	}

	public void setRegionName(String regionName) {
		this.regionName = regionName;
	}

	public Point getCenterRegion() {
		return centerRegion;
	}

	public void setCenterRegion(Point centerRegion) {
		this.centerRegion = centerRegion;
	}

	public Double getRadius() {
		return radius;
	}

	public void setRadius(Double radius) {
		this.radius = radius;
	}

	public List<Point> getInnerPoints(Trajectory trajectory) {
		List<Point> points = new ArrayList<>();
		for(int i = 0; i < trajectory.getPoints().size(); i++) {
			double dist = this.getCenterRegion().calcDistance(trajectory.getPoints().get(i));
			if(dist < this.getRadius()) {
				points.add(trajectory.getPoints().get(i));
			}
		}
		return points;
	}
	
	@Override
	public String toString() {
		return "drawCircle(" + this.getCenterRegion().getLat() + "," + 
				this.getCenterRegion().getLng() + "," + 
				this.getRadius() + ");";
	}
}
