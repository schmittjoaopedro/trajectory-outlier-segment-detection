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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.udesc.trajectory_outlier_detection.Point;
import org.udesc.trajectory_outlier_detection.Trajectory;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

public class UberExtractor {
	
	private String url = "jdbc:postgresql://localhost:5432/nyc";
	
	private String user = "udesc";

	private String password = "udesc";

	MongoClient mongoClient;
	
	private Map<Integer, Trajectory> trajectories = new HashMap<Integer, Trajectory>();
	
	public void loadTrajectories(String path) {
		try {
			File file = new File(path);
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String line;
			int lineCount = 0;
			DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			while ((line = bufferedReader.readLine()) != null) {
				if(lineCount == 1) {
					String data[] = line.split("\t");
					Integer id = Integer.valueOf(data[0]);
					String dateStr = data[1].split("T")[0] + " "  + data[1].split("T")[1].split("\\+")[0];
					LocalDateTime date = LocalDateTime.parse(dateStr, format);
					Double lat = Double.valueOf(data[2]);
					Double lng = Double.valueOf(data[3]);
					
					if(!trajectories.containsKey(id)) {
						Trajectory trajectory = new Trajectory();
						trajectory.setName(id.toString());
						trajectories.put(id, trajectory);
					}
					trajectories.get(id).getPoints().add(new Point(
							date.getHour(), 
							date.getMinute(), 
							lat, 
							lng, 
							Timestamp.valueOf(date).getTime()));
					
					if(lineCount % 1000 == 0) System.out.println(lineCount);
					
				} else {
					lineCount++;
				}
			}
			bufferedReader.close();
			for(Trajectory t : trajectories.values()) {
				t.initialize();
				loadTrajectoryMongo(t);
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
		String insert = "INSERT INTO trajectory_application (name, country, state, city, start_hour, start_minute, trajectory) VALUES ('"
				+ t.getName() + "','EUA','CA','San Francisco'," +
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
		document.put("country", "EUA");
		document.put("state", "CA");
		document.put("city", "San Francisco");
		document.put("start_hour", t.getPoints().get(0).getHour());
		document.put("start_minute", t.getPoints().get(0).getMinutes());
		document.put("trajectory", trajectory);
		document.put("points", pointsFull);
		
		db.insert(document);
	}
	
	private Connection getConn() throws SQLException {
		return DriverManager.getConnection(url, user, password);
	}
	
	public List<Trajectory> getTrajectories() {
		return new ArrayList<Trajectory>(this.trajectories.values());
	}
	
	private DBCollection getDatabase() {
		if(this.mongoClient == null) {
			this.mongoClient = new MongoClient("localhost", 27017);
		}
		return this.mongoClient.getDB("udesc").getCollection("trajectories");
	}
	
	public static void main(String[] args) {
		UberExtractor extractor = new UberExtractor();
		extractor.loadTrajectories("/home/joao/Área de Trabalho/Mestrado/Extracted/uber/all.tsv");
	}
	

	
}
