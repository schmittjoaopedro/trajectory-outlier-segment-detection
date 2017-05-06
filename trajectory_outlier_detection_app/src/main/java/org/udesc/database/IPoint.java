package org.udesc.database;

public interface IPoint {

	double getLng();
	
	void setLng(double lng);
	
	double getLat();
	
	void setLat(double lat);
	
	long getTimestamp();
	
	void setTimestamp(long timestamp);
	
}
