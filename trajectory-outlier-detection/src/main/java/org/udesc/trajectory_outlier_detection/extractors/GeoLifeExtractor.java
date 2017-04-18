package org.udesc.trajectory_outlier_detection.extractors;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;

import org.udesc.trajectory_outlier_detection.Point;
import org.udesc.trajectory_outlier_detection.Trajectory;

public class GeoLifeExtractor {

	String url = "jdbc:postgresql://localhost:5432/nyc";
	String user = "udesc";
	String password = "udesc";
	
	private Connection getConn() throws SQLException {
		return DriverManager.getConnection(url, user, password);
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
					int lineCount = 0;
					DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
					while ((line = bufferedReader.readLine()) != null) {
						if(lineCount == 6) {
							String data[] = line.split(",");
							Double lat = Double.valueOf(data[0]);
							Double lng = Double.valueOf(data[1]);
							LocalDateTime date = LocalDateTime.parse(data[5] + " " + data[6], format);
							trajectory.getPoints().add(new Point(
									date.getHour(), 
									date.getMinute(), 
									lat, 
									lng, 
									Timestamp.valueOf(date).getTime()));
						} else {
							lineCount++;
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
		String insert = "INSERT INTO trajectory_geolife (name, trajectory) VALUES ('"
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
	
	
	public static void main(String[] args) {
		GeoLifeExtractor geoLife = new GeoLifeExtractor();
		geoLife.loadTrajectories("/home/joao/Área de Trabalho/Mestrado/Extracted/geolife/Geolife Trajectories 1.3/Data/000/Trajectory");
	}
	
}
