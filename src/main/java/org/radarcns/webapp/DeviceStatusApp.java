package org.radarcns.webapp;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.radarcns.avro.restapi.device.Device;
import org.radarcns.monitor.Empatica;
import org.radarcns.security.Param;
import org.radarcns.util.ResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Francesco Nobilia on 18/10/2016.
 */
@Api
@Path("/Device")
public class DeviceStatusApp {

    private static Logger logger = LoggerFactory.getLogger(DeviceStatusApp.class);

    @Context private ServletContext context;
    @Context private HttpServletRequest request;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/Status/{userID}/{sourceID}")
    @ApiOperation(
            value = "Return a Device values",
            notes = "Using the last seen values of each device sensors, it computes the sender"
                + "status for the given userID and sourceID")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "No value for the given parameters, in the body" +
                    "there is a message.avsc object with more details"),
            @ApiResponse(code = 200, message = "Return a device.avsc object containing last"
                + "computed status")})
    public Response getRTStatByUserDevice(
            @PathParam("userID") String userID,
            @PathParam("sourceID") String sourceID) {
        try {
            Param.isValidInput(userID, sourceID);

            Device device = Empatica.monitor(userID, sourceID, context);

            return ResponseHandler.getJsonResponse(request, device);
        }
        catch (Exception e){
            logger.error(e.getMessage(), e);
            return ResponseHandler.getJsonErrorResponse(request, "Your request cannot be completed. If this error persists, please contact the service administrator.");
        }
    }
}
