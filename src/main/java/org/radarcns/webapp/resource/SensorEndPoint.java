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
import static org.radarcns.webapp.resource.BasePath.AVRO_BINARY;
import static org.radarcns.webapp.resource.BasePath.DATA;
import static org.radarcns.webapp.resource.BasePath.REALTIME;
import static org.radarcns.webapp.resource.Parameter.END;
import static org.radarcns.webapp.resource.Parameter.INTERVAL;
import static org.radarcns.webapp.resource.Parameter.PROJECT_NAME;
import static org.radarcns.webapp.resource.Parameter.SOURCEDATATYPE;
import static org.radarcns.webapp.resource.Parameter.SOURCE_ID;
import static org.radarcns.webapp.resource.Parameter.START;
import static org.radarcns.webapp.resource.Parameter.STAT;
import static org.radarcns.webapp.resource.Parameter.SUBJECT_ID;

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
import org.radarcns.auth.NeedsPermissionOnSubject;
import org.radarcns.catalog.SourceCatalog;
import org.radarcns.dao.SourceDataValueAccessObject;
import org.radarcns.domain.restapi.TimeWindow;
import org.radarcns.domain.restapi.dataset.Dataset;
import org.radarcns.domain.restapi.header.DescriptiveStatistic;
import org.radarcns.domain.restapi.header.EffectiveTimeFrame;
import org.radarcns.domain.restapi.header.Header;
import org.radarcns.listener.managementportal.ManagementPortalClient;
import org.radarcns.webapp.filter.Authenticated;
import org.radarcns.webapp.validation.Alphanumeric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sensor web-app. Function set to access all data data.
 */
