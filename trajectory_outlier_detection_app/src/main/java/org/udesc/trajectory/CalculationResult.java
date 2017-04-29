package org.udesc.trajectory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class CalculationResult implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int trajectoriesAnalysed;
	
	private List<Group> standards;
	
	private List<Group> notStandards;
	
	private long queryTime;
	
	private long programTime;
	
	public CalculationResult() {
		super();
	}
	
	public long getQueryTime() {
		return queryTime;
	}

	public void setQueryTime(long queryTime) {
		this.queryTime = queryTime;
	}

	public long getProgramTime() {
		return programTime;
	}

	public void setProgramTime(long programTime) {
		this.programTime = programTime;
	}

	public int getTrajectoriesAnalysed() {
		return trajectoriesAnalysed;
	}

	public void setTrajectoriesAnalysed(int trajectoriesAnalysed) {
		this.trajectoriesAnalysed = trajectoriesAnalysed;
	}

	public List<Group> getStandards() {
		if(standards == null) standards = new ArrayList<Group>();
		return standards;
	}

	public void setStandards(List<Group> standards) {
		this.standards = standards;
	}

	public List<Group> getNotStandards() {
		if(notStandards == null) notStandards = new ArrayList<>();
		return notStandards;
	}

	public void setNotStandards(List<Group> notStandards) {
		this.notStandards = notStandards;
	}
	
}
