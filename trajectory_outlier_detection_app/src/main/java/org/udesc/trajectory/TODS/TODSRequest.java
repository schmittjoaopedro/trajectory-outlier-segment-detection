package org.udesc.trajectory.TODS;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class TODSRequest implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String country;
	
	private String state;
	
	private String city;
	
	private int startHour;
	
	private int endHour;
	
	private Grid startGrid;
	
	private Grid endGrid;
	
	private double distance;
	
	private double angle;
	
	private int kStandard;
	
	private double interpolation;
	
	private double sd;
	
	private double sigma;
	
	public TODSRequest() {
		super();
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public int getStartHour() {
		return startHour;
	}

	public void setStartHour(int startHour) {
		this.startHour = startHour;
	}

	public int getEndHour() {
		return endHour;
	}

	public void setEndHour(int endHour) {
		this.endHour = endHour;
	}

	public Grid getStartGrid() {
		return startGrid;
	}

	public void setStartGrid(Grid startGrid) {
		this.startGrid = startGrid;
	}

	public Grid getEndGrid() {
		return endGrid;
	}

	public void setEndGrid(Grid endGrid) {
		this.endGrid = endGrid;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public int getkStandard() {
		return kStandard;
	}

	public void setkStandard(int kStandard) {
		this.kStandard = kStandard;
	}

	public double getAngle() {
		return angle;
	}

	public void setAngle(double angle) {
		this.angle = angle;
	}

	public double getInterpolation() {
		return interpolation;
	}

	public void setInterpolation(double interpolation) {
		this.interpolation = interpolation;
	}

	public double getSd() {
		return sd;
	}

	public void setSd(double sd) {
		this.sd = sd;
	}

	public double getSigma() {
		return sigma;
	}

	public void setSigma(double sigma) {
		this.sigma = sigma;
	}
	
}
