package org.udesc.http;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

public class TrajectoryApplication extends Application {

	private Set<Object> singletons = new HashSet<Object>();
	
	public TrajectoryApplication() {
		singletons.add(new TrajectoryResource());
	}

	@Override
	public Set<Object> getSingletons() {
		return singletons;
	}
	
}
