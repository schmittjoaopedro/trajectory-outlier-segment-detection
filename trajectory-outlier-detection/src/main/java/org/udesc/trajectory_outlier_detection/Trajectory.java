package org.udesc.trajectory_outlier_detection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Trajectory implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String name;
	
	private List<Point> points;
	
	private Point[] binaryPointsLat;
	
	private Point[] binaryPointsLng;
	
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
	
	public void initialize() {
		Collections.sort(this.getPoints(), new Comparator<Point>() {
			public int compare(Point o1, Point o2) {
				return (int) (o2.getTimestamp() - o1.getTimestamp());
			}
		});
		List<Point> temp = new ArrayList<Point>();
		temp.addAll(this.getPoints());
		Collections.sort(temp, new Comparator<Point>() {
			public int compare(Point o1, Point o2) {
				double dif = o2.getLat() - o1.getLat();
				if(dif < 0.0) return 1;
				else if(dif > 0.0) return -1;
				else return 0;
			}
		});
		this.binaryPointsLat = new Point[temp.size()];
		this.binaryPointsLat = temp.toArray(new Point[] {});
		Collections.sort(temp, new Comparator<Point>() {
			public int compare(Point o1, Point o2) {
				double dif = o2.getLng() - o1.getLng();
				if(dif < 0.0) return 1;
				else if(dif > 0.0) return -1;
				else return 0;
			}
		});
		this.binaryPointsLng = new Point[temp.size()];
		this.binaryPointsLng = temp.toArray(new Point[] {});
	}
	
	public boolean binarySearch(Point p, double distance) {
		int low = 0;
		int middle = 0;
		int high = this.binaryPointsLat.length - 1;
		while(high >= low) {
			middle = (low + high) / 2;
			if(this.binaryPointsLat[middle].calculateDistance(p) <= distance) {
				return true;
			}
			if(this.binaryPointsLat[middle].getLat() < p.getLat()) {
				low = middle + 1;
			} else {
				high = middle - 1;
			}
		}
		
		low = 0;
		middle = 0;
		high = this.binaryPointsLng.length - 1;
		while(high >= low) {
			middle = (low + high) / 2;
			if(this.binaryPointsLng[middle].calculateDistance(p) <= distance) {
				return true;
			}
			if(this.binaryPointsLng[middle].getLng() < p.getLng()) {
				low = middle + 1;
			} else {
				high = middle - 1;
			}
		}
		
		return false;
	}
	
	public boolean sequentialSearch(Point p, double distance) {
		for(Point p1 : this.getPoints()) {
			if(p1.calculateDistance(p) <= distance) {
				return true;
			}
		}
		return false;
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
