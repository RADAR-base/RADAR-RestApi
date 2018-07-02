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
import static org.radarcns.domain.restapi.TimeWindow.TEN_SECOND;
import static org.radarcns.service.DataSetService.emptyDataset;
import static org.radarcns.webapp.param.TimeScaleParser.MAX_NUMBER_OF_WINDOWS;
import static org.radarcns.webapp.resource.BasePath.AVRO_BINARY;
import static org.radarcns.webapp.resource.BasePath.DATA;
import static org.radarcns.webapp.resource.BasePath.LATEST;
import static org.radarcns.webapp.resource.Parameter.END;
import static org.radarcns.webapp.resource.Parameter.PROJECT_NAME;
import static org.radarcns.webapp.resource.Parameter.SOURCE_DATA_NAME;
import static org.radarcns.webapp.resource.Parameter.SOURCE_ID;
import static org.radarcns.webapp.resource.Parameter.START;
import static org.radarcns.webapp.resource.Parameter.STAT;
import static org.radarcns.webapp.resource.Parameter.SUBJECT_ID;
import static org.radarcns.webapp.resource.Parameter.TIME_WINDOW;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.io.IOException;
import java.time.Instant;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import org.radarcns.auth.NeedsPermissionOnSubject;
import org.radarcns.domain.restapi.TimeWindow;
import org.radarcns.domain.restapi.dataset.Dataset;
import org.radarcns.domain.restapi.header.DescriptiveStatistic;
import org.radarcns.domain.restapi.header.TimeFrame;
import org.radarcns.listener.managementportal.ManagementPortalClient;
import org.radarcns.service.DataSetService;
import org.radarcns.util.RadarConverter;
import org.radarcns.webapp.filter.Authenticated;
import org.radarcns.webapp.param.InstantParam;
import org.radarcns.webapp.param.TimeScaleParser;
import org.radarcns.webapp.param.TimeScale;
import org.radarcns.webapp.validation.Alphanumeric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sensor web-app. Function set to access all data data.
 */
