package com.trajectory;

import java.util.List;

public class App {
	
	public static void main(String[] args) {
		//Database database = new Database("/home/joao/Área de Trabalho/Mestrado/TRA-SOD/trajectories");
		Database database = new Database("/home/joao/Área de Trabalho/Mestrado/TRA-SOD/joinville");
		database.initialize();
		
		List<Region> regions = database.getRegions();
		double maxDist = 0.003;
		int minSup = 2;
		
		for(int i = 0; i < regions.size(); i++) {
			System.out.println(regions.get(i));
			for(int n = i + 1; n < regions.size(); n++) {
				List<Trajectory> candidates = database.findCandidates(regions.get(i), regions.get(n));
				List<Trajectory> standards = database.findStandards(candidates, maxDist, minSup);
				for(Trajectory traj : candidates) { if(traj.getPoints().size() > 1) System.out.println(traj); }
				//for(Trajectory std : standards) { if(std.getPoints().size() > 1) System.out.println(std); }
			}
		}
		
	}
	
}
