package org.udesc.database;

import java.util.List;

public interface ITrajectory<P extends IPoint> {

	String getName();
	
	void setName(String name);
	
	String getCity();
	
	void setCity(String city);
	
	String getState();
	
	void setState(String state);
	
	String getCountry();
	
	void setCountry(String country);
	
	int getStartHour();
	
	void setStartHour(int hour);
	
	int getStartMinute();
	
	void setStartMinute(int minute);
	
	void sortPoints();
	
	List<P> getPoints();
	
	void setPoints(List<P> points);
	
}
