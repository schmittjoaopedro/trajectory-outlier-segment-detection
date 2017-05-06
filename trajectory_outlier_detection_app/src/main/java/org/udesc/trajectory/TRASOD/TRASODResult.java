package org.udesc.trajectory.TRASOD;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class TRASODResult implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int trajectoriesAnalysed;
	
	private List<Trajectory> standards;
	
	private List<Trajectory> notStandards;
	
	private List<Trajectory> rawResult;
	
	private long queryTime;
	
	private long programTime;
	
	public TRASODResult() {
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

	public List<Trajectory> getStandards() {
		if(standards == null) standards = new ArrayList<Trajectory>();
		return standards;
	}

	public void setStandards(List<Trajectory> standards) {
		this.standards = standards;
	}

	public List<Trajectory> getNotStandards() {
		if(notStandards == null) notStandards = new ArrayList<>();
		return notStandards;
	}

	public void setNotStandards(List<Trajectory> notStandards) {
		this.notStandards = notStandards;
	}

	public List<Trajectory> getRawResult() {
		if(rawResult == null) rawResult = new ArrayList<>();
		return rawResult;
	}

	public void setRawResult(List<Trajectory> rawResult) {
		this.rawResult = rawResult;
	}
	
}
