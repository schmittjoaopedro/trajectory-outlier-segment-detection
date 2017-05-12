package trajectory_outlier_detection_app;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.udesc.database.TrajectoryMongoDB;
import org.udesc.trajectory.TODS.Grid;
import org.udesc.trajectory.TODS.Point;
import org.udesc.trajectory.TODS.Route;
import org.udesc.trajectory.TODS.TODS;
import org.udesc.trajectory.TODS.TODSRequest;
import org.udesc.trajectory.TODS.TODSResult;
import org.udesc.trajectory.TODS.Trajectory;

public class TODSTest {
	
//	Joinville
//	private static double latSt = -26.370924;
//	private static double latEn = -26.237597;
//	private static double lngSt = -48.944078;
//	private static double lngEn = -48.775826;
//	San Francisco
	private static double latSt = 37.711624;
	private static double latEn = 37.813405;
	private static double lngSt = -122.495955;
	private static double lngEn = -122.390732;
	
	private static double L = 30.0;
	private static String outputFolder = "/home/joao/Área de Trabalho/Mestrado/output/TODS/San Francisco - Inv";
	private static List<Trajectory> trajectories;
	private static List<Grid> regions;
	private static TODS tods;
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception {
		
		trajectories = new TrajectoryMongoDB().findAll("EUA", Trajectory.class, Point.class);
		for(Trajectory trajectory : trajectories) {
			trajectory.filterNoise(0.0015, 0.001);
			trajectory.interpolate(0.0003);
		}
		regions =  createGrid();
		tods = new TODS();
		
		for(int i = 0; i < regions.size(); i++) {
    		long tStart = System.currentTimeMillis();
    		for(int j = i + 1; j < regions.size(); j++) {
    			execute(j, i);
        	}
    		System.out.println(i + " in " + (System.currentTimeMillis() - tStart));
    	}
		
	}
	
	public static void execute(int i, int j) throws Exception {
		TODSRequest request = new TODSRequest();
		request.setAngle(30.0);
		request.setDistance(0.0003);
		request.setkStandard(1);
		request.setStartHour(0);
		request.setEndHour(23);
		request.setStartGrid(regions.get(i));
		request.setEndGrid(regions.get(j));
		TODSResult result = tods.run(request, trajectories);
		if(!result.getNotStandards().isEmpty()) {
			printData(request.getStartGrid(), request.getEndGrid(), i, j, result);
		}
	}
	
	
	public static List<Grid> createGrid() {
    	List<Grid> grids = new ArrayList<Grid>();
    	double W = (latEn - latSt) / L;
    	double H = (lngEn - lngSt) / L;
    	for(double w = 0; w < L; w++) {
    		for(double h = 0; h < L; h++) {
    			Grid grid = new Grid();
    			grid.setLatMin(latSt + (W * w));
    			grid.setLatMax(latSt + (W * (w + 1)));
    			grid.setLngMin(lngSt + (H * h));
    			grid.setLngMax(lngSt + (H * (h + 1)));
    			grids.add(grid);
    		}
    	}
    	return grids;
    }
	
	public static void printData(Grid SR, Grid ER, int i, int j, TODSResult result) throws Exception {
    	File file = new File(outputFolder + "/_" + i + "_" + j + ".txt");
    	int standards = 0, notStandards = 0, stdSeg = 0, notStdSeg = 0;
    	
    	FileUtils.write(file, "//Start grid", "UTF-8", true);
    	FileUtils.write(file, drawPolyline(Arrays.asList(
			new Point(SR.getLatMin(), SR.getLngMin()),
			new Point(SR.getLatMin(), SR.getLngMax()),
			new Point(SR.getLatMax(), SR.getLngMax()),
			new Point(SR.getLatMax(), SR.getLngMin()),
			new Point(SR.getLatMin(), SR.getLngMin())
    	), "FFFFFF"), "UTF-8", true);
    	
    	FileUtils.write(file, "\n//End grid", "UTF-8", true);
    	FileUtils.write(file, drawPolyline(Arrays.asList(
			new Point(ER.getLatMin(), ER.getLngMin()),
			new Point(ER.getLatMin(), ER.getLngMax()),
			new Point(ER.getLatMax(), ER.getLngMax()),
			new Point(ER.getLatMax(), ER.getLngMin()),
			new Point(ER.getLatMin(), ER.getLngMin())
		), "000000"), "UTF-8", true);
    	
    	FileUtils.write(file, "\n\n//Standards", "UTF-8", true);
    	for(int g = 0; g < result.getStandards().size(); g++) {
    		FileUtils.write(file, "\n//Group " + g, "UTF-8", true);
    		for(Trajectory t : result.getStandards().get(g).getTrajectories()) {
    			standards++;
    			FileUtils.write(file, drawPolyline(t.getPoints(), "FF0000"), "UTF-8", true);
    		}
    	}
    	
    	FileUtils.write(file, "\n\n//Standards route", "UTF-8", true);
    	for(int g = 0; g < result.getNotStandards().size(); g++) {
    		FileUtils.write(file, "\n//Group " + g, "UTF-8", true);
    		for(Route route : result.getNotStandards().get(g).getRoutes()) {
    			notStandards++;
    			for(Trajectory t : route.getStandards()) {
    				stdSeg++;
    				FileUtils.write(file, drawPolyline(t.getPoints(), "FF0000"), "UTF-8", true);
        		}
    			for(Trajectory t : route.getNotStandards()) {
    				notStdSeg++;
    				FileUtils.write(file, drawPolyline(t.getPoints(), "0000FF"), "UTF-8", true);
        		}
    		}
    	}
    	
    	FileUtils.write(file, "\n\n//Time taken: " + result.getProgramTime(), "UTF-8", true);
    	FileUtils.write(file, "\n//STD: " + standards, "UTF-8", true);
    	FileUtils.write(file, "\n//NOT STD: " + notStandards, "UTF-8", true);
    	FileUtils.write(file, "\n//STD SEG: " + stdSeg, "UTF-8", true);
    	FileUtils.write(file, "\n//NOT STD SEG: " + notStdSeg, "UTF-8", true);
    }
	
	public static String drawPolyline(List<Point> points, String color) {
		StringBuilder data = new StringBuilder();
		data.append("\ndrawPoints([");
		for(int i = 0; i < points.size(); i++) {
			data.append("{lat: " + points.get(i).getLat() + ",lng:" + points.get(i).getLng() + "}");
			if(i < points.size() - 1) {
				data.append(",");
			}
		}
		data.append("], '#" + color + "');");
		return data.toString();
	}
	
}
