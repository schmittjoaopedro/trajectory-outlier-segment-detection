package org.udesc.trajectory_outlier_detection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Trajectory implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String name;
	
	private List<Point> points;
	
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
		if(points == null) points = new ArrayList<Point>();
		return points;
	}

	public void setPoints(List<Point> points) {
		this.points = points;
	}
	
}
