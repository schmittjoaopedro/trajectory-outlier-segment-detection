package trajectory_outlier_detection_app;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.udesc.trajectory.TODS.*;

public class TODSTest {
	
//	Joinville
	private static double latSt = -26.370924;
	private static double latEn = -26.237597;
	private static double lngSt = -48.944078;
	private static double lngEn = -48.775826;
	private static String country = "Brazil";
	private static String state = "SC";
	private static String city = "Joinville";
	private static String outputFile = "/home/joao/Área de Trabalho/Mestrado/Estatísticas/statistics_bra2.csv";
	private static String inputFile  = "/home/joao/Área de Trabalho/Mestrado/Estatísticas/brasil_best.csv";
	
//	San Francisco
//	private static double latSt = 37.711624;
//	private static double latEn = 37.813405;
//	private static double lngSt = -122.495955;
//	private static double lngEn = -122.390732;
//	private static String country = "EUA";
//	private static String state = "CA";
//	private static String city = "San Francisco";
//	private static String outputFile = "/home/joao/Área de Trabalho/Mestrado/Estatísticas/statistics_eua2.csv";
//	private static String inputFile  = "/home/joao/Área de Trabalho/Mestrado/Estatísticas/eua_best.csv";
	
	private static String outputFolder = "/home/joao/Área de Trabalho/Mestrado/output/TODSv2/Joinville";
	private static double L = 30.0;
	private static List<Trajectory> trajectories;
	private static List<Grid> regions;
	private static TODS tods;
	
	public static void main(String[] args) throws Exception {
		
//		trajectories = new TrajectoryMongoDB().findAll(country, Trajectory.class, Point.class);
		regions =  createGrid();
		tods = new TODS();
		
		
//		FileUtils.write(new File(outputFile), "R1,R2,QT,CT,GT,STDT,SEGT,MEM,STD,NSTD,STDSEG,NOTSTDSEG,PTQTDALL,PTQTDEPR\n", "UTF-8", true);
//		for(int i = 0; i < regions.size(); i++) {
//			for(int j = i + 1; j < regions.size(); j++) {
//				execute(i, j);
//			}
//		}
		
		/*int regions[][] = readRegions();
		for(int i = 0; i < regions.length; i++) {
			executeProgram(regions[i][0], regions[i][1]);
		}*/

	}

	public static void loadTest(int i, int j, int sample) throws Exception {
        TODSRequest request = new TODSRequest();
        request.setCountry(country);
        request.setState(state);
        request.setCity(city);
        request.setAngle(30.0);
        request.setDistance(0.00044996400287976963);
        request.setkStandard(1);
        request.setInterpolation(0.0003);
        request.setSd(0.01);
        request.setSigma(0.0015);
        request.setSimilarityRatio(0.95);
        request.setStartHour(0);
        request.setEndHour(23);
        request.setStartGrid(regions.get(i));
        request.setEndGrid(regions.get(j));
        TODSResult result = null;//tods.loadTest(request, sample);
        String data = result.getRawResult().size() + ";" + result.countPts() + ";" +
                String.format("%.9f", (result.getCandidateTime() / Math.pow(10, 9))) + ";" +
                String.format("%.9f", (result.getGroupTime() / Math.pow(10, 9))) + ";" +
                String.format("%.9f", (result.getStandardTime() / Math.pow(10, 9))) + ";" +
                String.format("%.9f", (result.getSegmentationTime() / Math.pow(10, 9))) + "\n";
        FileUtils.write(new File(outputFolder + "/temp2.csv"), data, true);
    }
	
