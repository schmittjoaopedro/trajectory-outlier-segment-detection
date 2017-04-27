package org.udesc.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.udesc.trajectory.Grid;
import org.udesc.trajectory.Point;
import org.udesc.trajectory.Trajectory;

public class TrajectoryPostGreeBase extends Database {
	
	private static final String TRAJECTORY_SQL = "select " +
			"tt.name,  " +
			"tt.city,  " +
			"tt.state,  " +
			"tt.country,  " +
			"tt.start_hour,  " +
			"tt.start_minute,  " +
			"ST_AsText(tt.trajectory) trajectory " + 
		"from trajectory_application tt  " +
			"inner join trajectory_application t1 on " +
				"ST_Intersects(t1.trajectory, ST_GeometryFromText('SRID=4326;POLYGON((#ST_REGION))')) and " +
				"ST_Intersects(t1.trajectory, ST_GeometryFromText('SRID=4326;POLYGON((#END_REGION))')) and " +
				"tt.id = t1.id " +
		"where  " +
			"tt.country = '#COUNTRY' and " + 
			"tt.state = '#STATE' and  " +
			"tt.city = '#CITY' and " + 
			"tt.start_hour >= #START_HOUR and  " +
			"tt.start_hour <= #END_HOUR ";
	
	public List<Trajectory> findTrajectories(String country, String state, String city, int startHour, int endHour, Grid st, Grid en) throws Exception {
		StringBuilder start = new StringBuilder();
		start.append(st.getLngMin()).append(" ").append(st.getLatMin()).append(",")
		.append(st.getLngMin()).append(" ").append(st.getLatMax()).append(",")
		.append(st.getLngMax()).append(" ").append(st.getLatMax()).append(",")
		.append(st.getLngMax()).append(" ").append(st.getLatMin()).append(",")
		.append(st.getLngMin()).append(" ").append(st.getLatMin());
		
		StringBuilder end = new StringBuilder();
		end.append(en.getLngMin()).append(" ").append(en.getLatMin()).append(",")
		.append(en.getLngMin()).append(" ").append(en.getLatMax()).append(",")
		.append(en.getLngMax()).append(" ").append(en.getLatMax()).append(",")
		.append(en.getLngMax()).append(" ").append(en.getLatMin()).append(",")
		.append(en.getLngMin()).append(" ").append(en.getLatMin());
		
		String sql = TRAJECTORY_SQL
				.replace("#COUNTRY", country)
				.replace("#STATE", state)
				.replace("#CITY", city)
				.replace("#START_HOUR", String.valueOf(startHour))
				.replace("#END_HOUR", String.valueOf(endHour))
				.replace("#ST_REGION", start.toString())
				.replace("#END_REGION", end.toString());			
		List<Trajectory> trajectoriesFound = new ArrayList<Trajectory>();
		Connection conn = this.getConn();
		Statement stmt = conn.createStatement();
		stmt.setFetchSize(1000);
		ResultSet rs = stmt.executeQuery(sql.toString());
		while(rs.next()) {
			Trajectory trajectory = new Trajectory();
			trajectory.setName(rs.getString("name"));
			trajectory.setCity(rs.getString("city"));
			trajectory.setState(rs.getString("state"));
			trajectory.setCountry(rs.getString("country"));
			trajectory.setStartHour(rs.getInt("start_hour"));
			trajectory.setStartMinute(rs.getInt("start_minute"));
			String trajectoryDB = rs.getString("trajectory");
			trajectoryDB = trajectoryDB.substring(trajectoryDB.indexOf("(") + 1, trajectoryDB.indexOf(")"));
			String pointsDB[] = trajectoryDB.split(",");
			for(String points : pointsDB) {
				String pointDB[] = points.split(" ");
				Point point = new Point();
				point.setLng(Double.valueOf(pointDB[0]));
				point.setLat(Double.valueOf(pointDB[1]));
				point.setTimestamp(Long.valueOf(pointDB[2]));
				trajectory.getPoints().add(point);
			}
			trajectory.sortPoints();
			trajectoriesFound.add(trajectory);
		}
		rs.close();
		stmt.close();
		conn.close();
		return trajectoriesFound;
	}
	
}