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

package org.radarcns.webapp.resource;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.radarcns.auth.authorization.Permission.Entity.MEASUREMENT;
import static org.radarcns.auth.authorization.Permission.Operation.READ;
import static org.radarcns.webapp.util.BasePath.AVRO_BINARY;
import static org.radarcns.webapp.util.BasePath.DATA;
import static org.radarcns.webapp.util.BasePath.REALTIME;
import static org.radarcns.webapp.util.Parameter.END;
import static org.radarcns.webapp.util.Parameter.INTERVAL;
import static org.radarcns.webapp.util.Parameter.PROJECT_NAME;
import static org.radarcns.webapp.util.Parameter.SENSOR;
import static org.radarcns.webapp.util.Parameter.SOURCE_ID;
import static org.radarcns.webapp.util.Parameter.START;
import static org.radarcns.webapp.util.Parameter.STAT;
import static org.radarcns.webapp.util.Parameter.SUBJECT_ID;

import com.mongodb.MongoClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.ext.Provider;
import org.radarcns.auth.NeedsPermissionOnSubject;
import org.radarcns.catalogue.TimeWindow;
import org.radarcns.catalogue.Unit;
import org.radarcns.dao.SensorDataAccessObject;
import org.radarcns.dao.SubjectDataAccessObject;
import org.radarcns.listener.managementportal.ManagementPortalClient;
import org.radarcns.restapi.dataset.Dataset;
import org.radarcns.restapi.header.DescriptiveStatistic;
import org.radarcns.restapi.header.EffectiveTimeFrame;
import org.radarcns.restapi.header.Header;
import org.radarcns.webapp.validation.Alphanumeric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sensor web-app. Function set to access all data data.
 */
