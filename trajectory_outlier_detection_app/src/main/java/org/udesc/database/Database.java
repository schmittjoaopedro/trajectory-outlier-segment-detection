package org.udesc.database;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

public abstract class Database {

	private static final String RESOURCE_NAME = "db.properties";
	
	private Properties properties;
	
	private MongoClient mongoClient;

	public Database() {
		super();
		try {
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			properties = new Properties();
			try (InputStream resourceStream = loader.getResourceAsStream(RESOURCE_NAME)) {
				properties.load(resourceStream);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(1);
		}
	}
	
	protected Connection getConn() throws Exception {
		Class.forName("org.postgresql.Driver");
		return DriverManager.getConnection(properties.getProperty("url"), properties.getProperty("user"), properties.getProperty("password"));
	}
	
	@SuppressWarnings("deprecation")
	protected DBCollection getDatabase() {
		if(this.mongoClient == null) {
			this.mongoClient = new MongoClient("localhost", 27017);
		}
		return this.mongoClient.getDB("udesc").getCollection("trajectories");
	}
	
}
