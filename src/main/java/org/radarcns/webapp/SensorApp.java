package org.radarcns.webapp;

/*
 *  Copyright 2016 Kings College London and The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import org.radarcns.avro.restapi.header.DescriptiveStatistic;
import org.radarcns.avro.restapi.sensor.SensorType;
import org.radarcns.dao.SensorDataAccessObject;
import org.radarcns.security.Param;
import org.radarcns.util.RadarConverter;
import org.radarcns.util.ResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sensor web-app. Function set to access all sensor data.
 */
@Api
@Path("/sensor")
public class SensorApp {

    private static Logger logger = LoggerFactory.getLogger(SensorApp.class);

    @Context private ServletContext context;
    @Context private HttpServletRequest request;

    //--------------------------------------------------------------------------------------------//
    //                                    REAL-TIME FUNCTIONS                                     //
    //--------------------------------------------------------------------------------------------//
    /**
     * JSON function that returns the last seen sensor value if available.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/realTime/{sensor}/{stat}/{userID}/{sourceID}")
    @ApiOperation(
            value = "Return an Acceleration values",
            notes = "Return the last seen Acceleration value of type stat for the given userID and"
                + "sourceID")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "An error occurs while executing, in the body"
                + "there is a message.avsc object with more details"),
            @ApiResponse(code = 204, message = "No value for the given parameters, in the body"
                + "there is a message.avsc object with more details"),
            @ApiResponse(code = 200, message = "Return a dataset.avsc object containing last seen"
                + "acceleration.avsc value for the required statistic function")})
    public Response getRealTimeUserJson(
            @PathParam("sensor") SensorType sensor,
            @PathParam("stat") DescriptiveStatistic stat,
            @PathParam("userID") String user,
            @PathParam("sourceID") String source) {
        try {
            return ResponseHandler.getJsonResponse(request,
                getRealTimeUserWorker(user, source, sensor, stat),
                RadarConverter.getSensorName(sensor));
        } catch (Exception exec) {
            logger.error(exec.getMessage(), exec);
            return ResponseHandler.getJsonErrorResponse(request, "Your request cannot be"
                + "completed. If this error persists, please contact the service administrator.");
        }
    }

    /**
     * AVRO function that returns the last seen sensor value if available.
     */
    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Path("/avro/realTime/{sensor}/{stat}/{userID}/{sourceID}")
    @ApiOperation(
            value = "Return an Acceleration values",
            notes = "Return the last seen Acceleration value of type stat for the given userID and"
            + "sourceID")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "An error occurs while executing"),
            @ApiResponse(code = 204, message = "No value for the given parameters"),
            @ApiResponse(code = 200, message = "Return a byte array serialising a dataset.avsc"
                + "object containing last seen acceleration.avsc value for the required statistic"
                + "function")})
    public Response getRealTimeUserAvro(
            @PathParam("sensor") SensorType sensor,
            @PathParam("stat") DescriptiveStatistic stat,
            @PathParam("userID") String user,
            @PathParam("sourceID") String source) {
        try {
            return ResponseHandler.getAvroResponse(request,
                getRealTimeUserWorker(user, source, sensor, stat));
        } catch (Exception exec) {
            logger.error(exec.getMessage(), exec);
            return ResponseHandler.getAvroErrorResponse(request);
        }
    }

    /**
     * Actual implementation of AVRO and JSON getRealTimeUser.
     **/
    private Dataset getRealTimeUserWorker(String user, String source, SensorType sensor,
            DescriptiveStatistic stat) throws ConnectException {
        Param.isValidInput(user, source);

        Dataset data = SensorDataAccessObject.getInstance().valueRTByUserSource(user, source,
                    stat, sensor, context);

        if (data.getDataset().isEmpty()) {
            logger.info("No data for the user {} with source {}", user, source);
        }

        return data;
    }

    //--------------------------------------------------------------------------------------------//
    //                                   WHOLE-DATA FUNCTIONS                                     //
    //--------------------------------------------------------------------------------------------//
    /**
     * JSON function that returns all available samples for the given sensor.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{sensor}/{stat}/{userID}/{sourceID}")
    @ApiOperation(
            value = "Return a dataset of Acceleration values",
            notes = "Return a dataset for the given userID and sourceID of type stat")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "An error occurs while executing, in the body there"
                + "is a message.avsc object with more details"),
            @ApiResponse(code = 204, message = "No value for the given parameters, in the body"
                + "there is a message.avsc object with more details"),
            @ApiResponse(code = 200, message = "Return a dataset.avsc object containing all"
                + "available acceleration.avsc values for the required statistic function")})
    public Response getAllByUserJson(
            @PathParam("sensor") SensorType sensor,
            @PathParam("stat") DescriptiveStatistic stat,
            @PathParam("userID") String user,
            @PathParam("sourceID") String source) {
        try {
            return ResponseHandler.getJsonResponse(request,
                getAllByUserWorker(user, source, stat, sensor),
                RadarConverter.getSensorName(sensor));
        } catch (Exception exec) {
            logger.error(exec.getMessage(), exec);
            return ResponseHandler.getJsonErrorResponse(request, "Your request cannot be"
                + "completed. If this error persists, please contact the service administrator.");
        }
    }

    /**
     * AVRO function that returns all available samples for the given sensor.
     */
    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Path("/avro/{sensor}/{stat}/{userID}/{sourceID}")
    @ApiOperation(
            value = "Return a dataset of Acceleration values",
            notes = "Return a dataset for the given userID and sourceID of type stat")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "An error occurs while executing"),
            @ApiResponse(code = 204, message = "No value for the given parameters"),
            @ApiResponse(code = 200, message = "Return a byte array serialising a dataset.avsc"
                + "object containing all available acceleration.avsc values for the required"
                + "statistic function")})
    public Response getAllByUserAvro(
            @PathParam("sensor") SensorType sensor,
            @PathParam("stat") DescriptiveStatistic stat,
            @PathParam("userID") String user,
            @PathParam("sourceID") String source) {
        try {
            return ResponseHandler.getAvroResponse(request,
                getAllByUserWorker(user, source, stat, sensor));
        } catch (Exception exec) {
            logger.error(exec.getMessage(), exec);
            return ResponseHandler.getAvroErrorResponse(request);
        }
    }

    /**
     * Actual implementation of AVRO and JSON getAllByUser.
     **/
    private Dataset getAllByUserWorker(String user, String source, DescriptiveStatistic stat,
            SensorType sensor) throws ConnectException {
        Param.isValidInput(user, source);

        Dataset data = SensorDataAccessObject.getInstance().valueByUserSource(user, source, stat,
                sensor, context);

        if (data.getDataset().isEmpty()) {
            logger.info("No data for the user {} with source {}", user, source);
        }

        return data;
    }

    //--------------------------------------------------------------------------------------------//
    //                                 WINDOWED-DATA FUNCTIONS                                    //
    //--------------------------------------------------------------------------------------------//
    /**
     * JSON function that returns all sensor value inside the time-window [start-end].
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{sensor}/{stat}/{userID}/{sourceID}/{start}/{end}")
    @ApiOperation(
            value = "Return a dataset of Acceleration values",
            notes = "Return a dataset of type stat for the given userID and sourceID with data"
                + "belonging to the time window [start - end]")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "An error occurs while executing, in the body"
                + "there is a message.avsc object with more details"),
            @ApiResponse(code = 204, message = "No value for the given parameters, in the body"
                + "there is a message.avsc object with more details"),
            @ApiResponse(code = 200, message = "Return a dataset.avsc object containing all"
                + "acceleration.avsc values belonging to the time window [start - end] for the"
                + "required statistic function")})
    public Response getByUserForWindowJson(
            @PathParam("sensor") SensorType sensor,
            @PathParam("stat") DescriptiveStatistic stat,
            @PathParam("userID") String user,
            @PathParam("sourceID") String source,
            @PathParam("start") long start,
            @PathParam("end") long end) {
        try {
            return ResponseHandler.getJsonResponse(request,
                getByUserForWindowWorker(user, source, stat, sensor, start, end),
                RadarConverter.getSensorName(sensor));
        } catch (Exception exec) {
            logger.error(exec.getMessage(), exec);
            return ResponseHandler.getJsonErrorResponse(request, "Your request cannot be "
                + "completed. If this error persists, please contact the service administrator.");
        }
    }

    /**
     * AVRO function that returns all sensor value inside the time-window [start-end].
     */
    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Path("/avro/{sensor}/{stat}/{userID}/{sourceID}/{start}/{end}")
    @ApiOperation(
            value = "Return a dataset of Acceleration values",
            notes = "Return a dataset of type stat for the given userID and sourceID with data"
                + "belonging to the time window [start - end]")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "An error occurs while executing"),
            @ApiResponse(code = 204, message = "No value for the given parameters"),
            @ApiResponse(code = 200, message = "Return a byte array serialising a dataset.avsc"
                + "object containing all acceleration.avsc values belonging to the time window"
                + "[start - end] for the required statistic function")})
    public Response getByUserForWindowAvro(
            @PathParam("sensor") SensorType sensor,
            @PathParam("stat") DescriptiveStatistic stat,
            @PathParam("userID") String user,
            @PathParam("sourceID") String source,
            @PathParam("start") long start,
            @PathParam("end") long end) {
        try {
            return ResponseHandler.getAvroResponse(request,
                getByUserForWindowWorker(user, source, stat, sensor, start, end));
        } catch (Exception exec) {
            logger.error(exec.getMessage(), exec);
            return ResponseHandler.getAvroErrorResponse(request);
        }
    }

    /**
     * Actual implementation of AVRO and JSON getByUserForWindow.
     **/
    private Dataset getByUserForWindowWorker(String user, String source, DescriptiveStatistic stat,
            SensorType sensor, long start, long end) throws ConnectException {
        Param.isValidInput(user, source);

        Dataset acc = SensorDataAccessObject.getInstance().valueByUserSourceWindow(user, source,
                stat, start, end, sensor, context);

        if (acc.getDataset().isEmpty()) {
            logger.info("No data for the user {} with source {}", user, source);
        }

        return acc;
    }

}
