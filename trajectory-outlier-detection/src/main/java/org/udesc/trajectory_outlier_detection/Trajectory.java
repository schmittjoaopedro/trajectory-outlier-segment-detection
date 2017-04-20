package org.udesc.trajectory_outlier_detection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class Trajectory implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String name;
	
	private List<Point> points;
	
	private Point[] binaryPointsLat;
	
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
		this.initialize(false);
	}
	
	public void initialize(boolean removeOuliers) {
		Collections.sort(this.getPoints(), new Comparator<Point>() {
			public int compare(Point o1, Point o2) {
				return (int) (o2.getTimestamp() - o1.getTimestamp());
			}
		});
		if(removeOuliers) {
			removeOutliersGaussianDistribution();
		}
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
		
		if(middle != 0) {
			int min = middle, max = middle;
			double dist = 0;
			while(dist < distance && min >= 0) {
				dist = Math.abs(this.binaryPointsLat[min].getLat() - p.getLat());
				if(dist <= distance) {
					if(this.binaryPointsLat[min].calculateDistance(p) <= distance)
						return true; 
				}
				min--;
			}
			dist = 0;
			while(dist < distance && max < this.binaryPointsLat.length) {
				dist = Math.abs(this.binaryPointsLat[max].getLat() - p.getLat());
				if(dist <= distance) {
					if(this.binaryPointsLat[max].calculateDistance(p) <= distance)
						return true; 
				}
				max++;
			}
		}
		
		return false;
	}
	
	public boolean isInStandardPath(Trajectory t, Point p, double distance, double degree) {
		int low = 0;
		int middle = 0;
		int high = this.binaryPointsLat.length - 1;
		Point point = null;
		
		while(high >= low) {
			middle = (low + high) / 2;
			if(this.binaryPointsLat[middle].calculateDistance(p) <= distance) {
				point = this.binaryPointsLat[middle];
				break;
			}
			if(this.binaryPointsLat[middle].getLat() < p.getLat()) {
				low = middle + 1;
			} else {
				high = middle - 1;
			}
		}
		if(middle != 0) {
			int min = middle, max = middle;
			double dist = 0;
			while(dist < distance && point == null && min >= 0) {
				dist = Math.abs(this.binaryPointsLat[min].getLat() - p.getLat());
				if(dist <= distance) {
					if(this.binaryPointsLat[min].calculateDistance(p) <= distance)
						point = this.binaryPointsLat[min]; 
				}
				min--;
			}
			if(point == null) {
				dist = 0;
				while(dist < distance && point == null && max < this.binaryPointsLat.length) {
					dist = Math.abs(this.binaryPointsLat[max].getLat() - p.getLat());
					if(dist <= distance) {
						if(this.binaryPointsLat[max].calculateDistance(p) <= distance)
							point = this.binaryPointsLat[max]; 
					}
					max++;
				}
			}
		}
		if(point != null) {		
			int idx = this.getPoints().indexOf(point);
			int pIdx = t.getPoints().indexOf(p);
			if(idx < this.getPoints().size() - 1 && pIdx < t.getPoints().size() - 1) {
				Point next = this.getPoints().get(idx + 1);
				Point tNext = t.getPoints().get(pIdx + 1);
				double diff = Math.abs(
						Math.toDegrees(Math.atan2(next.getLat() - point.getLat(), next.getLng() - point.getLng())) - 
						Math.toDegrees(Math.atan2(tNext.getLat() - p.getLat(), tNext.getLng() - p.getLng())));
				if(diff > degree) {
					return false;
				}
			}
			return true;
		} else {
			return false;
		}
	}
	
	public boolean sequentialSearch(Point p, double distance) {
		for(Point p1 : this.getPoints()) {
			if(p1.calculateDistance(p) <= distance) {
				return true;
			}
		}
		return false;
	}
	
	public void removeOutliersGaussianDistribution() {
		if(this.getPoints().size() > 1) {
			double[] pointDistance = new double[this.getPoints().size()];
			pointDistance[0] = this.getPoints().get(0).calculateDistance(this.getPoints().get(1));
			DescriptiveStatistics stats = new DescriptiveStatistics();
			for(int i =  1; i < this.getPoints().size(); i++) {
				pointDistance[i] = this.getPoints().get(i - 1).calculateDistance(this.getPoints().get(i));
				stats.addValue(pointDistance[i]);
			}
			if(stats.getStandardDeviation() > 0) {
				NormalDistribution normal = new NormalDistribution(stats.getMean(), stats.getStandardDeviation());
				List<Integer> indexToRemove = new ArrayList<Integer>();
				for(int i = 0; i < pointDistance.length; i++) {
					double prob = normal.cumulativeProbability(pointDistance[i]);
					if(prob > 0.99865 || prob < 0.0135) { //3 sigmas
						indexToRemove.add(i);
					}
				}
				for(int i = indexToRemove.size() - 1; i >= 0; i--) {
					this.getPoints().remove(this.getPoints().get(indexToRemove.get(i)));
				}
			}
		}
	}
	
	public void interpolate(double distance) {
		int pointSize = this.getPoints().size();
		for(int i = 0; i < pointSize - 1; i++) {
			Point prev = this.getPoints().get(i);
			Point next = this.getPoints().get(i + 1);
			double pointDist = prev.calculateDistance(next);
			if(pointDist >= distance) {
				double latRange = next.getLat() - prev.getLat();
				double lngRange = next.getLng() - prev.getLng();
				long timeRange = next.getTimestamp() - prev.getTimestamp();
				double qtde = (int) ((pointDist / distance) + 1);
				for(int n = 1; n < qtde; n++) {
					Point p = new Point();
					p.setHour(prev.getHour());
					p.setMinutes(prev.getMinutes());
					p.setLat(prev.getLat() + ((latRange / qtde) * n));
					p.setLng(prev.getLng() + ((lngRange / qtde) * n));
					p.setTimestamp(prev.getTimestamp() + ((long) ((timeRange / qtde) * n)));
					this.getPoints().add(i + n, p);
				}
				pointSize = this.getPoints().size();
			}
		}
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
