package org.radarcns.webapp;

/*
 *  Copyright 2016 King's College London and The Hyve
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
import org.radarcns.avro.restapi.header.TimeFrame;
import org.radarcns.avro.restapi.sensor.SensorType;
import org.radarcns.dao.SensorDataAccessObject;
import org.radarcns.security.Param;
import org.radarcns.util.ResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sensor web-app. Function set to access all data data.
 */
@Api
@Path("/data")
public class SensorEndPoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(SensorEndPoint.class);

    @Context private ServletContext context;
    @Context private HttpServletRequest request;

    //--------------------------------------------------------------------------------------------//
    //                                    REAL-TIME FUNCTIONS                                     //
    //--------------------------------------------------------------------------------------------//
    /**
     * JSON function that returns the last seen data value if available.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/realTime/{sensor}/{stat}/{interval}/{patientID}/{sourceID}")
    @ApiOperation(
            value = "Returns a dataset object formatted in JSON.",
            notes = "Each collected sample is aggregated to provide near real-time statistical "
                + "results. This end-point returns the last computed result of type stat for the "
                + "given patientID, sourceID, and sensor. Data can be queried using different "
                + "time-frame resolutions. The response is formatted in JSON.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "An error occurs while executing, in the body "
                + "there is a message.avsc object with more details."),
            @ApiResponse(code = 204, message = "No value for the given parameters, in the body "
                + "there is a message.avsc object with more details."),
            @ApiResponse(code = 200, message = "Returns a dataset.avsc object containing last "
                + "computed sample for the given inputs formatted either Acceleration.avsc or "
                + "DoubleValue.avsc")})
    public Response getRealTimeUserJson(
            @PathParam("sensor") SensorType sensor,
            @PathParam("stat") DescriptiveStatistic stat,
            @PathParam("interval") TimeFrame interval,
            @PathParam("patientID") String user,
            @PathParam("sourceID") String source) {
        try {
            return ResponseHandler.getJsonResponse(request,
                    getRealTimeUserWorker(user, source, sensor, stat, interval));
        } catch (Exception exec) {
            LOGGER.error(exec.getMessage(), exec);
            return ResponseHandler.getJsonErrorResponse(request, "Your request cannot be "
                + "completed. If this error persists, please contact the service administrator.");
        }
    }

    /**
     * AVRO function that returns the last seen data value if available.
     */
    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Path("/avro/realTime/{sensor}/{stat}/{interval}/{patientID}/{sourceID}")
    @ApiOperation(
            value = "Returns a dataset object formatted in Apache AVRO.",
            notes = "Each collected sample is aggregated to provide near real-time statistical "
                + "results. This end-point returns the last computed result of type stat for the "
                + "given patientID, sourceID, and sensor. Data can be queried using different "
                + "time-frame resolutions. The response is formatted in Apache AVRO.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "An error occurs while executing"),
            @ApiResponse(code = 204, message = "No value for the given parameters."),
            @ApiResponse(code = 200, message = "Returns a byte array serialising a dataset.avsc "
                + "object containing last computed sample for the given inputs formatted either "
                + "Acceleration.avsc or DoubleValue.avsc")})
    public Response getRealTimeUserAvro(
            @PathParam("sensor") SensorType sensor,
            @PathParam("stat") DescriptiveStatistic stat,
            @PathParam("interval") TimeFrame interval,
            @PathParam("patientID") String user,
            @PathParam("sourceID") String source) {
        try {
            return ResponseHandler.getAvroResponse(request,
                getRealTimeUserWorker(user, source, sensor, stat, interval));
        } catch (Exception exec) {
            LOGGER.error(exec.getMessage(), exec);
            return ResponseHandler.getAvroErrorResponse(request);
        }
    }

    /**
     * Actual implementation of AVRO and JSON getRealTimeUser.
     **/
    private Dataset getRealTimeUserWorker(String user, String source, SensorType sensor,
            DescriptiveStatistic stat, TimeFrame interval) throws ConnectException {
        Param.isValidInput(user, source);

        Dataset data = SensorDataAccessObject.getInstance().valueRTByUserSource(user, source,
                    stat, interval, sensor, context);

        if (data.getDataset().isEmpty()) {
            LOGGER.info("No data for the user {} with source {}", user, source);
        }

        return data;
    }

    //--------------------------------------------------------------------------------------------//
    //                                   WHOLE-DATA FUNCTIONS                                     //
    //--------------------------------------------------------------------------------------------//
    /**
     * JSON function that returns all available samples for the given data.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{sensor}/{stat}/{interval}/{patientID}/{sourceID}")
    @ApiOperation(
            value = "Returns a dataset object formatted in JSON.",
            notes = "Each collected sample is aggregated to provide near real-time statistical "
                + "results. This end-point returns all available results of type stat for the "
                + "given patientID, sourceID, and sensor. Data can be queried using different "
                + "time-frame resolutions. The response is formatted in JSON.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "An error occurs while executing, in the body there "
                + "is a message.avsc object with more details."),
            @ApiResponse(code = 204, message = "No value for the given parameters, in the body "
                + "there is a message.avsc object with more details."),
            @ApiResponse(code = 200, message = "Returns a dataset.avsc object containing all "
                + "available samples for the given inputs formatted either Acceleration.avsc or "
                + "DoubleValue.avsc")})
    public Response getAllByUserJson(
            @PathParam("sensor") SensorType sensor,
            @PathParam("stat") DescriptiveStatistic stat,
            @PathParam("interval") TimeFrame interval,
            @PathParam("patientID") String user,
            @PathParam("sourceID") String source) {
        try {
            return ResponseHandler.getJsonResponse(request,
                getAllByUserWorker(user, source, stat, interval, sensor));
        } catch (Exception exec) {
            LOGGER.error(exec.getMessage(), exec);
            return ResponseHandler.getJsonErrorResponse(request, "Your request cannot be "
                + "completed. If this error persists, please contact the service administrator.");
        }
    }

    /**
     * AVRO function that returns all available samples for the given data.
     */
    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Path("/avro/{sensor}/{stat}/{interval}/{patientID}/{sourceID}")
    @ApiOperation(
            value = "Returns a dataset object formatted in Apache AVRO.",
            notes = "Each collected sample is aggregated to provide near real-time statistical "
                + "results. This end-point returns all available results of type stat for the "
                + "given patientID, sourceID, and sensor. Data can be queried using different "
                + "time-frame resolutions. The response is formatted in Apache AVRO.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "An error occurs while executing."),
            @ApiResponse(code = 204, message = "No value for the given parameters."),
            @ApiResponse(code = 200, message = "Returns a byte array serialising a dataset.avsc "
                + "object containing all available samples for the given inputs formatted either "
                + "Acceleration.avsc or DoubleValue.avsc")})
    public Response getAllByUserAvro(
            @PathParam("sensor") SensorType sensor,
            @PathParam("stat") DescriptiveStatistic stat,
            @PathParam("interval") TimeFrame interval,
            @PathParam("patientID") String user,
            @PathParam("sourceID") String source) {
        try {
            return ResponseHandler.getAvroResponse(request,
                getAllByUserWorker(user, source, stat, interval, sensor));
        } catch (Exception exec) {
            LOGGER.error(exec.getMessage(), exec);
            return ResponseHandler.getAvroErrorResponse(request);
        }
    }

    /**
     * Actual implementation of AVRO and JSON getAllByUser.
     **/
    private Dataset getAllByUserWorker(String user, String source, DescriptiveStatistic stat,
            TimeFrame interval, SensorType sensor) throws ConnectException {
        Param.isValidInput(user, source);

        Dataset data = SensorDataAccessObject.getInstance().valueByUserSource(user, source, stat,
                interval, sensor, context);

        if (data.getDataset().isEmpty()) {
            LOGGER.info("No data for the user {} with source {}", user, source);
        }

        return data;
    }

    //--------------------------------------------------------------------------------------------//
    //                                 WINDOWED-DATA FUNCTIONS                                    //
    //--------------------------------------------------------------------------------------------//
    /**
     * JSON function that returns all data value inside the time-window [start-end].
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{sensor}/{stat}/{interval}/{patientID}/{sourceID}/{start}/{end}")
    @ApiOperation(
            value = "Returns a dataset object formatted in JSON.",
            notes = "Each collected sample is aggregated to provide near real-time statistical "
                + "results. This end-point returns all available results of type stat for the "
                + "given patientID, sourceID, and sensor belonging to the time window [start - end]. "
                + "Data can be queried using different time-frame resolutions. The response is "
                + "formatted in JSON.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "An error occurs while executing, in the body "
                + "there is a message.avsc object with more details."),
            @ApiResponse(code = 204, message = "No value for the given parameters, in the body "
                + "there is a message.avsc object with more details."),
            @ApiResponse(code = 200, message = "Returns a dataset.avsc object containing samples "
                + "belonging to the time window [start - end] for the given inputs formatted "
                + "either Acceleration.avsc or DoubleValue.avsc.")})
    public Response getByUserForWindowJson(
            @PathParam("sensor") SensorType sensor,
            @PathParam("stat") DescriptiveStatistic stat,
            @PathParam("patientID") String user,
            @PathParam("sourceID") String source,
            @PathParam("interval") TimeFrame interval,
            @PathParam("start") long start,
            @PathParam("end") long end) {
        try {
            return ResponseHandler.getJsonResponse(request,
                getByUserForWindowWorker(user, source, stat, interval, sensor, start, end));
        } catch (Exception exec) {
            LOGGER.error(exec.getMessage(), exec);
            return ResponseHandler.getJsonErrorResponse(request, "Your request cannot be "
                + "completed. If this error persists, please contact the service administrator.");
        }
    }

    /**
     * AVRO function that returns all data value inside the time-window [start-end].
     */
    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Path("/avro/{sensor}/{stat}/{interval}/{patientID}/{sourceID}/{start}/{end}")
    @ApiOperation(
            value = "Returns a dataset object formatted in Apache AVRO.",
            notes = "Each collected sample is aggregated to provide near real-time statistical "
                + "results. This end-point returns all available results of type stat for the "
                + "given patientID, sourceID, and sensor belonging to the time window [start - end]. "
                + "Data can be queried using different time-frame resolutions. The response is "
                + "formatted in Apache AVRO.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "An error occurs while executing"),
            @ApiResponse(code = 204, message = "No value for the given parameters"),
            @ApiResponse(code = 200, message = "Returns a byte array serialising a dataset.avsc "
                + "object containing samples belonging to the time window [start - end] for the "
                + "given inputs formatted either Acceleration.avsc or DoubleValue.avsc.")})
    public Response getByUserForWindowAvro(
            @PathParam("sensor") SensorType sensor,
            @PathParam("stat") DescriptiveStatistic stat,
            @PathParam("interval") TimeFrame interval,
            @PathParam("patientID") String user,
            @PathParam("sourceID") String source,
            @PathParam("start") long start,
            @PathParam("end") long end) {
        try {
            return ResponseHandler.getAvroResponse(request,
                getByUserForWindowWorker(user, source, stat, interval, sensor, start, end));
        } catch (Exception exec) {
            LOGGER.error(exec.getMessage(), exec);
            return ResponseHandler.getAvroErrorResponse(request);
        }
    }

    /**
     * Actual implementation of AVRO and JSON getByUserForWindow.
     **/
    private Dataset getByUserForWindowWorker(String user, String source, DescriptiveStatistic stat,
            TimeFrame interval, SensorType sensor, long start, long end) throws ConnectException {
        Param.isValidInput(user, source);

        Dataset acc = SensorDataAccessObject.getInstance().valueByUserSourceWindow(user, source,
                stat, interval, start, end, sensor, context);

        if (acc.getDataset().isEmpty()) {
            LOGGER.info("No data for the user {} with source {}", user, source);
        }

        return acc;
    }

}