@Authenticated
@Path("/" + DATA)
public class SensorEndPoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(SensorEndPoint.class);

    @Inject
    private MongoClient mongoClient;

    @Inject
    private ManagementPortalClient mpClient;

    @Inject
    private SourceCatalog sourceCatalog;

    @Inject
    private SourceDataValueAccessObject sourceDataValueAccessObject;

    //--------------------------------------------------------------------------------------------//
    //                                    REAL-TIME FUNCTIONS                                     //
    //--------------------------------------------------------------------------------------------//

    /**
     * JSON function that returns the last seen data value if available.
     */
    @GET
    @Produces({APPLICATION_JSON, AVRO_BINARY})
    @Path("/" + REALTIME + "/{" + SOURCEDATATYPE + "}/{" + STAT + "}/{" + INTERVAL + "}/{" + PROJECT_NAME
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
    @ApiResponse(responseCode = "200", description =
            "Returns a dataset.avsc object containing last "
                    + "computed sample for the given inputs formatted either Acceleration.avsc or "
                    + "DoubleValue.avsc")
    @ApiResponse(responseCode = "401", description = "Access denied error occurred")
    @ApiResponse(responseCode = "403", description = "Not Authorised error occurred")
    @ApiResponse(responseCode = "404", description = "Subject not found.")
    @NeedsPermissionOnSubject(entity = MEASUREMENT, operation = READ)
    public Dataset getLastReceivedSampleJson(
            @Alphanumeric @PathParam(SOURCEDATATYPE) String sensor,
            @PathParam(STAT) DescriptiveStatistic stat,
            @PathParam(INTERVAL) TimeWindow interval,
            @Alphanumeric @PathParam(PROJECT_NAME) String projectName,
            @Alphanumeric @PathParam(SUBJECT_ID) String subjectId,
            @Alphanumeric @PathParam(SOURCE_ID) String sourceId) throws IOException {
        // todo: 404 if given source does not exist.
        // Note that a source doesn't necessarily need to be linked anymore, as long as it exists
        // and historical data of it is linked to the given user.
        mpClient.getSubject(subjectId);

            Dataset dataset = this.sourceDataValueAccessObject
                    .getLastReceivedSample(subjectId, sourceId,
                            stat, interval, this.sourceCatalog.getSourceData(sensor), mongoClient
                            , null); //TODO fetch source-type

            if (dataset.getDataset().isEmpty()) {
                LOGGER.debug("No data for the subject {} with source {}", subjectId, sourceId);
                return emptyDataset(subjectId, sourceId, sensor, stat, interval,
                        Instant.now());
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
    @Produces({APPLICATION_JSON, AVRO_BINARY})
    @Path("/{" + SOURCEDATATYPE + "}/{" + STAT + "}/{" + INTERVAL + "}/{" + PROJECT_NAME + "}/{"
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
    @ApiResponse(responseCode = "200", description = "Returns a dataset.avsc object containing all "
            + "available samples for the given inputs formatted either Acceleration.avsc or "
            + "DoubleValue.avsc")
    @ApiResponse(responseCode = "401", description = "Access denied error occurred")
    @ApiResponse(responseCode = "403", description = "Not Authorised error occurred")
    @ApiResponse(responseCode = "404", description = "Subject not found.")
    @NeedsPermissionOnSubject(entity = MEASUREMENT, operation = READ)
    public Dataset getSamplesJson(
            @Alphanumeric @PathParam(SOURCEDATATYPE) String sensor,
            @PathParam(STAT) DescriptiveStatistic stat,
            @PathParam(INTERVAL) TimeWindow interval,
            @Alphanumeric @PathParam(PROJECT_NAME) String projectName,
            @Alphanumeric @PathParam(SUBJECT_ID) String subjectId,
            @Alphanumeric @PathParam(SOURCE_ID) String sourceId) throws IOException {
        // todo: 404 if given source does not exist.
        // Note that a source doesn't necessarily need to be linked anymore, as long as it exists
        // and historical data of it is linked to the given user.
        mpClient.getSubject(subjectId);

            Dataset dataset = sourceDataValueAccessObject.getSamples(subjectId,
                    sourceId, stat, interval, this.sourceCatalog.getSourceData(sensor), mongoClient ,
                    null);

            if (dataset.getDataset().isEmpty()) {
                LOGGER.debug("No data for the subject {} with source {}", subjectId, sourceId);
                return emptyDataset(subjectId, sourceId, sensor, stat, interval,
                        Instant.now());
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
    @Produces({APPLICATION_JSON, AVRO_BINARY})
    @Path("/{" + SOURCEDATATYPE + "}/{" + STAT + "}/{" + INTERVAL + "}/{" + PROJECT_NAME + "}/{"
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
    @ApiResponse(responseCode = "200", description = "Returns a dataset.avsc object containing "
            + "samples "
            + "belonging to the time window [start - end] for the given inputs formatted "
            + "either Acceleration.avsc or DoubleValue.avsc.")
    @ApiResponse(responseCode = "401", description = "Access denied error occurred")
    @ApiResponse(responseCode = "403", description = "Not Authorised error occurred")
    @ApiResponse(responseCode = "404", description = "Subject not found.")
    @NeedsPermissionOnSubject(entity = MEASUREMENT, operation = READ)
    public Dataset getSamplesWithinWindowJson(
            @Alphanumeric @PathParam(SOURCEDATATYPE) String sensor,
            @PathParam(STAT) DescriptiveStatistic stat,
            @Alphanumeric @PathParam(PROJECT_NAME) String projectName,
            @Alphanumeric @PathParam(SUBJECT_ID) String subjectId,
            @Alphanumeric @PathParam(SOURCE_ID) String sourceId,
            @PathParam(INTERVAL) TimeWindow interval,
            @PathParam(START) long start,
            @PathParam(END) long end) throws IOException {
        // todo: 404 if given source does not exist.
        // Note that a source doesn't necessarily need to be linked anymore, as long as it exists
        // and historical data of it is linked to the given user.
        mpClient.getSubject(subjectId);

            Dataset dataset = sourceDataValueAccessObject.getSamples(
                    subjectId, sourceId, stat, interval, start, end, this.sourceCatalog
                            .getSourceData(sensor),
                    mongoClient , null);

            if (dataset.getDataset().isEmpty()) {
                LOGGER.debug("No data for the subject {} with source {}", subjectId, sourceId);
                return emptyDataset(subjectId, sourceId, sensor, stat, interval,
                        Instant.ofEpochMilli(start));
            }

            return dataset;

    }

    private static Dataset emptyDataset(String subjectId, String sourceId, String sensor,
            DescriptiveStatistic stat, TimeWindow interval, Instant timeFrameStart) {
        String time = timeFrameStart.toString();
        return new Dataset(new Header(subjectId, sourceId, "UNKNOWN", sensor, stat,
                null, interval, new EffectiveTimeFrame(time, time)),
                Collections.emptyList());
    }
}
