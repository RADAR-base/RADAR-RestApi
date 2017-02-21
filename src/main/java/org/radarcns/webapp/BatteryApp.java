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
import org.radarcns.dao.mongo.BatteryDAO;
import org.radarcns.dao.mongo.util.MongoHelper;
import org.radarcns.security.Param;
import org.radarcns.util.ResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Battery sensor web-app. Function set to access Battery level data.
 */
@Api
@Path("/B")
public class BatteryApp {

    private static Logger logger = LoggerFactory.getLogger(BatteryApp.class);

    private final String sensorName = "battery";

    @Context private ServletContext context;
    @Context private HttpServletRequest request;

    //--------------------------------------------------------------------------------------------//
    //                                    REAL-TIME FUNCTIONS                                     //
    //--------------------------------------------------------------------------------------------//
    /**
     * JSON function that returns the last seen Battery level if available.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/RT/{stat}/{userID}/{sourceID}")
    @ApiOperation(
            value = "Return a Battery level values",
            notes = "Return the last seen Battery level value of type stat for the given userID"
                + "and sourceID")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "An error occurs while executing, in the body"
                + "there is a message.avsc object with more details"),
            @ApiResponse(code = 204, message = "No value for the given parameters, in the body"
                + "there is a message.avsc object with more details"),
            @ApiResponse(code = 200, message = "Return a dataset.avsc object containing last seen"
                + "battery.avsc value for the required statistic function")})
    public Response getRealTimeUserJsonBattery(
            @PathParam("userID") String user,
            @PathParam("sourceID") String source,
            @PathParam("stat") MongoHelper.Stat stat) {
        try {
            return ResponseHandler.getJsonResponse(request,
                getRealTimeUserWorker(user, source, stat), sensorName);
        } catch (Exception exec) {
            logger.error(exec.getMessage(), exec);
            return ResponseHandler.getJsonErrorResponse(request, "Your request cannot be"
                + "completed. If this error persists, please contact the service administrator.");
        }
    }

    /**
     * JSON function that returns the last seen Battery level if available.
     */
    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Path("/AVRO/RT/{stat}/{userID}/{sourceID}")
    @ApiOperation(
            value = "Return a Battery level values",
            notes = "Return the last seen Battery level value of type stat for the given userID"
                + "and sourceID")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "An error occurs while executing"),
            @ApiResponse(code = 204, message = "No value for the given parameters"),
            @ApiResponse(code = 200, message = "Return a byte array serialising a dataset.avsc"
                + "object containing last seen battery.avsc value for the required statistic"
                + "function")})
    public Response getRealTimeUserAvroBattery(
            @PathParam("userID") String user,
            @PathParam("sourceID") String source,
            @PathParam("stat") MongoHelper.Stat stat) {
        try {
            return ResponseHandler.getAvroResponse(request,
                getRealTimeUserWorker(user, source, stat));
        } catch (Exception exec) {
            logger.error(exec.getMessage(), exec);
            return ResponseHandler.getAvroErrorResponse(request);
        }
    }

    /**
     * Actual implementation of AVRO and JSON getRealTimeUser.
     **/
    private Dataset getRealTimeUserWorker(String user, String source, MongoHelper.Stat stat)
            throws ConnectException {
        Param.isValidInput(user, source);

        Dataset battery = BatteryDAO.getInstance().valueRTByUserSource(user, source,
                Unit.percentage, stat, context);

        if (battery.getDataset().isEmpty()) {
            logger.info("No data for the user {} with source {}", user, source);
        }

        return battery;
    }

    //--------------------------------------------------------------------------------------------//
    //                                   WHOLE-DATA FUNCTIONS                                     //
    //--------------------------------------------------------------------------------------------//
    /**
     * JSON function that returns all available Battery level samples.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{stat}/{userID}/{sourceID}")
    @ApiOperation(
            value = "Return a dataset of Battery level values",
            notes = "Return a dataset for the given userID and sourceID of type stat")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "An error occurs while executing, in the body"
                + "there is a message.avsc object with more details"),
            @ApiResponse(code = 204, message = "No value for the given parameters, in the body"
                + "there is a message.avsc object with more details"),
            @ApiResponse(code = 200, message = "Return a dataset.avsc object containing all"
                + "available battery.avsc values for the required statistic function")})
    public Response getAllByUserJsonBattery(
            @PathParam("userID") String user,
            @PathParam("sourceID") String source,
            @PathParam("stat") MongoHelper.Stat stat) {
        try {
            return ResponseHandler.getJsonResponse(request, getAllByUserWorker(user, source,
                stat), sensorName);
        } catch (Exception exec) {
            logger.error(exec.getMessage(), exec);
            return ResponseHandler.getJsonErrorResponse(request, "Your request cannot be"
                + "completed. If this error persists, please contact the service administrator.");
        }
    }

    /**
     * AVRO function that returns all available Battery level samples.
     */
    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Path("/AVRO/{stat}/{userID}/{sourceID}")
    @ApiOperation(
            value = "Return a dataset of Battery level values",
            notes = "Return a dataset for the given userID and sourceID of type stat")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "An error occurs while executing"),
            @ApiResponse(code = 204, message = "No value for the given parameters"),
            @ApiResponse(code = 200, message = "Return a byte array serialising a dataset.avsc"
                + "object containing all available battery.avsc values for the required statistic"
                + "function")})
    public Response getAllByUserAvroBattery(
            @PathParam("userID") String user,
            @PathParam("sourceID") String source,
            @PathParam("stat") MongoHelper.Stat stat) {
        try {
            return ResponseHandler.getAvroResponse(request, getAllByUserWorker(user, source,
                stat));
        } catch (Exception exec) {
            logger.error(exec.getMessage(), exec);
            return ResponseHandler.getAvroErrorResponse(request);
        }
    }

    /**
     * Actual implementation of AVRO and JSON getAllByUser.
     **/
    private Dataset getAllByUserWorker(String user, String source, MongoHelper.Stat stat)
            throws ConnectException {
        Param.isValidInput(user, source);

        Dataset battery = BatteryDAO.getInstance().valueByUserSource(user, source,
                Unit.percentage, stat, context);

        if (battery.getDataset().isEmpty()) {
            logger.info("No data for the user {} with source {}", user, source);
        }

        return battery;
    }

    //--------------------------------------------------------------------------------------------//
    //                                 WINDOWED-DATA FUNCTIONS                                    //
    //--------------------------------------------------------------------------------------------//
    /**
     * JSON function that returns all Battery level inside the time-window [start-end].
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{stat}/{userID}/{sourceID}/{start}/{end}")
    @ApiOperation(
            value = "Return a dataset of Battery level values",
            notes = "Return a dataset of type stat for the given userID and sourceID with data"
                + "belonging to the time window [start - end]")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "An error occurs while executing, in the body"
                + "there is a message.avsc object with more details"),
            @ApiResponse(code = 204, message = "No value for the given parameters, in the body"
                + "there is a message.avsc object with more details"),
            @ApiResponse(code = 200, message = "Return a dataset.avsc object containing all"
                + "battery.avsc values belonging to the time window [start - end] for the required"
                + "statistic function")})
    public Response getByUserForWindowJsonBattery(
            @PathParam("userID") String user,
            @PathParam("sourceID") String source,
            @PathParam("stat") MongoHelper.Stat stat,
            @PathParam("start") long start,
            @PathParam("end") long end) {
        try {
            return ResponseHandler.getJsonResponse(request, getByUserForWindowWorker(user,
                source, stat, start, end), sensorName);
        } catch (Exception exec) {
            logger.error(exec.getMessage(), exec);
            return ResponseHandler.getJsonErrorResponse(request, "Your request cannot be"
                + "completed. If this error persists, please contact the service administrator.");
        }
    }

    /**
     * JSON function that returns all Battery level samples inside the time-window [start-end].
     */
    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Path("/AVRO/{stat}/{userID}/{sourceID}/{start}/{end}")
    @ApiOperation(
            value = "Return a dataset of Battery level values",
            notes = "Return a dataset of type stat for the given userID and sourceID with data"
                + "belonging to the time window [start - end]")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "An error occurs while executing"),
            @ApiResponse(code = 204, message = "No value for the given parameters"),
            @ApiResponse(code = 200, message = "Return a byte array serialising a dataset.avsc"
                + "object containing all battery.avsc values belonging to the time window"
                + "[start - end] for the required statistic function")})
    public Response getByUserForWindowAvroBattery(
            @PathParam("userID") String user,
            @PathParam("sourceID") String source,
            @PathParam("stat") MongoHelper.Stat stat,
            @PathParam("start") long start,
            @PathParam("end") long end) {
        try {
            return ResponseHandler.getAvroResponse(request, getByUserForWindowWorker(user,
                source, stat, start, end));
        } catch (Exception exec) {
            logger.error(exec.getMessage(), exec);
            return ResponseHandler.getAvroErrorResponse(request);
        }
    }

    /**
     * Actual implementation of AVRO and JSON getByUserForWindow.
     **/
    private Dataset getByUserForWindowWorker(String user, String source, MongoHelper.Stat stat,
            long start, long end) throws ConnectException {
        Param.isValidInput(user, source);

        Dataset battery = BatteryDAO.getInstance().valueByUserSourceWindow(user, source,
                Unit.percentage, stat, start, end, context);

        if (battery.getDataset().isEmpty()) {
            logger.info("No data for the user {} with source {}", user, source);
        }

        return battery;
    }

}
