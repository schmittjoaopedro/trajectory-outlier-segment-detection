package org.udesc.trajectory_outlier_detection.extractors;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.udesc.trajectory_outlier_detection.Point;
import org.udesc.trajectory_outlier_detection.Trajectory;

public class UberExtractor {


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
				t.initialize(true);
				t.interpolate(0.0003);
				t.initialize(true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public List<Trajectory> getTrajectories() {
		return new ArrayList<Trajectory>(this.trajectories.values());
	}
	
	public static void main(String[] args) {
		UberExtractor extractor = new UberExtractor();
		extractor.loadTrajectories("/home/joao/Área de Trabalho/Mestrado/Extracted/uber/all.tsv");
		List<Trajectory> trajectories = extractor.getTrajectories();
		extractor.getBounds(trajectories);
		System.out.println(trajectories.size());
		for(int i = 0; i < trajectories.size(); i++) {
			System.out.println(trajectories.get(i).toStringDefault());
			if(i % 100 == 0) {
				System.out.println("");
			}
		}
	}
	
	public void getBounds(List<Trajectory> trajectories) {
    	double minLat = 0, maxLat = 0, minLng = 0, maxLng = 0;
    	for(Trajectory trajectory : trajectories) {
    		for(Point p : trajectory.getPoints()) {
    			if(p.getLat() < minLat || minLat == 0) {
    				minLat = p.getLat();
    			}
    			if(p.getLat() > maxLat || maxLat == 0) {
    				maxLat = p.getLat();
    			}
    			if(p.getLng() < minLng || minLng == 0) {
    				minLng = p.getLng();
    			}
    			if(p.getLng() > maxLng || maxLng == 0) {
    				maxLng = p.getLng();
    			}
    		}
    	}
    	System.out.println("Min lat: " + minLat);
    	System.out.println("Max lat: " + maxLat);
    	System.out.println("Min lng: " + minLng);
    	System.out.println("Max lng: " + maxLng);
    	System.out.println("Lat center: " + (minLat + ((maxLat - minLat) / 2)));
    	System.out.println("Lng center: " + (minLng + ((maxLng - minLng) / 2)));
    }
}
