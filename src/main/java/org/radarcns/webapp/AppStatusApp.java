package org.radarcns.webapp;

import com.mongodb.MongoClient;
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
import org.radarcns.dao.mongo.util.MongoHelper;
import org.radarcns.security.Param;
import org.radarcns.util.ResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Android application status web-app. Function set to access Android app status information.
 */
@Api
@Path("/android")
public class AppStatusApp {

    private static Logger logger = LoggerFactory.getLogger(AppStatusApp.class);

    @Context private ServletContext context;
    @Context private HttpServletRequest request;

    //--------------------------------------------------------------------------------------------//
    //                                    REAL-TIME FUNCTIONS                                     //
    //--------------------------------------------------------------------------------------------//
    /**
     * JSON function that returns the status app of the given user.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/status/{userID}/{sourceID}")
    @ApiOperation(
            value = "Return an Applications status",
            notes = "The Android application periodically updates its current status")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "An error occurs while executing, in the body"
                + "there is a message.avsc object with more details"),
            @ApiResponse(code = 204, message = "No value for the given parameters, in the body"
                + "there is a message.avsc object with more details"),
            @ApiResponse(code = 200, message = "Return a application.avsc object containing last"
                + "received status")})
    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    public Response getRTStatByUserDeviceJsonAppStatus(
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
     * AVRO function that returns the status app of the given user.
     */
    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Path("/avro/status/{userID}/{sourceID}")
    @ApiOperation(
            value = "Return an Applications status",
            notes = "The Android application periodically updates its current status")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "An error occurs while executing"),
            @ApiResponse(code = 204, message = "No value for the given parameters"),
            @ApiResponse(code = 200, message = "Return a application.avsc object containing last"
                + "received status")})
    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    public Response getRTStatByUserDeviceAvroAppStatus(
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
     * Actual implementation of AVRO and JSON getRealTimeUser.
     **/
    private Application getRTStatByUserDeviceWorker(String user, String source)
            throws ConnectException {
        Param.isValidInput(user, source);

        MongoClient client = MongoHelper.getClient(context);

        Application application = AndroidDAO.getInstance().getStatus(user, source, client);

        return application;
    }
}
