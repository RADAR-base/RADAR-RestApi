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
import org.radarcns.avro.restapi.app.Application;
import org.radarcns.dao.mongo.AndroidDAO;
import org.radarcns.security.Param;
import org.radarcns.util.AvroConverter;
import org.radarcns.util.ResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Android Application Status web-app
 */
@Api
@Path("/Android")
public class AppStatusApp {

    private static Logger logger = LoggerFactory.getLogger(AppStatusApp.class);

    @Context private ServletContext context;
    @Context private HttpServletRequest request;

    /******************************************* STATUS *******************************************/
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/Status/{userID}/{sourceID}")
    @ApiOperation(
        value = "Return an Applications status",
        notes = "The Android application periodically updates its current status")
    @ApiResponses(value = {
        @ApiResponse(code = 500, message = "An error occurs while executing, in the body" +
            "there is a message.avsc object with more details"),
        @ApiResponse(code = 204, message = "No value for the given parameters, in the body" +
            "there is a message.avsc object with more details"),
        @ApiResponse(code = 200, message = "Return a application.avsc object containing last"
            + "received status")})
    public Response getRTStatByUserDeviceJson(
        @PathParam("userID") String userID,
        @PathParam("sourceID") String sourceID) {
        try {
            return ResponseHandler.getJsonResponse(request,
                getRTStatByUserDeviceWorker(userID, sourceID));
        }
        catch (Exception e){
            logger.error(e.getMessage(), e);
            return ResponseHandler.getJsonErrorResponse(request, "Your request cannot be"
                + "completed. If this error persists, please contact the service administrator.");
        }
    }

    @GET
    @Produces(AvroConverter.MEDIA_TYPE)
    @Path("/AVRO/Status/{userID}/{sourceID}")
    @ApiOperation(
        value = "Return an Applications status",
        notes = "The Android application periodically updates its current status")
    @ApiResponses(value = {
        @ApiResponse(code = 500, message = "An error occurs while executing, in the body" +
            "there is a a byte array serialising a message.avsc object with more details"),
        @ApiResponse(code = 204, message = "No value for the given parameters, in the body" +
            "there is a a byte array serialising a message.avsc object with more details"),
        @ApiResponse(code = 200, message = "Return a application.avsc object containing last"
            + "received status")})
    public Response getRTStatByUserDeviceAvro(
        @PathParam("userID") String userID,
        @PathParam("sourceID") String sourceID) {
        try {
            return ResponseHandler.getAvroResponse(request,
                getRTStatByUserDeviceWorker(userID, sourceID));
        }
        catch (Exception e){
            logger.error(e.getMessage(), e);
            return ResponseHandler.getAvroErrorResponse(request, "Your request cannot be"
                + "completed. If this error persists, please contact the service administrator.");
        }
    }

    /**
     * Actual implementation of AVRO and JSON getRealTimeUser
     **/
    private Application getRTStatByUserDeviceWorker(String userID, String sourceID)
        throws ConnectException {
        Param.isValidInput(userID, sourceID);

        Application application = AndroidDAO.getInstance().getStatus(userID, sourceID, context);

        return application;
    }
}
