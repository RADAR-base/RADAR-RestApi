package org.radarcns.webapp.resource;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.radarcns.auth.authorization.Permission.Entity.MEASUREMENT;
import static org.radarcns.auth.authorization.Permission.Operation.READ;
import static org.radarcns.domain.restapi.TimeWindow.TEN_SECOND;
import static org.radarcns.service.DataSetService.emptyDataset;
import static org.radarcns.webapp.resource.BasePath.AVRO_BINARY;
import static org.radarcns.webapp.resource.BasePath.DATA;
import static org.radarcns.webapp.resource.BasePath.LATEST;
import static org.radarcns.webapp.resource.Parameter.PROJECT_NAME;
import static org.radarcns.webapp.resource.Parameter.SOURCE_DATA_NAME;
import static org.radarcns.webapp.resource.Parameter.SOURCE_ID;
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
import org.radarcns.webapp.validation.Alphanumeric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Authenticated
@Path("/" + DATA)
public class DataAggregateEndPoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataAggregateEndPoint.class);

    @Inject
    private ManagementPortalClient mpClient;

    @Inject
    private DataSetService dataSetService;


    //--------------------------------------------------------------------------------------------//
    //                                    Volume API functions                                    //
    //--------------------------------------------------------------------------------------------//

    /**
     * JSON function that returns the last seen data value if available.
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



}
