package org.udesc.trajectory_outlier_detection.webapp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.udesc.trajectory_outlier_detection.Point;
import org.udesc.trajectory_outlier_detection.Trajectory;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

public class JoinvilleExtractor {

	String url = "jdbc:postgresql://localhost:5432/nyc";
	String user = "udesc";
	String password = "udesc";
	String tableName = "trajectory_application";
	
	MongoClient mongoClient;
	
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
							int size = lineS.length - 1;
							LocalDateTime date = LocalDateTime.of(
									Integer.valueOf(lineS[size - 7]), 
									Integer.valueOf(lineS[size - 6]), 
									Integer.valueOf(lineS[size - 5]), 
									Integer.valueOf(lineS[size - 4]), 
									Integer.valueOf(lineS[size - 3]), 
									Integer.valueOf(lineS[size - 2]), 
									Integer.valueOf(lineS[size - 1]));
							trajectory.getPoints().add(new Point(
									Integer.valueOf(lineS[indexHour]),
									Integer.valueOf(lineS[indexMinute]), 
									Double.valueOf(lineS[indexLat]),
									Double.valueOf(lineS[indexLng]),
									Timestamp.valueOf(date).getTime()));
						}
					}
					trajectory.initialize();
					this.loadTrajectoryMongo(trajectory);
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
		String insert = "INSERT INTO " + tableName + " (name, country, state, city, start_hour, start_minute, trajectory) VALUES ('"
				+ t.getName() + "','Brazil','SC','Massaranduba'," +
				t.getPoints().get(0).getHour() + "," + 
				t.getPoints().get(0).getMinutes() + 
				", ST_GeomFromEWKT('SRID=4326;MULTIPOINTM(" + ts.toString() + ")'));";
		
		Connection conn = this.getConn();
		conn.setAutoCommit(false);
		Statement statement = conn.createStatement();
		statement.executeUpdate(insert);
		statement.close();
		conn.commit();
		conn.close();
	}
	
	public void loadTrajectoryMongo(Trajectory t) throws Exception {
		DBCollection db = this.getDatabase();
		
		BasicDBObject trajectory = new BasicDBObject();
		trajectory.put("type", "LineString");
		BasicDBList points = new BasicDBList();
		for(Point p : t.getPoints()) {
			BasicDBList pts = new BasicDBList();
			pts.add(p.getLng());
			pts.add(p.getLat());
			points.add(pts);
		}
		trajectory.put("coordinates", points);
		
		BasicDBList pointsFull = new BasicDBList();
		for(Point p : t.getPoints()) {
			BasicDBList pts = new BasicDBList();
			pts.add(p.getLng());
			pts.add(p.getLat());
			pts.add(p.getTimestamp());
			pointsFull.add(pts);
		}
		
		BasicDBObject document = new BasicDBObject();
		document.put("name", t.getName());
		document.put("country", "Brazil");
		document.put("state", "SC");
		document.put("city", "Joinville");
		document.put("start_hour", t.getPoints().get(0).getHour());
		document.put("start_minute", t.getPoints().get(0).getMinutes());
		document.put("trajectory", trajectory);
		document.put("points", pointsFull);
		
		db.insert(document);
	}
	
	private DBCollection getDatabase() {
		if(this.mongoClient == null) {
			this.mongoClient = new MongoClient("localhost", 27017);
		}
		return this.mongoClient.getDB("udesc").getCollection("trajectories");
	}
	
	private Connection getConn() throws SQLException {
		return DriverManager.getConnection(url, user, password);
	}
	
	public static void main(String[] args) {
		JoinvilleExtractor joinvilleExtractor = new JoinvilleExtractor();
		joinvilleExtractor.loadTrajectories("/home/joao/Área de Trabalho/Mestrado/Extracted/tidy/ALL");
	}
	
}
