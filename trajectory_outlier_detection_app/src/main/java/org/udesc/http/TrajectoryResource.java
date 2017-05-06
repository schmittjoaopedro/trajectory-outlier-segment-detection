package org.udesc.http;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.udesc.trajectory.TODS.TODSRequest;
import org.udesc.trajectory.TODS.TODSResult;
import org.udesc.trajectory.TODS.TODS;
import org.udesc.trajectory.TRASOD.TRASOD;
import org.udesc.trajectory.TRASOD.TRASODRequest;
import org.udesc.trajectory.TRASOD.TRASODResult;

@Path("/resources/trajectory")
public class TrajectoryResource {

	private TODS tods;
	
	private TRASOD trasod;
	
	public TrajectoryResource() {
		super();
		tods = new TODS();
		trasod = new TRASOD();
	}
	
	@POST
	@Path("/process/TODS")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public TODSResult processTODS(TODSRequest request) throws Exception {
		return tods.run(request);
	}
	
	@POST
	@Path("/process/TRASOD")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public TRASODResult processTRASOD(TRASODRequest request) throws Exception {
		return trasod.run(request);
	}
	
}
