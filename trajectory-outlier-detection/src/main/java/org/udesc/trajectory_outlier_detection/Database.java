package org.udesc.trajectory_outlier_detection;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Database {

	private String path;

	private List<Trajectory> trajectories = new ArrayList<Trajectory>();

	public Database(String path) {
		super();
		this.path = path;
	}

	public void initialize() {
		try {
			File f = new File(path);
			ArrayList<String> names = new ArrayList<String>(Arrays.asList(f.list()));
			for (int i = 0; i < names.size(); i++) {
				Trajectory trajectory = new Trajectory();
				trajectory.setName(names.get(i));
				File file = new File(path + "/" + names.get(i));
				FileReader fileReader = new FileReader(file);
				BufferedReader bufferedReader = new BufferedReader(fileReader);
				String line;
				boolean fileHeaderProcessed = false;
				while ((line = bufferedReader.readLine()) != null) {
					if (!fileHeaderProcessed && line.length() > 0) {
						fileHeaderProcessed = true;
					} else if (fileHeaderProcessed && line.length() > 0) {
						String[] lineS = line.split(";");
						trajectory.getPoints().add(new Point(
								Integer.valueOf(lineS[13]),
								Integer.valueOf(lineS[14]), 
								Double.valueOf(lineS[3]),
								Double.valueOf(lineS[4]),
								Long.valueOf(lineS[17])));
					}
				}
				Collections.sort(trajectory.getPoints(), new Comparator<Point>() {
					public int compare(Point o1, Point o2) {
						return (int) (o1.getTimestamp() - o2.getTimestamp());
					}
				});
				if (trajectory.getPoints().size() > 1) {
					this.trajectories.add(trajectory);
				}
				bufferedReader.close();
				if(i % 10 == 0) {
					System.out.println("Load: " + i + " from: " + names.size());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public List<Trajectory> getTrajectories() {
		return this.trajectories;
	}

}
