package com.trajectory;

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
	
	private List<Trajectory> trajectories = new ArrayList<>();
	
	private List<Region> regions = new ArrayList<>();
	
	public Database(String path) {
		super();
		this.path = path;
	}
	
	public void initialize() {
		try {
			File f = new File(path);
			ArrayList<String> names = new ArrayList<String>(Arrays.asList(f.list()));
			for(int i = 0; i < names.size(); i++) {
				if(names.get(i).equals("regions.csv")) {
					File file = new File(path + "/" + names.get(i));
					FileReader fileReader = new FileReader(file);
					BufferedReader bufferedReader = new BufferedReader(fileReader);
					String line;
					while ((line = bufferedReader.readLine()) != null) {
						regions.add(new Region(
							line.split(";")[0], 
							Double.valueOf(line.split(";")[1]), 
							Double.valueOf(line.split(";")[2]),
							Double.valueOf(line.split(";")[3])));
					}
					bufferedReader.close();
				} else {
					Trajectory trajectory = new Trajectory();
					trajectory.setName(names.get(i));
					File file = new File(path + "/" + names.get(i));
					FileReader fileReader = new FileReader(file);
					BufferedReader bufferedReader = new BufferedReader(fileReader);
					String line;
					boolean fileHeaderProcessed = false;
					while ((line = bufferedReader.readLine()) != null) {
						if(!fileHeaderProcessed && line.length() > 0) {
							fileHeaderProcessed = true;
						} else if(fileHeaderProcessed && line.length() > 0) {
							trajectory.getPoints().add(new Point(
								Double.valueOf(line.split(";")[0]), 
								Double.valueOf(line.split(";")[1]), 
								Double.valueOf(line.split(";")[2])));
						}
					}
					Collections.sort(trajectory.getPoints(), new Comparator<Point>() {
						@Override
						public int compare(Point o1, Point o2) {
							return (int) (o1.getTime() - o2.getTime());
						}
					});
					if(trajectory.getPoints().size() > 1) {
						this.trajectories.add(trajectory);
					}
					bufferedReader.close();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public List<Region> getRegions() {
		return regions;
	}
	
	public List<Trajectory> findCandidates(Region regionStart, Region regionEnd) {
		List<Trajectory> trajectories = new ArrayList<>();
		
		for(Trajectory trajectory : this.trajectories) {
			
			List<Point> edgesR1 = regionStart.getInnerPoints(trajectory);
			List<Point> edgesR2 = regionEnd.getInnerPoints(trajectory);
			if(!edgesR1.isEmpty() && !edgesR2.isEmpty()) {
				List<Point> selectedPoints = this.getLessDistancePoints(edgesR1, edgesR2);
				if(selectedPoints.size() == 2) {
					int startPoint = trajectory.getPoints().indexOf(selectedPoints.get(0));
					int endPoint = trajectory.getPoints().indexOf(selectedPoints.get(1));
					if(startPoint > endPoint) {
						int temp = endPoint;
						startPoint = endPoint;
						endPoint = temp;
					}
					Trajectory traj = new Trajectory();
					traj.setName(trajectory.getName());
					traj.getPoints().addAll(trajectory.getPoints().subList(startPoint, endPoint + 1));
					trajectories.add(traj);
				}
			}			
		}
		
		return trajectories;
	}
	
	private List<Point> getLessDistancePoints(List<Point> pts1, List<Point> pts2) {
		List<Point> points = new ArrayList<>();
		Point p1S = pts1.get(0), p2S = pts2.get(0);
		double distance = p1S.calcDistance(p2S);
		for(int i = 0; i < pts1.size(); i++) {
			for(int j = 0; j < pts2.size(); j++) {
				if(pts1.get(i).calcDistance(pts2.get(j)) < distance) {
					p1S = pts1.get(i);
					p2S = pts2.get(j);
				}
			}
		}
		points.add(p1S);
		points.add(p2S);
		return points;
	}
	
	public List<Trajectory> findStandards(List<Trajectory> candidates, double maxDist, int minSup) {
		List<Trajectory> standarts = new ArrayList<>();
		for(Trajectory candidate : candidates) {
			if(this.findPointWithLessNeighborhood(candidates, candidate, maxDist) >= minSup) {
				candidate.setStandard(true);
				standarts.add(candidate);
			}
		}
		return standarts;
	}
	
	private int findPointWithLessNeighborhood(List<Trajectory> candidates, Trajectory trajectory, double maxDist) {
		int qtd = -1;
		for(Point p : trajectory.getPoints()) {
			int nghbd = 0;
			for(Trajectory candidate : candidates) {
				if(candidate != trajectory) {
					for(Point candidateP : candidate.getPoints()) {
						if(candidateP.calcDistance(p) < maxDist) {
							nghbd++;
						}
					}
				}
			}
			if(qtd == -1) {
				qtd = nghbd;
			} else if(nghbd < qtd) {
				qtd = nghbd;
			}
		}
		return qtd;
	}
	
}
