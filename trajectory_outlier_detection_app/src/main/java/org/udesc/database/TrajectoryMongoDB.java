package org.udesc.database;

import java.util.ArrayList;
import java.util.List;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class TrajectoryMongoDB extends Database {

	@SuppressWarnings({ "rawtypes" })
	public List findTrajectories(String country, String state, String city, int startHour, int endHour, IGrid st, IGrid en, Class<? extends ITrajectory> trajectoryClazz, Class<? extends IPoint> pointClazz) throws Exception {
		DBCollection collection = this.getDatabase();
		
		BasicDBObject query = new BasicDBObject();
		BasicDBList and = new BasicDBList();
		
		BasicDBObject general = new BasicDBObject();
		general.put("country", country);
		general.put("state", state);
		general.put("city", city);
		BasicDBObject between = new BasicDBObject();
		between.put("$gte", startHour);
		between.put("$lte", endHour);
		general.put("start_hour", between);
		
		and.add(general);
		and.add(this.createRegionQuery(st));
		and.add(this.createRegionQuery(en));
		query.put("$and", and);
		
		DBCursor cursor = collection.find(query);
		return createTrajectories(trajectoryClazz, pointClazz, cursor);
	}
	
	@SuppressWarnings({ "rawtypes" })
	public List findAll(String country, Class<? extends ITrajectory> trajectoryClazz, Class<? extends IPoint> pointClazz) throws Exception {
		DBCollection collection = this.getDatabase();
		BasicDBObject general = new BasicDBObject();
		general.put("country", country);
		DBCursor cursor = collection.find(general);
		return createTrajectories(trajectoryClazz, pointClazz, cursor);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List<ITrajectory> createTrajectories(Class<? extends ITrajectory> trajectoryClazz, Class<? extends IPoint> pointClazz, DBCursor cursor) throws InstantiationException, IllegalAccessException {
		List<ITrajectory> trajectoriesFound = new ArrayList<ITrajectory>();
        int id = 0;
        int pId = 0;
		while (cursor.hasNext()) {
			DBObject object = cursor.next();
			ITrajectory t = trajectoryClazz.newInstance();
			t.setId(id++);
			t.setName(String.valueOf(object.get("name")));
			t.setCountry(String.valueOf(object.get("country")));
			t.setState(String.valueOf(object.get("state")));
			t.setCity(String.valueOf(object.get("city")));
			t.setStartHour((int) object.get("start_hour"));
			t.setStartMinute((int) object.get("start_minute"));
			BasicDBList coordinates = (BasicDBList) object.get("points");
			for(int i = 0; i < coordinates.size(); i++) {
				BasicDBList coordinate = (BasicDBList) coordinates.get(i);
				IPoint p = pointClazz.newInstance();
				p.setId(pId++);
				p.setLng((double) coordinate.get(0));
				p.setLat((double) coordinate.get(1));
				p.setTimestamp((long) coordinate.get(2));
				p.setTrajectory(t);
				t.getPoints().add(p);
			}
			t.sortPoints();
			trajectoriesFound.add(t);
		}
		return trajectoriesFound;
	}
	
	private BasicDBObject createRegionQuery(IGrid grid) {
		BasicDBObject root = new BasicDBObject();
		BasicDBObject trajectory = new BasicDBObject();
		BasicDBObject geoIntersect = new BasicDBObject();
		BasicDBObject geometry = new BasicDBObject();
		geometry.put("type", "LineString");
		BasicDBList coordinates = new BasicDBList();
		
		BasicDBList p1 = new BasicDBList();
		p1.add(grid.getLngMin());
		p1.add(grid.getLatMin());
		
		BasicDBList p2 = new BasicDBList();
		p2.add(grid.getLngMin());
		p2.add(grid.getLatMax());
		
		BasicDBList p3 = new BasicDBList();
		p3.add(grid.getLngMax());
		p3.add(grid.getLatMax());
		
		BasicDBList p4 = new BasicDBList();
		p4.add(grid.getLngMax());
		p4.add(grid.getLatMin());
		
		BasicDBList p5 = new BasicDBList();
		p5.add(grid.getLngMin());
		p5.add(grid.getLatMin());

		coordinates.add(p1);
		coordinates.add(p2);
		coordinates.add(p3);
		coordinates.add(p4);
		coordinates.add(p5);
		
		geometry.put("coordinates", coordinates);
		geoIntersect.put("$geometry", geometry);
		trajectory.put("$geoIntersects", geoIntersect);
		root.put("trajectory", trajectory);
		return root;
	}
	
}