@Authenticated
@Path('/' + DATA)
public class DataSetEndPoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataSetEndPoint.class);

    @Inject
    private ManagementPortalClient mpClient;

    @Inject
    private DataSetService dataSetService;

    @Inject
    private TimeScaleParser timeScaleParser;

    /**
     * Last seen data value if available.
     */
    @GET
    @Produces({APPLICATION_JSON, AVRO_BINARY})
    @Path("/{" + PROJECT_NAME + "}/{" + SUBJECT_ID + "}/{" + SOURCE_ID + "}/{" + SOURCE_DATA_NAME
            + "}/{" + STAT + "}/" + LATEST)
    @Operation(summary = "Returns a Dataset object formatted in JSON.",
            description = "Each collected sample is aggregated to provide near real-time "
                    + "statistical results. This end-point returns the latest available record "
                    + "for the stat for the given projectID, subjectID, sourceID and SourceDataName"
                    + " Data can be queried using different time-frame resolutions.")
    @ApiResponse(responseCode = "500", description = "An error occurs while executing")
    @ApiResponse(responseCode = "200", description = "Returns a dataset object containing latest "
            + "available record for the given inputs")
    @ApiResponse(responseCode = "401", description = "Access denied error occurred")
    @ApiResponse(responseCode = "403", description = "Not Authorised error occurred")
    @ApiResponse(responseCode = "404", description = "Subject not found.")
    @NeedsPermissionOnSubject(entity = MEASUREMENT, operation = READ)
    public Dataset getLastReceivedSampleJson(
            @Alphanumeric @PathParam(PROJECT_NAME) String projectName,
            @Alphanumeric @PathParam(SUBJECT_ID) String subjectId,
            @Alphanumeric @PathParam(SOURCE_ID) String sourceId,
            @Alphanumeric @PathParam(SOURCE_DATA_NAME) String sourceDataName,
            @PathParam(STAT) DescriptiveStatistic stat,
            @QueryParam(TIME_WINDOW) TimeWindow interval) throws IOException {
        // todo: 404 if given source does not exist.
        // Note that a source doesn't necessarily need to be linked anymore, as long as it exists
        // and historical data of it is linked to the given user.
        mpClient.getSubject(subjectId);
        // if timeWindow is not set use default TEN_SECOND
        TimeWindow timeWindow = interval != null ? interval : TEN_SECOND;

        Dataset dataset = this.dataSetService
                .getLastReceivedSample(projectName, subjectId, sourceId, sourceDataName, stat,
                        timeWindow);
        if (dataset.getDataset().isEmpty()) {
            LOGGER.debug("No data for the subject {} with source {}", subjectId, sourceId);
            Instant now = Instant.now();
            return emptyDataset(projectName, subjectId, sourceId, sourceDataName, stat, interval,
                    new TimeFrame(now.minus(RadarConverter.getDuration(timeWindow)), now));
        }

        return dataset;
    }

    /**
     * All available records for the given data.
     */
    @GET
    @Produces({APPLICATION_JSON, AVRO_BINARY})
    @Path("/{" + PROJECT_NAME + "}/{" + SUBJECT_ID + "}/{" + SOURCE_ID + "}/{" + SOURCE_DATA_NAME
            + "}/{" + STAT + "}")
    @Operation(summary = "Returns a Dataset object formatted in JSON.",
            description = "Each collected sample is aggregated to provide near real-time "
                    + "statistical results. This end-point returns the all available record "
                    + "for the stat for the given projectID, subjectID, sourceID and SourceDataName"
                    + "Data can be queried using different time-frame resolutions.  If endTime is"
                    + "not provided the data will be calculated from current timestamp. If the "
                    + "startTime is not provided startTime will be calculated based on default "
                    + "number of windows and given timeWindow. If no timeWindow is provided, a "
                    + "best fitting timeWindow will be calculated. If none of the parameters are "
                    + "provided, API will return data for a period of 1 year with ONE_WEEK of "
                    + "timeWindow (~52 records) from current timestamp.")
    @ApiResponse(responseCode = "500", description = "An error occurs while executing")
    @ApiResponse(responseCode = "200", description = "Returns a dataset object containing all "
            + "available record for the given inputs")
    @ApiResponse(responseCode = "400", description = "startTime should not be after endTime in "
            + "query and the maximum number of time windows should not exceed "
            + MAX_NUMBER_OF_WINDOWS + ".")
    @ApiResponse(responseCode = "401", description = "Access denied error occurred")
    @ApiResponse(responseCode = "403", description = "Not Authorised error occurred")
    @ApiResponse(responseCode = "404", description = "Subject not found.")
    @NeedsPermissionOnSubject(entity = MEASUREMENT, operation = READ)
    @Inject
    public Dataset getSamplesJson(
            @Alphanumeric @PathParam(PROJECT_NAME) String projectName,
            @Alphanumeric @PathParam(SUBJECT_ID) String subjectId,
            @Alphanumeric @PathParam(SOURCE_ID) String sourceId,
            @Alphanumeric @PathParam(SOURCE_DATA_NAME) String sourceDataName,
            @PathParam(STAT) DescriptiveStatistic stat,
            @QueryParam(TIME_WINDOW) TimeWindow interval,
            @QueryParam(START) InstantParam start,
            @QueryParam(END) InstantParam end) throws IOException {
        // todo: 404 if given source does not exist.
        // Note that a source doesn't necessarily need to be linked anymore, as long as it exists
        // and historical data of it is linked to the given user.
        mpClient.checkSubjectInProject(projectName, subjectId);
        Dataset dataset;

        TimeScale timeScale = timeScaleParser.parse(start, end, interval);

        dataset = dataSetService
                .getAllRecordsInWindow(projectName, subjectId, sourceId, sourceDataName, stat,
                        timeScale.getTimeWindow(), timeScale.getTimeFrame());

        if (dataset.getDataset().isEmpty()) {
            LOGGER.debug("No data for the subject {} with source {}", subjectId, sourceId);
            return emptyDataset(projectName, subjectId, sourceId, sourceDataName, stat, interval,
                    new TimeFrame());
        }

        return dataset;

    }
}
