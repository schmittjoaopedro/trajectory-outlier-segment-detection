package com.trajectory;

import java.util.ArrayList;
import java.util.List;

public class Trajectory {

	private String name;
	
	private List<Point> points;
	
	private boolean standard = false;
	
	public Trajectory() {
		super();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Point> getPoints() {
		if(points == null) points = new ArrayList<>();
		return points;
	}

	public void setPoints(List<Point> points) {
		this.points = points;
	}
	
	public boolean isStandard() {
		return standard;
	}

	public void setStandard(boolean standard) {
		this.standard = standard;
	}

	@Override
	public String toString() {
		String output = "drawPoints([";
		for(int i = 0; i < this.getPoints().size(); i++) {
			output += "{ lat: " + this.getPoints().get(i).getLat() + ", lng: " + this.getPoints().get(i).getLng();
			if(i != this.getPoints().size() - 1) {
				output += " },";
			} else {
				output += " }";
			}
		}
		output += "],'";
		if(this.isStandard()) {
			output += "#AA0000";
		} else {
			output += "#00AA00";
		}
		output += "');";
		return output;
	}
	
}
