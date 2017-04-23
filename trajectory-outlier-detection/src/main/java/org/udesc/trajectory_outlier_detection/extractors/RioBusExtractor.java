package org.udesc.trajectory_outlier_detection.extractors;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.udesc.trajectory_outlier_detection.Point;
import org.udesc.trajectory_outlier_detection.Trajectory;

public class RioBusExtractor {

	private List<Trajectory> trajectories = new ArrayList<Trajectory>();
	
	public void loadTrajectories(String path, int i, int maxToLoad) throws Exception {
		try {
			
			List<Object> files = Files.walk(Paths.get(path)).filter(Files::isRegularFile).collect(Collectors.toList());
			for(; i < maxToLoad; i++) {
				try {
					File file = new File(String.valueOf(files.get(i)));
					FileReader fileReader = new FileReader(file);
					BufferedReader bufferedReader = new BufferedReader(fileReader);
					String line;
					DateTimeFormatter format = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss");
					Trajectory t = new Trajectory();
					t.setName(file.getName());
					while ((line = bufferedReader.readLine()) != null) {
						String data[] = line.split(",");
						LocalDateTime date = LocalDateTime.parse(data[0], format);
						Double lat = Double.valueOf(data[2]);
						Double lng = Double.valueOf(data[3]);
						t.getPoints().add(new Point(date.getHour(), date.getMinute(), lat, lng, Timestamp.valueOf(date).getTime()));
					}
					t.initialize(true);
					if(!t.getPoints().isEmpty()) {
						t.interpolate(0.0003);
						t.initialize();
						trajectories.add(t);
					}
					bufferedReader.close();
					if(i % 1000 == 0) {
						System.out.println(i + " from " + maxToLoad);
					}
				} catch(Exception e) {
					
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public List<Trajectory> getTrajectories() {
		return this.trajectories;
	}
	
	public static void main(String[] args) throws Exception {
		Long time = System.currentTimeMillis();
		RioBusExtractor extractor = new RioBusExtractor();
		extractor.loadTrajectories("/home/joao/Área de Trabalho/Mestrado/Extracted/rio/BusRJ-2015/", 0, 10000);
		List<Trajectory> trajectories = extractor.getTrajectories();
		System.out.println(trajectories.size());
		System.out.println(System.currentTimeMillis() - time);
	}
	
}