@Provider
@Path("/" + DATA)
public class SensorEndPoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(SensorEndPoint.class);

    @Inject
    private MongoClient mongoClient;

    @Inject
    private ManagementPortalClient mpClient;

    //--------------------------------------------------------------------------------------------//
    //                                    REAL-TIME FUNCTIONS                                     //
    //--------------------------------------------------------------------------------------------//

    /**
     * JSON function that returns the last seen data value if available.
     */
    @GET
    @Produces({APPLICATION_JSON, AVRO_BINARY})
    @Path("/" + REALTIME + "/{" + SENSOR + "}/{" + STAT + "}/{" + INTERVAL + "}/{" + PROJECT_NAME
            + "}/{" + SUBJECT_ID
            + "}/{" + SOURCE_ID + "}")
    @Operation(summary = "Returns a dataset object formatted in JSON.",
            description = "Each collected sample is aggregated to provide near real-time "
                    + "statistical "
                    + "results. This end-point returns the last computed result of type stat for "
                    + "the "
                    + "given subjectID, sourceID, and sensor. Data can be queried using different "
                    + "time-frame resolutions. The response is formatted in JSON.")
    @ApiResponse(responseCode = "500", description = "An error occurs while executing, in the body "
            + "there is a message.avsc object with more details.")
    @ApiResponse(responseCode = "204", description = "No value for the given parameters, in the "
            + "body there is a message.avsc object with more details.")
    @ApiResponse(responseCode = "200", description =
            "Returns a dataset.avsc object containing last "
                    + "computed sample for the given inputs formatted either Acceleration.avsc or "
                    + "DoubleValue.avsc")
    @ApiResponse(responseCode = "401", description = "Access denied error occurred")
    @ApiResponse(responseCode = "403", description = "Not Authorised error occurred")
    @NeedsPermissionOnSubject(entity = MEASUREMENT, operation = READ)
    public Dataset getLastReceivedSampleJson(
            @Alphanumeric @PathParam(SENSOR) String sensor,
            @PathParam(STAT) DescriptiveStatistic stat,
            @PathParam(INTERVAL) TimeWindow interval,
            @Alphanumeric @PathParam(PROJECT_NAME) String projectName,
            @Alphanumeric @PathParam(SUBJECT_ID) String subjectId,
            @Alphanumeric @PathParam(SOURCE_ID) String sourceId) throws IOException {
        mpClient.getSubject(subjectId);
        if (SubjectDataAccessObject.exist(subjectId, mongoClient)) {
            Dataset dataset = SensorDataAccessObject.getInstance()
                    .getLastReceivedSample(subjectId, sourceId,
                            stat, interval, sensor, mongoClient);

            if (dataset.getDataset().isEmpty()) {
                LOGGER.debug("No data for the subject {} with source {}", subjectId, sourceId);
            }

            return dataset;
        } else {
            return emptyDataset(subjectId, sourceId, sensor, stat, interval,
                    Instant.now());

        }
    }

    //--------------------------------------------------------------------------------------------//
    //                                   WHOLE-DATA FUNCTIONS                                     //
    //--------------------------------------------------------------------------------------------//

    /**
     * JSON function that returns all available samples for the given data.
     */
    @GET
    @Produces({APPLICATION_JSON, AVRO_BINARY})
    @Path("/{" + SENSOR + "}/{" + STAT + "}/{" + INTERVAL + "}/{" + PROJECT_NAME + "}/{"
            + SUBJECT_ID + "}/{"
            + SOURCE_ID + "}")
    @Operation(summary = "Returns a dataset object formatted in JSON.",
            description = "Each collected sample is aggregated to provide near real-time "
                    + "statistical "
                    + "results. This end-point returns all available results of type stat for the "
                    + "given subjectID, sourceID, and sensor. Data can be queried using different "
                    + "time-frame resolutions. The response is formatted in JSON.")
    @ApiResponse(responseCode = "500", description = "An error occurs while executing, in the body "
            + "there is a message.avsc object with more details.")
    @ApiResponse(responseCode = "204", description =
            "No value for the given parameters, in the body "
                    + "there is a message.avsc object with more details.")
    @ApiResponse(responseCode = "200", description = "Returns a dataset.avsc object containing all "
            + "available samples for the given inputs formatted either Acceleration.avsc or "
            + "DoubleValue.avsc")
    @ApiResponse(responseCode = "401", description = "Access denied error occurred")
    @ApiResponse(responseCode = "403", description = "Not Authorised error occurred")
    @NeedsPermissionOnSubject(entity = MEASUREMENT, operation = READ)
    public Dataset getSamplesJson(
            @Alphanumeric @PathParam(SENSOR) String sensor,
            @PathParam(STAT) DescriptiveStatistic stat,
            @PathParam(INTERVAL) TimeWindow interval,
            @Alphanumeric @PathParam(PROJECT_NAME) String projectName,
            @Alphanumeric @PathParam(SUBJECT_ID) String subjectId,
            @Alphanumeric @PathParam(SOURCE_ID) String sourceId) throws IOException {
        mpClient.getSubject(subjectId);

        if (SubjectDataAccessObject.exist(subjectId, mongoClient)) {
            Dataset dataset = SensorDataAccessObject.getInstance().getSamples(subjectId,
                    sourceId, stat, interval, sensor, mongoClient);

            if (dataset.getDataset().isEmpty()) {
                LOGGER.debug("No data for the subject {} with source {}", subjectId, sourceId);
            }

            return dataset;
        } else {
            return emptyDataset(subjectId, sourceId, sensor, stat, interval,
                    Instant.now());
        }
    }

    //--------------------------------------------------------------------------------------------//
    //                                 WINDOWED-DATA FUNCTIONS                                    //
    //--------------------------------------------------------------------------------------------//

    /**
     * JSON function that returns all data value inside the time-window [start-end].
     */
    @GET
    @Produces({APPLICATION_JSON, AVRO_BINARY})
    @Path("/{" + SENSOR + "}/{" + STAT + "}/{" + INTERVAL + "}/{" + PROJECT_NAME + "}/{"
            + SUBJECT_ID + "}/{"
            + SOURCE_ID + "}/{" + START + "}/{" + END + "}")
    @Operation(summary = "Returns a dataset object formatted in JSON.",
            description = "Each collected sample is aggregated to provide near real-time "
                    + "statistical "
                    + "results. This end-point returns all available results of type stat for the "
                    + "given subjectID, sourceID, and sensor belonging to the time window "
                    + "[start - end]. Data can be queried using different time-frame resolutions. "
                    + "The response is formatted in JSON.")
    @ApiResponse(responseCode = "500", description = "An error occurs while executing, in the body "
            + "there is a message.avsc object with more details.")
    @ApiResponse(responseCode = "204", description = "No value for the given parameters, in the "
            + "body "
            + "there is a message.avsc object with more details.")
    @ApiResponse(responseCode = "200", description = "Returns a dataset.avsc object containing "
            + "samples "
            + "belonging to the time window [start - end] for the given inputs formatted "
            + "either Acceleration.avsc or DoubleValue.avsc.")
    @ApiResponse(responseCode = "401", description = "Access denied error occurred")
    @ApiResponse(responseCode = "403", description = "Not Authorised error occurred")
    @NeedsPermissionOnSubject(entity = MEASUREMENT, operation = READ)
    public Dataset getSamplesWithinWindowJson(
            @Alphanumeric @PathParam(SENSOR) String sensor,
            @PathParam(STAT) DescriptiveStatistic stat,
            @Alphanumeric @PathParam(PROJECT_NAME) String projectName,
            @Alphanumeric @PathParam(SUBJECT_ID) String subjectId,
            @Alphanumeric @PathParam(SOURCE_ID) String sourceId,
            @PathParam(INTERVAL) TimeWindow interval,
            @PathParam(START) long start,
            @PathParam(END) long end) throws IOException {
        mpClient.getSubject(subjectId);

        if (SubjectDataAccessObject.exist(subjectId, mongoClient)) {
            Dataset dataset = SensorDataAccessObject.getInstance().getSamples(
                    subjectId, sourceId, stat, interval, start, end, sensor, mongoClient);

            if (dataset.getDataset().isEmpty()) {
                LOGGER.debug("No data for the subject {} with source {}", subjectId, sourceId);
            }

            return dataset;
        } else {
            return emptyDataset(subjectId, sourceId, sensor, stat, interval,
                    Instant.ofEpochMilli(start));
        }
    }

    private static Dataset emptyDataset(String subjectId, String sourceId, String sensor,
            DescriptiveStatistic stat, TimeWindow interval, Instant timeFrameStart) {
        String time = timeFrameStart.toString();
        return new Dataset(new Header(subjectId, sourceId, "UNKNOWN", sensor, stat,
                Unit.UNKNOWN, interval, new EffectiveTimeFrame(time, time)),
                Collections.emptyList());
    }
}
