package org.radarcns.webapp;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.net.ConnectException;
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
 * Device status web-app. Function set to access device status information.
 */
@Api
@Path("/Device")
public class DeviceStatusApp {

    private static Logger logger = LoggerFactory.getLogger(DeviceStatusApp.class);

    @Context private ServletContext context;
    @Context private HttpServletRequest request;

    //--------------------------------------------------------------------------------------------//
    //                                    REAL-TIME FUNCTIONS                                     //
    //--------------------------------------------------------------------------------------------//
    /**
     * JSON function that returns the status of the given sensor.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/Status/{userID}/{sourceID}")
    @ApiOperation(
            value = "Return a Device values",
            notes = "Using the device sensors values arrived within last 60sec, it computes the"
                + "sender status for the given userID and sourceID")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "An error occurs while executing, in the body"
                + "there is a message.avsc object with more details"),
            @ApiResponse(code = 204, message = "No value for the given parameters, in the body"
                + "there is a message.avsc object with more details"),
            @ApiResponse(code = 200, message = "Return a device.avsc object containing last"
                + "computed status")})
    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    public Response getRTStatByUserDeviceJsonDevStatus(
            @PathParam("userID") String user,
            @PathParam("sourceID") String source) {
        try {
            return ResponseHandler.getJsonResponse(request,
                getRTStatByUserDeviceWorker(user, source));
        } catch (Exception exec) {
            logger.error(exec.getMessage(), exec);
            return ResponseHandler.getJsonErrorResponse(request, "Your request cannot be"
                + "completed. If this error persists, please contact the service administrator.");
        }
    }

    /**
     * AVRO function that returns the status of the given sensor.
     */
    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Path("/AVRO/Status/{userID}/{sourceID}")
    @ApiOperation(
            value = "Return a Device values",
            notes = "Using the device sensors values arrived within last 60sec, it computes the"
                + "sender status for the given userID and sourceID")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "An error occurs while executing"),
            @ApiResponse(code = 204, message = "No value for the given parameters"),
            @ApiResponse(code = 200, message = "Return a byte array serialising device.avsc object"
                + "containing last computed status")})
    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    public Response getRTStatByUserDeviceAvroDevStatus(
            @PathParam("userID") String user,
            @PathParam("sourceID") String source) {
        try {
            return ResponseHandler.getAvroResponse(request,
                getRTStatByUserDeviceWorker(user, source));
        } catch (Exception exec) {
            logger.error(exec.getMessage(), exec);
            return ResponseHandler.getAvroErrorResponse(request);
        }
    }

    /**
     * Actual implementation of AVRO and JSON getRTStatByUserDevice.
     **/
    private Device getRTStatByUserDeviceWorker(String user, String source)
        throws ConnectException {
        Param.isValidInput(user, source);

        Device device = Empatica.monitor(user, source, context);

        return device;
    }
}
