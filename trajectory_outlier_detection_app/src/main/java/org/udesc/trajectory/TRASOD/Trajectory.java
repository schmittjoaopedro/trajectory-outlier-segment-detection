package org.udesc.trajectory.TRASOD;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.udesc.database.ITrajectory;

public class Trajectory implements Serializable, ITrajectory<Point> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int id;

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

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getCity() {
		return city;
	}

	@Override
	public void setCity(String city) {
		this.city = city;
	}

	@Override
	public String getState() {
		return state;
	}

	@Override
	public void setState(String state) {
		this.state = state;
	}

	@Override
	public String getCountry() {
		return country;
	}

	@Override
	public void setCountry(String country) {
		this.country = country;
	}

	@Override
	public List<Point> getPoints() {
		if(this.points == null) this.points = new ArrayList<Point>();
		return points;
	}

	@Override
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
	@Override
	public void sortPoints() {
		Collections.sort(this.getPoints(), new Comparator<Point>() {
			public int compare(Point o1, Point o2) {
				return (int) (o2.getTimestamp() - o1.getTimestamp());
			}
		});
	}
			
}
