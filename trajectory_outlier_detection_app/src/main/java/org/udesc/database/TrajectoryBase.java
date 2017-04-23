package org.udesc.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.udesc.trajectory.Grid;
import org.udesc.trajectory.Point;
import org.udesc.trajectory.Trajectory;

public class TrajectoryBase extends Database {
	
	private static final String TABLE_NAME = "trajectory_application";
	
	public List<Trajectory> findTrajectories(String country, String state, String city, int startHour, int endHour, Grid st, Grid en) throws Exception {
		StringBuilder sql = new StringBuilder();
			sql.append("select tt.name, tt.city, tt.state, tt.country, tt.start_hour, tt.start_minute, ST_AsText(tt.trajectory) trajectory from ").append(TABLE_NAME).append(" tt ")
				.append("inner join ( ")
						.append("select ")
						.append("id, ")
						.append("name, ")
						.append("ST_Intersects(")
							.append("ST_GeometryFromText('SRID=4326;POLYGON((")
									.append(st.getLngMin()).append(" ").append(st.getLatMin()).append(",")
									.append(st.getLngMin()).append(" ").append(st.getLatMax()).append(",")
									.append(st.getLngMax()).append(" ").append(st.getLatMax()).append(",")
									.append(st.getLngMax()).append(" ").append(st.getLatMin()).append(",")
									.append(st.getLngMin()).append(" ").append(st.getLatMin())
							.append("))'), ")
							.append("trajectory ")
						.append(") as st ")
						.append("from ").append(TABLE_NAME).append(" ")
					.append(") start_db on start_db.id = tt.id ")
					.append("inner join ( ")
						.append("select ")
						.append("id, ")
						.append("name, ")
						.append("ST_Intersects(")
							.append("ST_GeometryFromText('SRID=4326;POLYGON((")
									.append(en.getLngMin()).append(" ").append(en.getLatMin()).append(",")
									.append(en.getLngMin()).append(" ").append(en.getLatMax()).append(",")
									.append(en.getLngMax()).append(" ").append(en.getLatMax()).append(",")
									.append(en.getLngMax()).append(" ").append(en.getLatMin()).append(",")
									.append(en.getLngMin()).append(" ").append(en.getLatMin())
							.append("))'), ")
							.append("trajectory ")
						.append(") as en ")
						.append("from ").append(TABLE_NAME).append(" ")
					.append(") end_db on end_db.id = tt.id ")
				.append("where start_db.st = true and end_db.en = true and ")
				.append("tt.country = '").append(country).append("' and ")
				.append("tt.state = '").append(state).append("' and ")
				.append("tt.city = '").append(city).append("' and ")
				.append("tt.start_hour >= ").append(startHour).append(" and tt.start_hour <= ").append(endHour);
			
		List<Trajectory> trajectoriesFound = new ArrayList<Trajectory>();
		Connection conn = this.getConn();
		Statement stmt = conn.createStatement();
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