	public static void executeProgram(int i, int j) throws Exception {
		TODSRequest request = new TODSRequest();
		request.setCountry(country);
		request.setState(state);
		request.setCity(city);
		request.setAngle(30.0);
		request.setDistance(0.00044996400287976963);
		request.setkStandard(1);
		request.setInterpolation(0.0003);
		request.setSd(0.01);
		request.setSigma(0.0015);
		request.setSimilarityRatio(0.95);
		request.setStartHour(0);
		request.setEndHour(23);
		request.setStartGrid(regions.get(i));
		request.setEndGrid(regions.get(j));
		TODSResult result = tods.run(request);
		
		int countStd = 0;
		int countNotStd = 0;
		int nStd = 0;
		int std = 0;
		long pointQtdeAll = 0;
		long pointQtdeProcessed = 0;
		
		for(Group group : result.getStandards()) {
			countStd += group.getTrajectories().size();
			for(Trajectory t : group.getTrajectories()) {
				pointQtdeProcessed += t.getPoints().size();
			}
		}
		for(Group group : result.getNotStandards()) {
			countNotStd += group.getTrajectories().size();
			for(Route route : group.getRoutes()) {
				std += route.getStandards().size();
				nStd += route.getNotStandards().size();
			}
			for(Trajectory t : group.getTrajectories()) {
				pointQtdeProcessed += t.getPoints().size();
			}
		}
		for(Trajectory t : result.getRawResult()) {
			pointQtdeAll += t.getPoints().size();
		}
		if(j % 100 == 0) {
			System.out.println(i + "\t" + j);
		}
		if(countStd > 0) {
			String data = String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
				i,
				j,
                result.getQueryTime(),
				result.getCandidateTime(),
				result.getGroupTime(),
				result.getStandardTime(),
				result.getSegmentationTime(),
				(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()),
				countStd,
				countNotStd,
				std,
				nStd,
				pointQtdeAll,
				pointQtdeProcessed);
			FileUtils.write(new File(outputFile), data, "UTF-8", true);
		}
	}
	
	public static List<Trajectory> getTrajectories(List<Trajectory> trajectoriesDB) {
		List<Trajectory> trajectories = new ArrayList<>();
		for(Trajectory t : trajectoriesDB) {
			Trajectory nT = new Trajectory();
			nT.setCity(t.getCity());
			nT.setCountry(t.getCountry());
			nT.setName(t.getName());
			nT.setStartHour(t.getStartHour());
			nT.setStartMinute(t.getStartMinute());
			nT.setState(t.getState());
			for(Point p : t.getPoints()) {
				Point nP = new Point();
				nP.setLat(p.getLat());
				nP.setLng(p.getLng());
				nP.setStandard(false);
				nP.setTimestamp(p.getTimestamp());
				nT.getPoints().add(nP);
			}
			trajectories.add(nT);
		}
		return trajectories;
	}
	
	public static void execute(int i, int j) throws Exception {
//		for(int h = 4; h < 24; h += 4) {
//			int n = h - 4;
			TODSRequest request = new TODSRequest();
			request.setCountry(country);
			request.setState(state);
			request.setCity(city);
			request.setAngle(30.0);
			request.setDistance(0.00044996400287976963);
			request.setkStandard(1);
			request.setInterpolation(0.0003);
			request.setSd(0.01);
			request.setSigma(0.0015);
			request.setSimilarityRatio(0.95);
			request.setStartHour(0);
			request.setEndHour(23);
			request.setStartGrid(regions.get(i));
			request.setEndGrid(regions.get(j));
			TODSResult result = tods.run(request);
			System.out.println(i + "\t" + j);
			if(!result.getNotStandards().isEmpty()) {
				printData(request.getStartGrid(), request.getEndGrid(), i, j, result);
			}
//		}
	}
	
	public static boolean betweenLat(Point p, Grid grid) {
		return grid.getLatMin() <= p.getLat() && p.getLat() <= grid.getLatMax();
	}
	
	public boolean betweenLng(Point p, Grid grid) {
		return grid.getLngMin() <= p.getLng() && p.getLng() <= grid.getLngMax();
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
		File file = new File(outputFolder + "/TODS_" + i + "_" + j + ".txt");
		int standards = 0, notStandards = 0, stdSeg = 0, notStdSeg = 0;

		for(int g = 0; g < result.getStandards().size(); g++) {
			standards += result.getStandards().get(g).getTrajectories().size();
		}
		for(int g = 0; g < result.getNotStandards().size(); g++) {
			for(Route route : result.getNotStandards().get(g).getRoutes()) {
				notStandards++;
				stdSeg += route.getStandards().size();
				notStdSeg += route.getNotStandards().size();
			}
		}

		FileUtils.write(file, "//getCandidateTime: " + String.format("%,d", result.getCandidateTime()), "UTF-8", true);
		FileUtils.write(file, "\n//getGroupTime: " + String.format("%,d", result.getGroupTime()), "UTF-8", true);
		FileUtils.write(file, "\n//getStandardTime: " + String.format("%,d", result.getStandardTime()), "UTF-8", true);
		FileUtils.write(file, "\n//getNotStandards: " + String.format("%,d", result.getSegmentationTime()), "UTF-8", true);
		FileUtils.write(file, "\n//STD: " + standards, "UTF-8", true);
		FileUtils.write(file, "\n//NOT STD: " + notStandards, "UTF-8", true);
		FileUtils.write(file, "\n//STD SEG: " + stdSeg, "UTF-8", true);
		FileUtils.write(file, "\n//NOT STD SEG: " + notStdSeg, "UTF-8", true);

		FileUtils.write(file, "\n//Start grid", "UTF-8", true);
		FileUtils.write(file, drawPolyline(Arrays.asList(
				new Point(SR.getLatMin(), SR.getLngMin(), null),
				new Point(SR.getLatMin(), SR.getLngMax(), null),
				new Point(SR.getLatMax(), SR.getLngMax(), null),
				new Point(SR.getLatMax(), SR.getLngMin(), null),
				new Point(SR.getLatMin(), SR.getLngMin(), null)
		), "FFFFFF"), "UTF-8", true);

		FileUtils.write(file, "\n//End grid", "UTF-8", true);
		FileUtils.write(file, drawPolyline(Arrays.asList(
				new Point(ER.getLatMin(), ER.getLngMin(), null),
				new Point(ER.getLatMin(), ER.getLngMax(), null),
				new Point(ER.getLatMax(), ER.getLngMax(), null),
				new Point(ER.getLatMax(), ER.getLngMin(), null),
				new Point(ER.getLatMin(), ER.getLngMin(), null)
		), "000000"), "UTF-8", true);

		FileUtils.write(file, "\n\n//Standards", "UTF-8", true);
		for(int g = 0; g < result.getStandards().size(); g++) {
			FileUtils.write(file, "\n//Group " + g, "UTF-8", true);
			for(Trajectory t : result.getStandards().get(g).getTrajectories()) {
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
	
	public static int[][] readRegions() throws Exception {
		List<String> lines = FileUtils.readLines(new File(inputFile), "UTF-8");
		lines.remove(0);
		int regions[][] = new int[lines.size()][2];
		for(int i = 0; i < lines.size(); i++) {
			String line[] = lines.get(i).split(",");
			regions[i][0] = Integer.valueOf(line[0]);
			regions[i][1] = Integer.valueOf(line[1]);
		}
		return regions;
	}
	
}
