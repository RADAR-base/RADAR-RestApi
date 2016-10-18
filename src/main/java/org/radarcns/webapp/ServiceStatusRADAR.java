package org.radarcns.webapp;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

/**
 * Created by Francesco Nobilia on 18/10/2016.
 */
@Path("/status")
public class ServiceStatusRADAR {

    @GET
    public Response getMessage() {
        String status = "<!DOCTYPE html><html><body><svg width=\"100%\" height=\"50\">" +
                "<rect width=\"100%\" height=\"100%\" style=\"fill:rgb(124,252,0);" +
                "stroke-width:1;stroke:rgb(0,0,0)\"/><text x=\"45%\" y=\"60%\" " +
                "fill=\"white\">STATUS OK!</text></svg></body></html>";

        return Response.status(200).entity(status).build();
    }
}
