package org.udesc.trajectory_outlier_detection;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostgreSQL {

	String url = "jdbc:postgresql://localhost:5432/nyc";
	String user = "udesc";
	String password = "udesc";
	String tableName;
	
	private Map<String, Trajectory> buffer = new HashMap<String, Trajectory>();
	
	public PostgreSQL(String table) {
		this.tableName = table;
	}
	
	public void loadTrajectories(String path) {
		try {
			File f = new File(path);
			ArrayList<String> names = new ArrayList<String>(Arrays.asList(f.list()));
			for (int i = 0; i < names.size(); i++) {
				try {
					Trajectory trajectory = new Trajectory();
					trajectory.setName(names.get(i));
					File file = new File(path + "/" + names.get(i));
					FileReader fileReader = new FileReader(file);
					BufferedReader bufferedReader = new BufferedReader(fileReader);
					String line;
					int indexLat = -1, indexLng = -1, indexHour = -1, indexMinute = -1, indexTime = -1;
					boolean fileHeaderProcessed = false;
					while ((line = bufferedReader.readLine()) != null) {
						if (!fileHeaderProcessed && line.length() > 0) {
							String lineS[] = line.split(";");
							for(int k = 0; k < lineS.length; k++) {
								if("latitude".equals(lineS[k].toLowerCase()))
									indexLat = k;
								if("longitude".equals(lineS[k].toLowerCase()))
									indexLng = k;
								if("hour".equals(lineS[k].toLowerCase()))
									indexHour = k;
								if("minute".equals(lineS[k].toLowerCase()))
									indexMinute = k;
								if("time_since_start_in_ms".equals(lineS[k].toLowerCase()))
									indexTime = k;
							}
							fileHeaderProcessed = true;
						} else if (fileHeaderProcessed && line.length() > 0) {
							String[] lineS = line.split(";");
							trajectory.getPoints().add(new Point(
									Integer.valueOf(lineS[indexHour]),
									Integer.valueOf(lineS[indexMinute]), 
									Double.valueOf(lineS[indexLat]),
									Double.valueOf(lineS[indexLng]),
									Long.valueOf(lineS[indexTime])));
						}
					}
					trajectory.initialize();
					this.loadTrajectory(trajectory);
					bufferedReader.close();
					if(i % 10 == 0) {
						System.out.println("Load: " + i + " from: " + names.size());
					}
				} catch (Exception e) {
//					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void loadTrajectory(Trajectory t) throws Exception {
		StringBuilder ts = new StringBuilder();
		for(int i = 0; i < t.getPoints().size(); i++) {
			if(i == t.getPoints().size() - 1) {
				ts.append(t.getPoints().get(i).getLng()).append(" ").append(t.getPoints().get(i).getLat()).append(" ").append(t.getPoints().get(i).getTimestamp());
			} else {
				ts.append(t.getPoints().get(i).getLng()).append(" ").append(t.getPoints().get(i).getLat()).append(" ").append(t.getPoints().get(i).getTimestamp()).append(",");
			}
		}
		String insert = "INSERT INTO " + tableName + " (name, trajectory) VALUES ('"
				+ t.getName() + "', ST_GeomFromEWKT('SRID=4326;MULTIPOINTM("
						+ ts.toString() + ")'));";
		
		Connection conn = this.getConn();
		conn.setAutoCommit(false);
		Statement statement = conn.createStatement();
		statement.executeUpdate(insert);
		statement.close();
		conn.commit();
		conn.close();
	}
	
	public List<Trajectory> findTrajectories(Grid st, Grid en) throws Exception {
		StringBuilder sql = new StringBuilder();
			sql.append("select tt.name, ST_AsText(tt.trajectory) trajectory from").append(tableName).append(" tt ")
				.append("inner join ( ")
						.append("select ")
						.append("id, ")
						.append("name, ")
						.append("ST_Intersects(")
							.append("ST_GeometryFromText('SRID=4326;POLYGON((")
									.append(st.getLatMin()).append(" ").append(st.getLngMin()).append(",")
									.append(st.getLatMin()).append(" ").append(st.getLngMax()).append(",")
									.append(st.getLatMax()).append(" ").append(st.getLngMax()).append(",")
									.append(st.getLatMax()).append(" ").append(st.getLngMin()).append(",")
									.append(st.getLatMin()).append(" ").append(st.getLngMin())
							.append("))'), ")
							.append("trajectory ")
						.append(") as st ")
						.append("from ").append(tableName).append(" ")
					.append(") start_db on start_db.id = tt.id ")
					.append("inner join ( ")
						.append("select ")
						.append("id, ")
						.append("name, ")
						.append("ST_Intersects(")
							.append("ST_GeometryFromText('SRID=4326;POLYGON((")
									.append(en.getLatMin()).append(" ").append(en.getLngMin()).append(",")
									.append(en.getLatMin()).append(" ").append(en.getLngMax()).append(",")
									.append(en.getLatMax()).append(" ").append(en.getLngMax()).append(",")
									.append(en.getLatMax()).append(" ").append(en.getLngMin()).append(",")
									.append(en.getLatMin()).append(" ").append(en.getLngMin())
							.append("))'), ")
							.append("trajectory ")
						.append(") as en ")
						.append("from ").append(tableName).append(" ")
					.append(") end_db on end_db.id = tt.id ")
				.append("where start_db.st = true and end_db.en = true");
			
		List<Trajectory> trajectoriesFound = new ArrayList<Trajectory>();
		Connection conn = this.getConn();
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(sql.toString());
		while(rs.next()) {
			String name = rs.getString("name");
			if(buffer.containsKey(name)) {
				trajectoriesFound.add(buffer.get(name));
			} else {
				String trajectoryDB = rs.getString("trajectory");
				trajectoryDB = trajectoryDB.substring(trajectoryDB.indexOf("(") + 1, trajectoryDB.indexOf(")"));
				String pointsDB[] = trajectoryDB.split(",");
				Trajectory trajectory = new Trajectory();
				trajectory.setName(name);
				for(String points : pointsDB) {
					String pointDB[] = points.split(" ");
					Point point = new Point();
					point.setLng(Double.valueOf(pointDB[0]));
					point.setLat(Double.valueOf(pointDB[1]));
					point.setTimestamp(Long.valueOf(pointDB[2]));
					trajectory.getPoints().add(point);
				}
				trajectory.initialize(true);
				buffer.put(name, trajectory);
				trajectoriesFound.add(trajectory);
			}
		}
		rs.close();
		stmt.close();
		conn.close();
		return trajectoriesFound;
	}
	
	public List<Trajectory> loadAllTrajectories() throws SQLException {
		List<Trajectory> trajectories = new ArrayList<Trajectory>();
		StringBuilder sql = new StringBuilder("select name, ST_AsText(trajectory) as trajectory from ").append(tableName).append(" ");
		Connection conn = this.getConn();
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(sql.toString());
		while(rs.next()) {
			String name = rs.getString("name");
			String trajectoryDB = rs.getString("trajectory");
			trajectoryDB = trajectoryDB.substring(trajectoryDB.indexOf("(") + 1, trajectoryDB.indexOf(")"));
			String pointsDB[] = trajectoryDB.split(",");
			Trajectory trajectory = new Trajectory();
			trajectory.setName(name);
			for(String points : pointsDB) {
				String pointDB[] = points.split(" ");
				Point point = new Point();
				point.setLng(Double.valueOf(pointDB[0]));
				point.setLat(Double.valueOf(pointDB[1]));
				point.setTimestamp(Long.valueOf(pointDB[2]));
				trajectory.getPoints().add(point);
			}
			trajectory.initialize(true);
			trajectories.add(trajectory);
		}
		rs.close();
		stmt.close();
		conn.close();
		return trajectories;
	}
	
	private Connection getConn() throws SQLException {
		return DriverManager.getConnection(url, user, password);
	}
	
	public static void main(String[] args) {
		PostgreSQL postgreSQL = new PostgreSQL("trajectory_test");
		postgreSQL.loadTrajectories("/home/joao/Área de Trabalho/Mestrado/Extracted/tidy/ALL");
	}
	
}
