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
import org.radarcns.avro.restapi.user.Cohort;
import org.radarcns.avro.restapi.user.Patient;
import org.radarcns.dao.mongo.UserDAO;
import org.radarcns.util.ResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Francesco Nobilia on 18/10/2016.
 */
@Api
@Path("/User")
public class UserApp {

    private static Logger logger = LoggerFactory.getLogger(UserApp.class);

    @Context private ServletContext context;
    @Context private HttpServletRequest request;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/GetAllPatients")
    @ApiOperation(
        value = "Return a list of users",
        notes = "Each user can have multiple sourceID associated with him")
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "No value for the given parameters, in the body" +
            "there is a message.avsc object with more details"),
        @ApiResponse(code = 200, message = "Return a list of user.avsc objects")})
    public Response getAllPatients(){
        try {
            Cohort cohort = UserDAO.findAllUser(context);

            return ResponseHandler.getJsonResponse(request, cohort);
        }
        catch (Exception e){
            logger.error(e.getMessage(), e);
            return ResponseHandler.getJsonErrorResponse(request, "Your request cannot be completed. If this error persists, please contact the service administrator.");
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/GetAllSources/{userID}")
    @ApiOperation(
        value = "Return a User value",
        notes = "Return all known sources associated with the give userID")
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "No value for the given parameters, in the body" +
            "there is a message.avsc object with more details"),
        @ApiResponse(code = 200, message = "Return a user.avsc object")})
    public Response getAllSources(
        @PathParam("userID") String userID
    ){
        try {
            Patient patient = UserDAO.findAllSoucesByUser(userID, context);

            return ResponseHandler.getJsonResponse(request, patient);
        }
        catch (Exception e){
            logger.error(e.getMessage(), e);
            return ResponseHandler.getJsonErrorResponse(request, "Your request cannot be completed. If this error persists, please contact the service administrator.");
        }
    }
}
