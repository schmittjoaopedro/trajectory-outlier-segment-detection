package org.udesc.trajectory.TRASOD;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class TRASODRequest implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String country;
	
	private String state;
	
	private String city;
	
	private Grid startGrid;
	
	private Grid endGrid;
	
	private double maxDist;
	
	private double minSup;
	
	public TRASODRequest() {
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

	public double getMaxDist() {
		return maxDist;
	}

	public void setMaxDist(double maxDist) {
		this.maxDist = maxDist;
	}

	public double getMinSup() {
		return minSup;
	}

	public void setMinSup(double minSup) {
		this.minSup = minSup;
	}
	
}
