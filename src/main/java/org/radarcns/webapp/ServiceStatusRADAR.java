package org.radarcns.webapp;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ResponseHeader;

/**
 * Created by Francesco Nobilia on 18/10/2016.
 */
@Api
@Path("/status")
public class ServiceStatusRADAR {

    @GET
    @ApiOperation(
            value = "Get Status",
            notes = "Check and return the healthy of each architecture components")
    @ApiResponses(value = {
            @ApiResponse(code = 503, message = "Service not available"),
            @ApiResponse(code = 200, message = "Service available")})
    public Response getStatus() {
        String status = "<!DOCTYPE html><html><body><svg width=\"100%\" height=\"50\">" +
                "<rect width=\"100%\" height=\"100%\" style=\"fill:rgb(124,252,0);" +
                "stroke-width:1;stroke:rgb(0,0,0)\"/><text x=\"45%\" y=\"60%\" " +
                "fill=\"white\">STATUS OK!</text></svg></body></html>";

        return Response.status(200).entity(status).build();
    }
}
