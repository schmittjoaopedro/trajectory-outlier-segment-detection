package org.udesc.http;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.udesc.trajectory.CalculationRequest;
import org.udesc.trajectory.CalculationResult;
import org.udesc.trajectory.TODS;

@Path("/resources/trajectory")
public class TrajectoryResource {

	private TODS tods;
	
	public TrajectoryResource() {
		super();
		tods = new TODS();
	}
	
	@POST
	@Path("/process")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public CalculationResult process(CalculationRequest request) throws Exception {
		return tods.run(request);
	}
	
}
