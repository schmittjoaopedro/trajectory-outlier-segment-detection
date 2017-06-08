package trajectory_outlier_detection_app;

import java.util.List;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.udesc.database.TrajectoryMongoDB;
import org.udesc.trajectory.TODS.Point;
import org.udesc.trajectory.TODS.Trajectory;

public class Statistics {

	public static void main(String[] args) throws Exception {
		
		TrajectoryMongoDB database = new TrajectoryMongoDB();
		List<Trajectory> trajectories = database.findAll("EUA", Trajectory.class, Point.class);
		DescriptiveStatistics stats = new DescriptiveStatistics();
		DescriptiveStatistics stats2 = new DescriptiveStatistics();
		long tN = 0;
		for(Trajectory t : trajectories) {
			tN++;
			for(int i = 0; i < t.getPoints().size() - 1; i++) {
				Point p1 = t.getPoints().get(i);
				Point p2 = t.getPoints().get(i + 1);
				stats.addValue(measure(p1.getLat(), p1.getLng(), p2.getLat(), p2.getLng()));
			}
			stats2.addValue(t.getPoints().size());
		}
		
		System.out.println("Mean: " + stats.getMean());
		System.out.println("Sd: " + stats.getStandardDeviation());
		System.out.println("Max: " + stats.getMax());
		System.out.println("Min: " + stats.getMin());
		System.out.println("N: " + stats.getN());
		System.out.println("Numero traj: " + tN);
		
		System.out.println("PMean: " + stats2.getMean());
		System.out.println("PSD: " + stats2.getStandardDeviation());
		System.out.println("PMax: " + stats2.getMax());
		System.out.println("PMin: " + stats2.getMin());
	}
	
	public static double measure(double lat1, double lon1, double lat2, double lon2) {
	    double R = 6378.137; // Radius of earth in KM
	    double dLat = lat2 * Math.PI / 180 - lat1 * Math.PI / 180;
	    double dLon = lon2 * Math.PI / 180 - lon1 * Math.PI / 180;
	    double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
	    Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) * Math.sin(dLon/2) * Math.sin(dLon/2);
	    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
	    double d = R * c;
	    return d * 1000; // meters
	}
	
}
