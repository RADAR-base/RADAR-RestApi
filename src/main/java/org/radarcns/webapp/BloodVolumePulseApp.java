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
import org.radarcns.dao.mongo.BloodVolumePulseDAO;
import org.radarcns.dao.mongo.util.MongoHelper;
import org.radarcns.security.Param;
import org.radarcns.util.AvroConverter;
import org.radarcns.util.ResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Photoplethysmograph sensor web-app
 */
@Api
@Path("/BVP")
public class BloodVolumePulseApp {

    private static Logger logger = LoggerFactory.getLogger(BloodVolumePulseApp.class);

    private final String sensorName = "blood_volume_pulse";

    @Context private ServletContext context;
    @Context private HttpServletRequest request;

    /********************************************* RT *********************************************/
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/RT/{stat}/{userID}/{sourceID}")
    @ApiOperation(
        value = "Return a BloodVolumePulse level values",
        notes = "Return the last seen BloodVolumePulse level value of type stat for the given"
                + "userID and sourceID")
    @ApiResponses(value = {
        @ApiResponse(code = 500, message = "An error occurs while executing, in the body" +
            "there is a message.avsc object with more details"),
        @ApiResponse(code = 204, message = "No value for the given parameters, in the body" +
                "there is a message.avsc object with more details"),
        @ApiResponse(code = 200, message = "Return a dataset.avsc object containing last seen"
            + "blood_volume_pulse.avsc value for the required statistic function")})
    public Response getRealTimeUserJson(
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
    @Produces(AvroConverter.MEDIA_TYPE)
    @Path("/AVRO/RT/{stat}/{userID}/{sourceID}")
    @ApiOperation(
        value = "Return a BloodVolumePulse level values",
        notes = "Return the last seen BloodVolumePulse level value of type stat for the given"
            + "userID and sourceID")
    @ApiResponses(value = {
        @ApiResponse(code = 500, message = "An error occurs while executing, in the body" +
            "there is a a byte array serialising a message.avsc object with more details"),
        @ApiResponse(code = 204, message = "No value for the given parameters, in the body" +
            "there is a a byte array serialising a message.avsc object with more details"),
        @ApiResponse(code = 200, message = "Return a byte array serialising a dataset.avsc object"
            + "containing last seen blood_volume_pulse.avsc value for the required statistic"
            + "function")})
    public Response getRealTimeUserAvro(
        @PathParam("userID") String userID,
        @PathParam("sourceID") String sourceID,
        @PathParam("stat") MongoHelper.Stat stat) {
        try {
            return ResponseHandler.getAvroResponse(request,
                getRealTimeUserWorker(userID, sourceID, stat));
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
    private Dataset getRealTimeUserWorker(String userID, String sourceID, MongoHelper.Stat stat)
        throws ConnectException {
        Param.isValidInput(userID, sourceID);

        Dataset bvp = BloodVolumePulseDAO.getInstance().valueRTByUserSource(userID, sourceID,
            Unit.nW, stat, context);

        if (bvp.getDataset().isEmpty()) {
            logger.info("No data for the user {} with source {}", userID, sourceID);
        }

        return bvp;
    }

    /*************************************** WHOLE DATASET ****************************************/
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{stat}/{userID}/{sourceID}")
    @ApiOperation(
        value = "Return a dataset of BloodVolumePulse level values",
        notes = "Return a dataset for the given userID and sourceID of type stat")
    @ApiResponses(value = {
        @ApiResponse(code = 500, message = "An error occurs while executing, in the body" +
            "there is a message.avsc object with more details"),
        @ApiResponse(code = 204, message = "No value for the given parameters, in the body" +
                "there is a message.avsc object with more details"),
        @ApiResponse(code = 200, message = "Return a dataset.avsc object containing all"
            + "available blood_volume_pulse.avsc values for the required statistic function")})
    public Response getAllByUserJson(
            @PathParam("userID") String userID,
            @PathParam("sourceID") String sourceID,
            @PathParam("stat") MongoHelper.Stat stat) {
        try {
            return ResponseHandler.getJsonResponse(request,
                getAllByUserWorker(userID, sourceID, stat), sensorName);
        }
        catch (Exception e){
            logger.error(e.getMessage(),e);
            return ResponseHandler.getJsonErrorResponse(request, "Your request cannot be"
                + "completed. If this error persists, please contact the service administrator.");
        }
    }

    @GET
    @Produces(AvroConverter.MEDIA_TYPE)
    @Path("/AVRO/{stat}/{userID}/{sourceID}")
    @ApiOperation(
        value = "Return a dataset of BloodVolumePulse level values",
        notes = "Return a dataset for the given userID and sourceID of type stat")
    @ApiResponses(value = {
        @ApiResponse(code = 500, message = "An error occurs while executing, in the body" +
            "there is a a byte array serialising a message.avsc object with more details"),
        @ApiResponse(code = 204, message = "No value for the given parameters, in the body" +
            "there is a a byte array serialising a message.avsc object with more details"),
        @ApiResponse(code = 200, message = "Return a byte array serialising a dataset.avsc object"
            + "containing all available blood_volume_pulse.avsc values for the required statistic"
            + "function")})
    public Response getAllByUserAvro(
        @PathParam("userID") String userID,
        @PathParam("sourceID") String sourceID,
        @PathParam("stat") MongoHelper.Stat stat) {
        try {
            return ResponseHandler.getAvroResponse(request,
                getAllByUserWorker(userID, sourceID, stat));
        }
        catch (Exception e){
            logger.error(e.getMessage(),e);
            return ResponseHandler.getAvroErrorResponse(request, "Your request cannot be"
                + "completed. If this error persists, please contact the service administrator.");
        }
    }

    /**
     * Actual implementation of AVRO and JSON getAllByUser
     **/
    private Dataset getAllByUserWorker( String userID, String sourceID, MongoHelper.Stat stat )
        throws ConnectException {
        Param.isValidInput(userID, sourceID);

        Dataset bvp = BloodVolumePulseDAO.getInstance().valueByUserSource(userID, sourceID,
            Unit.nW, stat, context);

        if (bvp.getDataset().isEmpty()) {
            logger.info("No data for the user {} with source {}", userID, sourceID);
        }

        return bvp;
    }

    /************************************* WINDOWED DATASET ***************************************/
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{stat}/{userID}/{sourceID}/{start}/{end}")
    @ApiOperation(
        value = "Return a dataset of BloodVolumePulse level values",
        notes = "Return a dataset of type stat for the given userID and sourceID with data"
                + "belonging to the time window [start - end]")
    @ApiResponses(value = {
        @ApiResponse(code = 500, message = "An error occurs while executing, in the body" +
            "there is a message.avsc object with more details"),
        @ApiResponse(code = 204, message = "No value for the given parameters, in the body" +
                "there is a message.avsc object with more details"),
        @ApiResponse(code = 200, message = "Return a dataset.avsc object containing all"
            + "blood_volume_pulse.avsc values belonging to the time window [start - end] for"
            + "the required statistic function")})
    public Response getByUserForWindowJson(
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
            logger.error(e.getMessage(),e);
            return ResponseHandler.getJsonErrorResponse(request, "Your request cannot be"
                + "completed. If this error persists, please contact the service administrator.");
        }
    }

    @GET
    @Produces(AvroConverter.MEDIA_TYPE)
    @Path("/AVRO/{stat}/{userID}/{sourceID}/{start}/{end}")
    @ApiOperation(
        value = "Return a dataset of BloodVolumePulse level values",
        notes = "Return a dataset of type stat for the given userID and sourceID with data"
            + "belonging to the time window [start - end]")
    @ApiResponses(value = {
        @ApiResponse(code = 500, message = "An error occurs while executing, in the body" +
            "there is a a byte array serialising a message.avsc object with more details"),
        @ApiResponse(code = 204, message = "No value for the given parameters, in the body" +
            "there is a a byte array serialising a message.avsc object with more details"),
        @ApiResponse(code = 200, message = "Return a byte array serialising a dataset.avsc object"
            + "containing all blood_volume_pulse.avsc values belonging to the time window"
            + "[start - end] for the required statistic function")})
    public Response getByUserForWindowAvro(
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
            logger.error(e.getMessage(),e);
            return ResponseHandler.getAvroErrorResponse(request, "Your request cannot be"
                + "completed. If this error persists, please contact the service administrator.");
        }
    }

    /**
     * Actual implementation of AVRO and JSON getByUserForWindow
     **/
    private Dataset getByUserForWindowWorker(String userID, String sourceID, MongoHelper.Stat stat,
        long start, long end) throws ConnectException {
        Param.isValidInput(userID, sourceID);

        Dataset bvp = BloodVolumePulseDAO.getInstance().valueByUserSourceWindow(userID,
            sourceID, Unit.nW, stat, start, end, context);

        if (bvp.getDataset().isEmpty()) {
            logger.info("No data for the user {} with source {}", userID, sourceID);
        }

        return bvp;
    }

}
