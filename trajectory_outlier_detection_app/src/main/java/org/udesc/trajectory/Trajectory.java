package org.udesc.trajectory;

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
	
	private String city;
	
	private String state;
	
	private String country;
	
	private int startHour;
	
	private int startMinute;
	
	private List<Point> points;
	
	private Point[] pointsLat = null;
	
	public Trajectory() {
		super();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public List<Point> getPoints() {
		if(this.points == null) this.points = new ArrayList<Point>();
		return points;
	}

	public void setPoints(List<Point> points) {
		this.points = points;
	}

	public Point[] getPointsLat() {
		return pointsLat;
	}

	public void setPointsLat(Point[] pointsLat) {
		this.pointsLat = pointsLat;
	}

	public int getStartHour() {
		return startHour;
	}

	public void setStartHour(int startHour) {
		this.startHour = startHour;
	}

	public int getStartMinute() {
		return startMinute;
	}

	public void setStartMinute(int startMinute) {
		this.startMinute = startMinute;
	}
	
	/**
	 * Sort points by timestamp
	 */
	public void sortPoints() {
		Collections.sort(this.getPoints(), new Comparator<Point>() {
			public int compare(Point o1, Point o2) {
				return (int) (o2.getTimestamp() - o1.getTimestamp());
			}
		});
	}
	
	/**
	 * Remove points with a distribution out of the Sigma thresholds and
	 * if the standard deviation of the coordinates is greater than sd
	 * then remove all points of the trajectory.
	 * 
	 * @param sigma
	 * @param sd
	 */
	public void filterNoise(double sigma, double sd) {
		if(this.getPoints().size() > 1) {
			double[] pointDistance = new double[this.getPoints().size()];
			pointDistance[0] = this.getPoints().get(0).calculateDistance(this.getPoints().get(1));
			DescriptiveStatistics stats = new DescriptiveStatistics();
			for(int i =  1; i < this.getPoints().size(); i++) {
				pointDistance[i] = this.getPoints().get(i - 1).calculateDistance(this.getPoints().get(i));
				stats.addValue(pointDistance[i]);
			}
			if(stats.getStandardDeviation() > 0 && stats.getStandardDeviation() < sd) {
				NormalDistribution normal = new NormalDistribution(stats.getMean(), stats.getStandardDeviation());
				List<Integer> indexToRemove = new ArrayList<Integer>();
				for(int i = 0; i < pointDistance.length; i++) {
					double prob = normal.cumulativeProbability(pointDistance[i]);
					if(prob > (1 - sigma) || prob < sigma) {
						indexToRemove.add(i);
					}
				}
				for(int i = indexToRemove.size() - 1; i >= 0; i--) {
					this.getPoints().remove(this.getPoints().get(indexToRemove.get(i)));
				}
			} else {
				this.getPoints().clear();
			}
		}
	}
	
	/**
	 * Interpolate points if the points difference in sequence is greater than distance
	 * 
	 * @param distance
	 */
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
					p.setLat(prev.getLat() + ((latRange / qtde) * n));
					p.setLng(prev.getLng() + ((lngRange / qtde) * n));
					p.setTimestamp(prev.getTimestamp() + ((long) ((timeRange / qtde) * n)));
					this.getPoints().add(i + n, p);
				}
				pointSize = this.getPoints().size();
			}
		}
	}
	
	/**
	 * Execute sequential search to find the first near point for a given point p
	 * 
	 * @param p
	 * @param distance
	 * 
	 * @return foundPoint
	 */
	public boolean sequentialSearch(Point p, double distance) {
		for(Point p1 : this.getPoints()) {
			if(p1.calculateDistance(p) <= distance) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Execute binary search to find the first near point for a given point p
	 * 
	 * @param p
	 * @param distance
	 * 
	 * @return foundPoint
	 */
	public Point binarySearch(Point p, double distance) {
		if(pointsLat == null || this.getPoints().size() != pointsLat.length) {
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
			this.pointsLat = new Point[temp.size()];
			this.pointsLat = temp.toArray(new Point[] {});
		}
		int low = 0;
		int middle = 0;
		int high = this.pointsLat.length - 1;
		while(high >= low) {
			middle = (low + high) / 2;
			if(this.pointsLat[middle].calculateDistance(p) <= distance) {
				return this.pointsLat[middle];
			}
			if(this.pointsLat[middle].getLat() < p.getLat()) {
				low = middle + 1;
			} else {
				high = middle - 1;
			}
		}
		if(middle >= 0) {
			int min = middle, max = middle;
			double dist = 0;
			while(dist < distance && min >= 0) {
				dist = Math.abs(this.pointsLat[min].getLat() - p.getLat());
				if(dist <= distance && this.pointsLat[min].calculateDistance(p) <= distance) {
					return this.pointsLat[min];
				}
				min--;
			}
			dist = 0;
			while(dist < distance && max < this.pointsLat.length) {
				dist = Math.abs(this.pointsLat[max].getLat() - p.getLat());
				if(dist <= distance && this.pointsLat[max].calculateDistance(p) <= distance) {
					return this.pointsLat[max]; 
				}
				max++;
			}
		}
		return null;
	}
	

	/**
	 * Return if the angle size is less than a threshold
	 * 
	 * @param p1Out
	 * @param p2Out
	 * @param distance
	 * @param angle
	 * 
	 * @return angleIsLower
	 */
	public boolean isLessAngleDifference(Point p1Out, Point p2Out, double distance, double angle) {
		Point p1In = this.binarySearch(p1Out, distance);
		Point p2In = this.binarySearch(p2Out, distance);
		if(p1In == p2In) {
			return true;
		} else if(p1In != null && p2In != null) {
			double a1 = Math.toDegrees(Math.atan2(p1Out.getLat() - p2Out.getLat(), p1Out.getLng() - p2Out.getLng()));
			double a2 = Math.toDegrees(Math.atan2(p1In.getLat() - p2In.getLat(), p1In.getLng() - p2In.getLng()));
			if(a1  < 0) a1 = 180 + a1;
			if(a2  < 0) a2 = 180 + a2;			
			double diff = Math.abs(a1 - a2);
			if(diff > 90) diff = 180 - 90;
			return diff <= angle;
		} else if((p1In == null && p2In != null) || (p1In != null && p2In == null)) {
			return false;
		} else {
			return true;
		}
	}
		
}
