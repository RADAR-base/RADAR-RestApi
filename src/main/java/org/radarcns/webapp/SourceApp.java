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
import org.radarcns.avro.restapi.source.Source;
import org.radarcns.avro.restapi.source.SourceSpecification;
import org.radarcns.avro.restapi.source.SourceType;
import org.radarcns.avro.restapi.user.Patient;
import org.radarcns.dao.mongo.SourceDAO;
import org.radarcns.dao.mongo.util.MongoHelper;
import org.radarcns.monitor.Monitors;
import org.radarcns.security.Param;
import org.radarcns.util.ResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SourceDefinition web-app. Function set to access source information.
 */
@Api
@Path("/source")
public class SourceApp {

    private static Logger logger = LoggerFactory.getLogger(SourceApp.class);

    @Context private ServletContext context;
    @Context private HttpServletRequest request;

    //--------------------------------------------------------------------------------------------//
    //                                       STATE FUNCTIONS                                      //
    //--------------------------------------------------------------------------------------------//
    /**
     * JSON function that returns the status of the given source.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/state/{userID}/{sourceID}")
    @ApiOperation(
            value = "Return a SourceDefinition values",
            notes = "Using the source sensors values arrived within last 60sec, it computes the"
                + "sender status for the given userID and sourceID")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "An error occurs while executing, in the body"
                + "there is a message.avsc object with more details"),
            @ApiResponse(code = 204, message = "No value for the given parameters, in the body"
                + "there is a message.avsc object with more details"),
            @ApiResponse(code = 200, message = "Return a source.avsc object containing last"
                + "computed status")})
    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    public Response getRTStateByUserDeviceJsonDevStatus(
            @PathParam("userID") String user,
            @PathParam("sourceID") String source) {
        try {
            return ResponseHandler.getJsonResponse(request,
                getRTStateByUserSourceWorker(user, source));
        } catch (Exception exec) {
            logger.error(exec.getMessage(), exec);
            return ResponseHandler.getJsonErrorResponse(request, "Your request cannot be"
                + "completed. If this error persists, please contact the service administrator.");
        }
    }

    /**
     * AVRO function that returns the status of the given source.
     */
    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Path("/avro/state/{userID}/{sourceID}")
    @ApiOperation(
            value = "Return a SourceDefinition values",
            notes = "Using the source sensors values arrived within last 60sec, it computes the"
                + "sender status for the given userID and sourceID")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "An error occurs while executing"),
            @ApiResponse(code = 204, message = "No value for the given parameters"),
            @ApiResponse(code = 200, message = "Return a byte array serialising source.avsc object"
                + "containing last computed status")})
    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    public Response getRTStateByUserDeviceAvroDevStatus(
            @PathParam("userID") String user,
            @PathParam("sourceID") String source) {
        try {
            return ResponseHandler.getAvroResponse(request,
                getRTStateByUserSourceWorker(user, source));
        } catch (Exception exec) {
            logger.error(exec.getMessage(), exec);
            return ResponseHandler.getAvroErrorResponse(request);
        }
    }

    /**
     * Actual implementation of AVRO and JSON getRTStateByUserDevice.
     **/
    private Source getRTStateByUserSourceWorker(String user, String source)
        throws ConnectException {
        Param.isValidInput(user, source);

        MongoClient client = MongoHelper.getClient(context);

        SourceType sourceType = SourceDAO.getSourceType(source, client);

        if (sourceType == null) {
            return null;
        }

        Source device = Monitors.getInstance().getState(user, source, sourceType, client);

        return device;
    }

    //--------------------------------------------------------------------------------------------//
    //                               SOURCE SPECIFICATION FUNCTIONS                               //
    //--------------------------------------------------------------------------------------------//
    /**
     * JSON function that returns the specification of the given source.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/specification/{sourceType}")
    @ApiOperation(
            value = "Return a SourceDefinition specification",
            notes = "Return the sensor specification of all on-board sensors for the given"
                + "source type")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "An error occurs while executing, in the body"
                + "there is a message.avsc object with more details"),
            @ApiResponse(code = 204, message = "No value for the given parameters, in the body"
                + "there is a message.avsc object with more details"),
            @ApiResponse(code = 200, message = "Return a source_specification.avsc object"
                + "containing last computed status")})
    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    public Response getSourceSpecificationJson(
            @PathParam("sourceType") SourceType source) {
        try {
            return ResponseHandler.getJsonResponse(request, getSourceSpecificationWorker(source));
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
    @Path("/avro/specification/{sourceType}")
    @ApiOperation(
            value = "Return a SourceDefinition specification",
            notes = "Return the sensor specification of all on-board sensors for the given"
                + "source type")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "An error occurs while executing"),
            @ApiResponse(code = 204, message = "No value for the given parameters"),
            @ApiResponse(code = 200, message = "Return a source_specification.avsc object"
                + "containing last computed status")})
    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    public Response getSourceSpecificationAvro(
            @PathParam("sourceType") SourceType source) {
        try {
            return ResponseHandler.getAvroResponse(request, getSourceSpecificationWorker(source));
        } catch (Exception exec) {
            logger.error(exec.getMessage(), exec);
            return ResponseHandler.getAvroErrorResponse(request);
        }
    }

    /**
     * Actual implementation of AVRO and JSON getSpecification.
     **/
    private SourceSpecification getSourceSpecificationWorker(SourceType source)
            throws ConnectException {
        SourceSpecification device = Monitors.getInstance().getSpecification(source);

        return device;
    }

    //--------------------------------------------------------------------------------------------//
    //                                         ALL SOURCES                                        //
    //--------------------------------------------------------------------------------------------//
    /**
     * JSON function that returns all known sources for the given user.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/getAllSources/{userID}")
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
            @PathParam("userID") String user) {
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
    @Path("/avro/getAllSources/{userID}")
    @ApiOperation(
            value = "Return a User value",
            notes = "Return all known sources associated with the give userID")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "An error occurs while executing"),
            @ApiResponse(code = 204, message = "No value for the given parameters"),
            @ApiResponse(code = 200, message = "Return a user.avsc object")})
    public Response getAllSourcesAvroUser(
            @PathParam("userID") String user) {
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
        Param.isValidUser(user);

        MongoClient client = MongoHelper.getClient(context);

        Patient patient = SourceDAO.findAllSoucesByUser(user, client);

        return patient;
    }
}
