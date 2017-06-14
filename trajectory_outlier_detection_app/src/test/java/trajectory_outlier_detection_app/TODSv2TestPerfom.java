package trajectory_outlier_detection_app;

import org.udesc.database.TrajectoryMongoDB;
import org.udesc.trajectory.TODS.Grid;
import org.udesc.trajectory.TODS.IndexManager;
import org.udesc.trajectory.TODS.Point;
import org.udesc.trajectory.TODS.Trajectory;
import smile.clustering.DBScan;
import smile.neighbor.RNNSearch;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by root on 10/06/17.
 */
public class TODSv2TestPerfom {

    private static List<Grid> regions;
    private static double L = 30.0;
    private static double latSt = -26.370924;
    private static double latEn = -26.237597;
    private static double lngSt = -48.944078;
    private static double lngEn = -48.775826;
    private static String country = "Brazil";
    private static String state = "SC";
    private static String city = "Joinville";

    public static void main(String[] args) throws Exception {

        TrajectoryMongoDB db = new TrajectoryMongoDB();

        for(int i = 1; i <= 10; i++) {
            List<Trajectory> trajectories = db.findAll(country, Trajectory.class, Point.class).subList(0, 100 * i);
            long points = 0;
            for(Trajectory t : trajectories) {
                points += t.getPoints().size();
            }
            System.out.print(points);
            long time = System.nanoTime();
            IndexManager index = new IndexManager(trajectories, 0.00044996400287976963);
            System.out.println(";" + ((System.nanoTime() - time) / Math.pow(10, 9)));
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

}
