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
import org.radarcns.avro.restapi.dataset.Dataset;
import org.radarcns.avro.restapi.header.Unit;
import org.radarcns.dao.mongo.TemperatureDAO;
import org.radarcns.dao.mongo.util.MongoHelper;
import org.radarcns.security.Param;
import org.radarcns.util.ResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Temperature sensor web-app
 */
@Api
@Path("/T")
public class TemperatureApp {

    private static Logger logger = LoggerFactory.getLogger(TemperatureApp.class);

    private final String sensorName = "temperature";

    @Context private ServletContext context;
    @Context private HttpServletRequest request;

    /********************************************* RT *********************************************/
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/RT/{stat}/{userID}/{sourceID}")
    @ApiOperation(
        value = "Return an Temperature values",
        notes = "Return the last seen Temperature value of type stat for the given userID and"
                + "sourceID")
    @ApiResponses(value = {
        @ApiResponse(code = 500, message = "An error occurs while executing, in the body" +
            "there is a message.avsc object with more details"),
        @ApiResponse(code = 204, message = "No value for the given parameters, in the body" +
                "there is a message.avsc object with more details"),
        @ApiResponse(code = 200, message = "Return a dataset.avsc object containing last seen"
            + "temperature.avsc value for the required statistic function")})
    public Response getRealTimeUserJsonTemperature(
            @PathParam("userID") String userID,
            @PathParam("sourceID") String sourceID,
            @PathParam("stat") MongoHelper.Stat stat) {
        try {
            return ResponseHandler.getJsonResponse(request,
                getRealTimeUserWorker(userID, sourceID, stat), sensorName);
        }
        catch (Exception e){
            logger.error(e.getMessage(), e);
            return ResponseHandler.getJsonErrorResponse(request, "Your request cannot be"
                + "completed. If this error persists, please contact the service administrator.");
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Path("/AVRO/RT/{stat}/{userID}/{sourceID}")
    @ApiOperation(
        value = "Return an Temperature values",
        notes = "Return the last seen Temperature value of type stat for the given userID and"
            + "sourceID")
    @ApiResponses(value = {
        @ApiResponse(code = 500, message = "An error occurs while executing"),
        @ApiResponse(code = 204, message = "No value for the given parameters"),
        @ApiResponse(code = 200, message = "Return a byte array serialising a dataset.avsc object"
            + "containing last seen temperature.avsc value for the required statistic function")})
    public Response getRealTimeUserAvroTemperature(
        @PathParam("userID") String userID,
        @PathParam("sourceID") String sourceID,
        @PathParam("stat") MongoHelper.Stat stat) {
        try {
            return ResponseHandler.getAvroResponse(request,
                getRealTimeUserWorker(userID, sourceID, stat));
        }
        catch (Exception e){
            logger.error(e.getMessage(), e);
            return ResponseHandler.getAvroErrorResponse(request);
        }
    }

    /**
     * Actual implementation of AVRO and JSON getRealTimeUser
     **/
    private Dataset getRealTimeUserWorker(String userID, String sourceID, MongoHelper.Stat stat)
        throws ConnectException {
        Param.isValidInput(userID, sourceID);

        Dataset temp = TemperatureDAO.getInstance().valueRTByUserSource(userID, sourceID,
            Unit.celsius, stat, context);

        if (temp.getDataset().isEmpty()) {
            logger.info("No data for the user {} with source {}", userID, sourceID);
        }

        return temp;
    }

    /*************************************** WHOLE DATASET ****************************************/
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{stat}/{userID}/{sourceID}")
    @ApiOperation(
        value = "Return a dataset of Temperature values",
        notes = "Return a dataset for the given userID and sourceID of type stat")
    @ApiResponses(value = {
        @ApiResponse(code = 500, message = "An error occurs while executing, in the body" +
            "there is a message.avsc object with more details"),
        @ApiResponse(code = 204, message = "No value for the given parameters, in the body" +
                "there is a message.avsc object with more details"),
        @ApiResponse(code = 200, message = "Return a dataset.avsc object containing all"
            + "available temperature.avsc values for the required statistic function")})
    public Response getAllByUserJsonTemperature(
            @PathParam("userID") String userID,
            @PathParam("sourceID") String sourceID,
            @PathParam("stat") MongoHelper.Stat stat) {
        try {
            return ResponseHandler.getJsonResponse(request,
                getAllByUserWorker(userID, sourceID, stat), sensorName);
        }
        catch (Exception e){
            logger.error(e.getMessage(), e);
            return ResponseHandler.getJsonErrorResponse(request, "Your request cannot be"
                + "completed. If this error persists, please contact the service administrator.");
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Path("/AVRO/{stat}/{userID}/{sourceID}")
    @ApiOperation(
        value = "Return a dataset of Temperature values",
        notes = "Return a dataset for the given userID and sourceID of type stat")
    @ApiResponses(value = {
        @ApiResponse(code = 500, message = "An error occurs while executing"),
        @ApiResponse(code = 204, message = "No value for the given parameters"),
        @ApiResponse(code = 200, message = "Return a byte array serialising a dataset.avsc object"
            + "containing all available temperature.avsc values for the required statistic"
            + "function")})
    public Response getAllByUserAvroTemperature(
        @PathParam("userID") String userID,
        @PathParam("sourceID") String sourceID,
        @PathParam("stat") MongoHelper.Stat stat) {
        try {
            return ResponseHandler.getAvroResponse(request,
                getAllByUserWorker(userID, sourceID, stat));
        }
        catch (Exception e){
            logger.error(e.getMessage(), e);
            return ResponseHandler.getAvroErrorResponse(request);
        }
    }

    /**
     * Actual implementation of AVRO and JSON getAllByUser
     **/
    private Dataset getAllByUserWorker( String userID, String sourceID, MongoHelper.Stat stat )
        throws ConnectException {
        Param.isValidInput(userID, sourceID);

        Dataset temp = TemperatureDAO.getInstance().valueByUserSource(userID, sourceID,
            Unit.celsius, stat, context);

        if (temp.getDataset().isEmpty()) {
            logger.info("No data for the user {} with source {}", userID, sourceID);
        }

        return temp;
    }

    /************************************* WINDOWED DATASET ***************************************/
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{stat}/{userID}/{sourceID}/{start}/{end}")
    @ApiOperation(
        value = "Return a dataset of Temperature values",
        notes = "Return a dataset of type stat for the given userID and sourceID with data"
                + "belonging to the time window [start - end]")
    @ApiResponses(value = {
        @ApiResponse(code = 500, message = "An error occurs while executing, in the body" +
            "there is a message.avsc object with more details"),
        @ApiResponse(code = 204, message = "No value for the given parameters, in the body" +
                "there is a message.avsc object with more details"),
        @ApiResponse(code = 200, message = "Return a dataset.avsc object containing all"
            + "temperature.avsc values belonging to the time window [start - end] for the"
            + "required statistic function")})
    public Response getByUserForWindowJsonTemperature(
            @PathParam("userID") String userID,
            @PathParam("sourceID") String sourceID,
            @PathParam("stat") MongoHelper.Stat stat,
            @PathParam("start") long start,
            @PathParam("end") long end) {
        try {
            return ResponseHandler.getJsonResponse(request,
                getByUserForWindowWorker(userID, sourceID, stat, start, end), sensorName);
        }
        catch (Exception e){
            logger.error(e.getMessage(), e);
            return ResponseHandler.getJsonErrorResponse(request, "Your request cannot be"
                + "completed. If this error persists, please contact the service administrator.");
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Path("/{stat}/{userID}/{sourceID}/{start}/{end}")
    @ApiOperation(
        value = "Return a dataset of Temperature values",
        notes = "Return a dataset of type stat for the given userID and sourceID with data"
            + "belonging to the time window [start - end]")
    @ApiResponses(value = {
        @ApiResponse(code = 500, message = "An error occurs while executing"),
        @ApiResponse(code = 204, message = "No value for the given parameters"),
        @ApiResponse(code = 200, message = "Return a byte array serialising a dataset.avsc object"
            + "containing all temperature.avsc values belonging to the time window [start - end]"
            + "for the required statistic function")})
    public Response getByUserForWindowAvroTemperature(
        @PathParam("userID") String userID,
        @PathParam("sourceID") String sourceID,
        @PathParam("stat") MongoHelper.Stat stat,
        @PathParam("start") long start,
        @PathParam("end") long end) {
        try {
            return ResponseHandler.getAvroResponse(request,
                getByUserForWindowWorker(userID, sourceID, stat, start, end));
        }
        catch (Exception e){
            logger.error(e.getMessage(), e);
            return ResponseHandler.getAvroErrorResponse(request);
        }
    }

    /**
     * Actual implementation of AVRO and JSON getByUserForWindow
     **/
    private Dataset getByUserForWindowWorker(String userID, String sourceID, MongoHelper.Stat stat,
        long start, long end) throws ConnectException {
        Param.isValidInput(userID, sourceID);

        Dataset temp = TemperatureDAO.getInstance().valueByUserSourceWindow(userID, sourceID,
            Unit.celsius, stat, start, end, context);

        if (temp.getDataset().isEmpty()) {
            logger.info("No data for the user {} with source {}", userID, sourceID);
        }

        return temp;
    }

}
