package trajectory_outlier_detection_app;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.udesc.database.TrajectoryMongoDB;
import org.udesc.trajectory.TRASOD.Grid;
import org.udesc.trajectory.TRASOD.Point;
import org.udesc.trajectory.TRASOD.TRASOD;
import org.udesc.trajectory.TRASOD.TRASODRequest;
import org.udesc.trajectory.TRASOD.TRASODResult;
import org.udesc.trajectory.TRASOD.Trajectory;

public class TRASODTest {


//	Joinville
	private static double latSt = -26.370924;
	private static double latEn = -26.237597;
	private static double lngSt = -48.944078;
	private static double lngEn = -48.775826;
//	San Francisco
//	private static double latSt = 37.711624;
//	private static double latEn = 37.813405;
//	private static double lngSt = -122.495955;
//	private static double lngEn = -122.390732;
	
	private static double L = 30.0;
	private static String outputFolder = "/home/joao/Área de Trabalho/Mestrado/output/TODS/Temp2";
	private static List<Trajectory> trajectories;
	private static List<Grid> regions;
	private static TRASOD trasod;
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception {
		
		trajectories = new TrajectoryMongoDB().findAll("Brazil", Trajectory.class, Point.class);
		regions =  createGrid();
		trasod = new TRASOD();
		
//		for(int i = 0; i < regions.size(); i++) {
//    		long tStart = System.currentTimeMillis();
//    		for(int j = i + 1; j < regions.size(); j++) {
//    			execute(j, i);
//        	}
//    		System.out.println(i + " in " + (System.currentTimeMillis() - tStart));
//    	}
		
		//San Francisco
//		execute(380,561);
//		execute(434,439);
//		execute(471,595);
//		execute(591,415);
		//Joinville
		execute(347,496);
		execute(378,707);
		execute(616,650);
		execute(676,527);
		
	}
	
	public static void execute(int i, int j) throws Exception {
		TRASODRequest request = new TRASODRequest();
		request.setStartGrid(regions.get(i));
		request.setEndGrid(regions.get(j));
		request.setMinSup(4.0);
		request.setMaxDist(0.00044996400287976963);
		TRASODResult result = trasod.run(request, trajectories);
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
	
	public static void printData(Grid SR, Grid ER, int i, int j, TRASODResult result) throws Exception {
    	File file = new File(outputFolder + "/TRASOD_" + i + "_" + j + ".txt");
    	int standards = 0, notStandards = 0;
    	
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
    	
    	FileUtils.write(file, "\n\n//Not Standards", "UTF-8", true);
		for(Trajectory t : result.getNotStandards()) {
			notStandards++;
    		FileUtils.write(file, drawPolyline(t.getPoints(), "0000FF"), "UTF-8", true);
    	}

		FileUtils.write(file, "\n\n//Standards", "UTF-8", true);
    	for(Trajectory t : result.getStandards()) {
    		standards++;
			FileUtils.write(file, drawPolyline(t.getPoints(), "FF0000"), "UTF-8", true);
    	}
    	
    	FileUtils.write(file, "\n\n//Time taken: " + result.getProgramTime(), "UTF-8", true);
    	FileUtils.write(file, "\n//STD: " + standards, "UTF-8", true);
    	FileUtils.write(file, "\n//NOT STD: " + notStandards, "UTF-8", true);
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
