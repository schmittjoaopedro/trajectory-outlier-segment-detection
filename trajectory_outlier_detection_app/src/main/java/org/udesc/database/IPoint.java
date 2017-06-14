package org.udesc.database;

public interface IPoint<T> {

    int getId();

    void setId(int id);

	double getLng();
	
	void setLng(double lng);
	
	double getLat();
	
	void setLat(double lat);
	
	long getTimestamp();
	
	void setTimestamp(long timestamp);
	
	T getTrajectory();
	
	void setTrajectory(T trajectory);
	
}
