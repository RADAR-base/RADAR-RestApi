/*
 * Copyright 2016 King's College London and The Hyve
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

package org.radarcns.webapp;

import static org.radarcns.webapp.util.BasePath.AVRO;
import static org.radarcns.webapp.util.BasePath.DATA;
import static org.radarcns.webapp.util.BasePath.REALTIME;
import static org.radarcns.webapp.util.Parameter.END;
import static org.radarcns.webapp.util.Parameter.TIME_WINDOW;
import static org.radarcns.webapp.util.Parameter.SENSOR;
import static org.radarcns.webapp.util.Parameter.SOURCE_ID;
import static org.radarcns.webapp.util.Parameter.START;
import static org.radarcns.webapp.util.Parameter.STAT;
import static org.radarcns.webapp.util.Parameter.SUBJECT_ID;
import static org.radarcns.webapp.util.ResponseHandler.AVRO_NOT_PREFERRED_MEDIA_TYPE;
import static org.radarcns.webapp.util.ResponseHandler.JSON_PREFERRED_MEDIA_TYPE;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.net.ConnectException;
import java.util.LinkedList;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.radarcns.avro.restapi.dataset.Dataset;
import org.radarcns.avro.restapi.dataset.Item;
import org.radarcns.avro.restapi.header.DescriptiveStatistic;
import org.radarcns.avro.restapi.header.TimeFrame;
import org.radarcns.avro.restapi.sensor.SensorType;
import org.radarcns.dao.SensorDataAccessObject;
import org.radarcns.dao.SubjectDataAccessObject;
import org.radarcns.security.Param;
import org.radarcns.webapp.util.ResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sensor web-app. Function set to access all data data.
 */
