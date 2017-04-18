package org.udesc.trajectory_outlier_detection.extractors;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.udesc.trajectory_outlier_detection.Point;
import org.udesc.trajectory_outlier_detection.Trajectory;

public class TDriveExtractor {

	private List<Trajectory> trajectories = new ArrayList<Trajectory>();
	
	public void loadTrajectories(String path) {
		try {
			File fldr = new File(path);
			ArrayList<String> folders = new ArrayList<String>(Arrays.asList(fldr.list()));
			for(String folder : folders) {
				File f = new File(path + "/" + folder);
				if(!f.isFile()) {
					ArrayList<String> names = new ArrayList<String>(Arrays.asList(f.list()));
					for (int i = 0; i < names.size(); i++) {
						this.trajectories.add(this.loadFile(path + "/" + folder + "/" + names.get(i)));
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Trajectory loadFile(String filePath) {
		try {
			Trajectory trajectory = new Trajectory();
			File file = new File(filePath);
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String line;
			DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			int count = 0;
			while ((line = bufferedReader.readLine()) != null && count < 100) {
				count++;
				String data[] = line.split(",");
				String id = data[0];
				LocalDateTime date = LocalDateTime.parse(data[1], format);
				Double lng = Double.valueOf(data[2]);
				Double lat = Double.valueOf(data[3]);
				trajectory.setName(id);
				trajectory.getPoints().add(new Point(
						date.getHour(), 
						date.getMinute(), 
						lat, 
						lng, 
						Timestamp.valueOf(date).getTime()));
			}
			trajectory.initialize(true);
			bufferedReader.close();
			return trajectory;
		} catch (Exception e) {
//			e.printStackTrace();
		}
		return null;
	}
	
	public List<Trajectory> getTrajectories() {
		return this.trajectories;
	}
	
	public static void main(String[] args) {
		TDriveExtractor extractor = new TDriveExtractor();
//		extractor.loadTrajectories("/home/joao/Área de Trabalho/Mestrado/Extracted/tdrive/tdrive");
//		List<Trajectory> trajectories = extractor.getTrajectories();
//		extractor.getBounds(trajectories);
//		System.out.println(trajectories.size());
//		System.out.println(trajectories.get(1000).toStringDefault());
		
		Trajectory t = extractor.loadFile("/home/joao/Área de Trabalho/Mestrado/Extracted/tdrive/tdrive/01/366.txt");
		extractor.getBounds(Arrays.asList(t));
		System.out.println(t.toStringDefault());
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
