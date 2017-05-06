package org.udesc.trajectory.TODS;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Route implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private List<Trajectory> standards;
	
	private List<Trajectory> notStandards;
	
	public Route() {
		super();
	}

	public List<Trajectory> getStandards() {
		if(standards == null) standards = new ArrayList<Trajectory>();
		return standards;
	}

	public void setStandards(List<Trajectory> standards) {
		this.standards = standards;
	}

	public List<Trajectory> getNotStandards() {
		if(notStandards == null) notStandards = new ArrayList<Trajectory>();
		return notStandards;
	}

	public void setNotStandards(List<Trajectory> notStandards) {
		this.notStandards = notStandards;
	}
	
}