@Api
@Path("/" + DATA)
public class SensorEndPoint {
    public static final String TIME_WINDOW_DEFAULT = "TEN_SECOND";
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
    @Produces(JSON_PREFERRED_MEDIA_TYPE)
    @Path("/" + REALTIME + "/{" + SENSOR + "}/{" + STAT + "}/{" + TIME_WINDOW + "}/{" + SUBJECT_ID
            + "}/{" + SOURCE_ID + "}")
    @ApiOperation(
            value = "Returns a dataset object formatted in JSON.",
            notes = "Each collected sample is aggregated to provide near real-time statistical "
                + "results. This end-point returns the last computed result of type stat for the "
                + "given subjectID, sourceID, and sensor. Data can be queried using different "
                + "time-frame resolutions. The response is formatted in JSON.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "An error occurs while executing, in the body "
                + "there is a message.avsc object with more details."),
            @ApiResponse(code = 204, message = "No value for the given parameters, in the body "
                + "there is a message.avsc object with more details."),
            @ApiResponse(code = 200, message = "Returns a dataset.avsc object containing last "
                + "computed sample for the given inputs formatted either Acceleration.avsc or "
                + "DoubleValue.avsc")})
    public Response getLastReceivedSampleJson(
            @PathParam(SENSOR) SensorType sensor,
            @PathParam(STAT) DescriptiveStatistic stat,
            @PathParam(TIME_WINDOW) TimeFrame interval,
            @PathParam(SUBJECT_ID) String subject,
            @PathParam(SOURCE_ID) String source) {
        try {
            return ResponseHandler.getJsonResponse(request,
                    getLastReceivedSampleWorker(subject, source, sensor, stat, interval));
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
    @Produces(AVRO_NOT_PREFERRED_MEDIA_TYPE)
    @Path("{" + SUBJECT_ID + "}/{" + SOURCE_ID + "}/" + REALTIME + "/{" + SENSOR + "}/{" + STAT)
    @ApiOperation(
            value = "Returns a dataset object formatted in Apache AVRO.",
            notes = "Each collected sample is aggregated to provide near real-time statistical "
                + "results. This end-point returns the last computed result of type stat for the "
                + "given subjectID, sourceID, and sensor. Data can be queried using different "
                + "time-frame resolutions. The response is formatted in Apache AVRO.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "An error occurs while executing"),
            @ApiResponse(code = 204, message = "No value for the given parameters."),
            @ApiResponse(code = 200, message = "Returns a byte array serialising a dataset.avsc "
                + "object containing last computed sample for the given inputs formatted either "
                + "Acceleration.avsc or DoubleValue.avsc")})
    public Response getLastReceivedSampleAvro(
            @PathParam(SENSOR) SensorType sensor,
            @PathParam(STAT) DescriptiveStatistic stat,
            @QueryParam(TIME_WINDOW) @DefaultValue(TIME_WINDOW_DEFAULT) TimeFrame interval,
            @PathParam(SUBJECT_ID) String subject,
            @PathParam(SOURCE_ID) String source) {
        try {
            return ResponseHandler.getAvroResponse(request,
                getLastReceivedSampleWorker(subject, source, sensor, stat, interval));
        } catch (Exception exec) {
            LOGGER.error(exec.getMessage(), exec);
            return ResponseHandler.getAvroErrorResponse(request);
        }
    }

    /**
     * Actual implementation of AVRO and JSON getRealTimeSubject.
     **/
    private Dataset getLastReceivedSampleWorker(String subject, String source, SensorType sensor,
            DescriptiveStatistic stat, TimeFrame interval) throws ConnectException {
        Param.isValidInput(subject, source);

        Dataset dataset = new Dataset(null, new LinkedList<>());

        if (SubjectDataAccessObject.exist(subject, context)) {
            dataset = SensorDataAccessObject.getInstance()
                .getLastReceivedSample(subject, source,
                    stat, interval, sensor, context);

            if (dataset.getDataset().isEmpty()) {
                LOGGER.debug("No data for the subject {} with source {}", subject, source);
            }
        }

        return dataset;
    }

    //--------------------------------------------------------------------------------------------//
    //                                   WHOLE-DATA FUNCTIONS                                     //
    //--------------------------------------------------------------------------------------------//
    /**
     * JSON function that returns all available samples for the given data.
     */
    @GET
    @Produces(JSON_PREFERRED_MEDIA_TYPE)
    @Path("{" + SUBJECT_ID + "}/{" + SOURCE_ID + "}/{" + SENSOR + "}/{" + STAT + "}")
    @ApiOperation(
            value = "Returns a dataset object formatted in JSON.",
            notes = "Each collected sample is aggregated to provide near real-time statistical "
                + "results. This end-point returns all available results of type stat for the "
                + "given subjectID, sourceID, and sensor. Data can be queried using different "
                + "time-frame resolutions. The response is formatted in JSON.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "An error occurs while executing, in the body there "
                + "is a message.avsc object with more details."),
            @ApiResponse(code = 204, message = "No value for the given parameters, in the body "
                + "there is a message.avsc object with more details."),
            @ApiResponse(code = 200, message = "Returns a dataset.avsc object containing all "
                + "available samples for the given inputs formatted either Acceleration.avsc or "
                + "DoubleValue.avsc")})
    public Response getSamplesJson(
            @PathParam(SENSOR) SensorType sensor,
            @PathParam(STAT) DescriptiveStatistic stat,
            @QueryParam(TIME_WINDOW) @DefaultValue(TIME_WINDOW_DEFAULT) TimeFrame interval,
            @PathParam(SUBJECT_ID) String subject,
            @PathParam(SOURCE_ID) String source) {
        try {
            return ResponseHandler.getJsonResponse(request,
                getSamplesWorker(subject, source, stat, interval, sensor));
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
    @Produces(AVRO_NOT_PREFERRED_MEDIA_TYPE)
    @Path("{" + SUBJECT_ID + "}/{" + SOURCE_ID + "}/{" + SENSOR + "}/{" + STAT + "}")
    @ApiOperation(
            value = "Returns a dataset object formatted in Apache AVRO.",
            notes = "Each collected sample is aggregated to provide near real-time statistical "
                + "results. This end-point returns all available results of type stat for the "
                + "given subjectID, sourceID, and sensor. Data can be queried using different "
                + "time-frame resolutions. The response is formatted in Apache AVRO.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "An error occurs while executing."),
            @ApiResponse(code = 204, message = "No value for the given parameters."),
            @ApiResponse(code = 200, message = "Returns a byte array serialising a dataset.avsc "
                + "object containing all available samples for the given inputs formatted either "
                + "Acceleration.avsc or DoubleValue.avsc")})
    public Response getSamplesAvro(
            @PathParam(SENSOR) SensorType sensor,
            @PathParam(STAT) DescriptiveStatistic stat,
            @QueryParam(TIME_WINDOW) @DefaultValue(TIME_WINDOW_DEFAULT) TimeFrame interval,
            @PathParam(SUBJECT_ID) String subject,
            @PathParam(SOURCE_ID) String source) {
        try {
            return ResponseHandler.getAvroResponse(request,
                getSamplesWorker(subject, source, stat, interval, sensor));
        } catch (Exception exec) {
            LOGGER.error(exec.getMessage(), exec);
            return ResponseHandler.getAvroErrorResponse(request);
        }
    }

    /**
     * Actual implementation of AVRO and JSON getAllBySubject.
     **/
    private Dataset getSamplesWorker(String subject, String source, DescriptiveStatistic stat,
            TimeFrame interval, SensorType sensor) throws ConnectException {
        Param.isValidInput(subject, source);

        Dataset dataset = new Dataset(null, new LinkedList<Item>());

        if (SubjectDataAccessObject.exist(subject, context)) {
            dataset = SensorDataAccessObject.getInstance().getSamples(subject,
                source, stat, interval, sensor, context);

            if (dataset.getDataset().isEmpty()) {
                LOGGER.debug("No data for the subject {} with source {}", subject, source);
            }
        }

        return dataset;
    }

    //--------------------------------------------------------------------------------------------//
    //                                 WINDOWED-DATA FUNCTIONS                                    //
    //--------------------------------------------------------------------------------------------//
    /**
     * JSON function that returns all data value inside the time-window [start-end].
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{" + SUBJECT_ID + "}/{" + SOURCE_ID + "}/{" + SENSOR + "}/{" + STAT + "}/{" + TIME_WINDOW + "}")
    @ApiOperation(
            value = "Returns a dataset object formatted in JSON.",
            notes = "Each collected sample is aggregated to provide near real-time statistical "
                + "results. This end-point returns all available results of type stat for the "
                + "given subjectID, sourceID, and sensor belonging to the time window "
                + "[start - end]. Data can be queried using different time-frame resolutions. "
                + "The response is formatted in JSON.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "An error occurs while executing, in the body "
                + "there is a message.avsc object with more details."),
            @ApiResponse(code = 204, message = "No value for the given parameters, in the body "
                + "there is a message.avsc object with more details."),
            @ApiResponse(code = 200, message = "Returns a dataset.avsc object containing samples "
                + "belonging to the time window [start - end] for the given inputs formatted "
                + "either Acceleration.avsc or DoubleValue.avsc.")})
    public Response getSamplesWithinWindowJson(
            @PathParam(SENSOR) SensorType sensor,
            @PathParam(STAT) DescriptiveStatistic stat,
            @PathParam(SUBJECT_ID) String subject,
            @PathParam(SOURCE_ID) String source,
            @QueryParam(TIME_WINDOW) @DefaultValue("TEN_SECOND") TimeFrame interval,
            @QueryParam(START) long start,
            @QueryParam(END) long end) {
        try {
            return ResponseHandler.getJsonResponse(request,
                getSamplesWithinWindowWorker(subject, source, stat, interval, sensor, start, end));
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
    @Path("/" + AVRO + "/{" + SENSOR + "}/{" + STAT + "}/{" + TIME_WINDOW + "}/{" + SUBJECT_ID + "}/{"
            + SOURCE_ID + "}/{" + START + "}/{" + END + "}")
    @ApiOperation(
            value = "Returns a dataset object formatted in Apache AVRO.",
            notes = "Each collected sample is aggregated to provide near real-time statistical "
                + "results. This end-point returns all available results of type stat for the "
                + "given subjectID, sourceID, and sensor belonging to the time window "
                + "[start - end]. Data can be queried using different time-frame resolutions. "
                + "The response is formatted in Apache AVRO.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "An error occurs while executing"),
            @ApiResponse(code = 204, message = "No value for the given parameters"),
            @ApiResponse(code = 200, message = "Returns a byte array serialising a dataset.avsc "
                + "object containing samples belonging to the time window [start - end] for the "
                + "given inputs formatted either Acceleration.avsc or DoubleValue.avsc.")})
    public Response getSamplesWithinWindowAvro(
            @PathParam(SENSOR) SensorType sensor,
            @PathParam(STAT) DescriptiveStatistic stat,
            @PathParam(TIME_WINDOW) TimeFrame interval,
            @PathParam(SUBJECT_ID) String subject,
            @PathParam(SOURCE_ID) String source,
            @PathParam(START) long start,
            @PathParam(END) long end) {
        try {
            return ResponseHandler.getAvroResponse(request,
                getSamplesWithinWindowWorker(subject, source, stat, interval, sensor, start, end));
        } catch (Exception exec) {
            LOGGER.error(exec.getMessage(), exec);
            return ResponseHandler.getAvroErrorResponse(request);
        }
    }

    /**
     * Actual implementation of AVRO and JSON getBySubjectForWindow.
     **/
    private Dataset getSamplesWithinWindowWorker(String subject, String source,
            DescriptiveStatistic stat, TimeFrame interval, SensorType sensor, long start,
            long end) throws ConnectException {
        Param.isValidInput(subject, source);

        Dataset dataset = new Dataset(null, new LinkedList<>());

        if (SubjectDataAccessObject.exist(subject, context)) {
            dataset = SensorDataAccessObject.getInstance().getSamples(
                subject, source, stat, interval, start, end, sensor, context);

            if (dataset.getDataset().isEmpty()) {
                LOGGER.debug("No data for the subject {} with source {}", subject, source);
            }
        }

        return dataset;
    }

}
