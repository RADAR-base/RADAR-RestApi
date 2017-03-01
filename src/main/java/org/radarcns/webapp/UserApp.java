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
import org.radarcns.avro.restapi.user.Cohort;
import org.radarcns.avro.restapi.user.Patient;
import org.radarcns.dao.mongo.UserDAO;
import org.radarcns.util.ResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User web-app. Function set to access users information.
 */
@Api
@Path("/User")
public class UserApp {

    private static Logger logger = LoggerFactory.getLogger(UserApp.class);

    @Context private ServletContext context;
    @Context private HttpServletRequest request;

    //--------------------------------------------------------------------------------------------//
    //                                        ALL PATIENTS                                        //
    //--------------------------------------------------------------------------------------------//
    /**
     * JSON function that returns all available patient.
     */
    //TODO add the studyID param
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/GetAllPatients")
    @ApiOperation(
            value = "Return a list of users",
            notes = "Each user can have multiple sourceID associated with him")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "An error occurs while executing, in the body"
                + "there is a message.avsc object with more details"),
            @ApiResponse(code = 204, message = "No value for the given parameters, in the body"
                + "there is a message.avsc object with more details"),
            @ApiResponse(code = 200, message = "Return a list of user.avsc objects")})
    public Response getAllPatientsJsonUser() {
        try {
            return ResponseHandler.getJsonResponse(request, getAllPatientsWorker());
        } catch (Exception exec) {
            logger.error(exec.getMessage(), exec);
            return ResponseHandler.getJsonErrorResponse(request, "Your request cannot be"
                + "completed. If this error persists, please contact the service administrator.");
        }
    }

    /**
     * AVRO function that returns all available patient.
     */
    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Path("/AVRO/GetAllPatients")
    @ApiOperation(
            value = "Return a list of users",
            notes = "Each user can have multiple sourceID associated with him")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "An error occurs while executing"),
            @ApiResponse(code = 204, message = "No value for the given parameters"),
            @ApiResponse(code = 200, message = "Return a byte array serialising a list of"
                + "user.avsc objects")})
    public Response getAllPatientsAvroUser() {
        try {
            return ResponseHandler.getAvroResponse(request, getAllPatientsWorker());
        } catch (Exception exec) {
            logger.error(exec.getMessage(), exec);
            return ResponseHandler.getAvroErrorResponse(request);
        }
    }

    /**
     * Actual implementation of AVRO and JSON getAllPatients.
     **/
    private Cohort getAllPatientsWorker() throws ConnectException {
        Cohort cohort = UserDAO.findAllUsers(context);

        return cohort;
    }

    //--------------------------------------------------------------------------------------------//
    //                                         ALL SOURCES                                        //
    //--------------------------------------------------------------------------------------------//
    /**
     * JSON function that returns all known sources for the given user.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/GetAllSources/{userID}")
    @ApiOperation(
            value = "Return a User value",
            notes = "Return all known sources associated with the give userID")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "An error occurs while executing, in the body"
                + "there is a message.avsc object with more details"),
            @ApiResponse(code = 204, message = "No value for the given parameters, in the body"
                + "there is a message.avsc object with more details"),
            @ApiResponse(code = 200, message = "Return a user.avsc object")})
    public Response getAllSourcesJsonUser(
            @PathParam("userID") String user
    ) {
        try {
            return ResponseHandler.getJsonResponse(request, getAllSourcesWorker(user));
        } catch (Exception exec) {
            logger.error(exec.getMessage(), exec);
            return ResponseHandler.getJsonErrorResponse(request, "Your request cannot be"
                + "completed. If this error persists, please contact the service administrator.");
        }
    }

    /**
     * AVRO function that returns all known sources for the given user.
     */
    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Path("/AVRO/GetAllSources/{userID}")
    @ApiOperation(
            value = "Return a User value",
            notes = "Return all known sources associated with the give userID")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "An error occurs while executing"),
            @ApiResponse(code = 204, message = "No value for the given parameters"),
            @ApiResponse(code = 200, message = "Return a user.avsc object")})
    public Response getAllSourcesAvroUser(
            @PathParam("userID") String user
    ) {
        try {
            return ResponseHandler.getAvroResponse(request, getAllSourcesWorker(user));
        } catch (Exception exec) {
            logger.error(exec.getMessage(), exec);
            return ResponseHandler.getAvroErrorResponse(request);
        }
    }

    /**
     * Actual implementation of AVRO and JSON getAllSources.
     **/
    private Patient getAllSourcesWorker(String user) throws ConnectException {
        Patient patient = UserDAO.findAllSoucesByUser(user, context);

        return patient;
    }
}
