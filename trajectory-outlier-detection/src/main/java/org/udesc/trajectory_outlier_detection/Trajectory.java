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
	
	public String toStringDefault() {
		String STstr = "drawPoints([";
		for(int i = 0; i < this.getPoints().size(); i++) {
			if(i < this.getPoints().size() - 1) {
				STstr += "{lat: " + this.getPoints().get(i).getLat() + ",lng:" + this.getPoints().get(i).getLng() + "},";
			} else {
				STstr += "{lat: " + this.getPoints().get(i).getLat() + ",lng:" + this.getPoints().get(i).getLng() + "}";
			}
		}
		STstr += "], '#FF0000');";
		return STstr;
	}
	
	public String toStringOutlier() {
		String STstr = "drawPoints([";
		for(int i = 0; i < this.getPoints().size(); i++) {
			if(i < this.getPoints().size() - 1) {
				STstr += "{lat: " + this.getPoints().get(i).getLat() + ",lng:" + this.getPoints().get(i).getLng() + "},";
			} else {
				STstr += "{lat: " + this.getPoints().get(i).getLat() + ",lng:" + this.getPoints().get(i).getLng() + "}";
			}
		}
		STstr += "], '#0000FF');";
		return STstr;
	}
	
}
