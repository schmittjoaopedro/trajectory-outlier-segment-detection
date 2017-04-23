package org.udesc.database;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public abstract class Database {

	private static final String RESOURCE_NAME = "db.properties";
	
	private Properties properties;

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

}
