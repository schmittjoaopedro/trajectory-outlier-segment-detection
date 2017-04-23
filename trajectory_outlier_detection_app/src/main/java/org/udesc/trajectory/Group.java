package org.udesc.trajectory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Group implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private List<Trajectory> trajectories;
	
	public Group() {
		super();
	}

	public List<Trajectory> getTrajectories() {
		if(trajectories == null) trajectories = new ArrayList<Trajectory>();
		return trajectories;
	}

	public void setTrajectories(List<Trajectory> trajectories) {
		this.trajectories = trajectories;
	}
	
}
