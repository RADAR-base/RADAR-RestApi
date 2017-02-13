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
import org.radarcns.avro.restapi.app.Application;
import org.radarcns.dao.mongo.AndroidDAO;
import org.radarcns.security.Param;
import org.radarcns.util.ResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Francesco Nobilia on 18/10/2016.
 */
@Api
@Path("/Android")
public class AppStatusApp {

    private static Logger logger = LoggerFactory.getLogger(AppStatusApp.class);

    @Context private ServletContext context;
    @Context private HttpServletRequest request;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/Status/{userID}/{sourceID}")
    @ApiOperation(
        value = "Return an Applications status",
        notes = "The Android application periodically updates its current status")
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "No value for the given parameters, in the body" +
            "there is a message.avsc object with more details"),
        @ApiResponse(code = 200, message = "Return a application.avsc object containing last"
            + "received status")})
    public Response getRTStatByUserDevice(
        @PathParam("userID") String userID,
        @PathParam("sourceID") String sourceID) {
        try {
            Param.isValidInput(userID, sourceID);

            Application application = AndroidDAO.getInstance().getStatus(userID, sourceID, context);

            return ResponseHandler.getJsonResponse(request, application);
        }
        catch (Exception e){
            logger.error(e.getMessage(), e);
            return ResponseHandler.getJsonErrorResponse(request, "Your request cannot be completed. If this error persists, please contact the service administrator.");
        }
    }
}
