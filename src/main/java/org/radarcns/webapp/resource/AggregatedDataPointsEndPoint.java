package org.radarcns.webapp.resource;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.radarcns.auth.authorization.Permission.Entity.MEASUREMENT;
import static org.radarcns.auth.authorization.Permission.Operation.READ;
import static org.radarcns.domain.restapi.TimeWindow.ONE_WEEK;
import static org.radarcns.service.DataSetService.emptyAggregatedData;
import static org.radarcns.webapp.resource.BasePath.AGGREGATE;
import static org.radarcns.webapp.resource.BasePath.DISTINCT;
import static org.radarcns.webapp.resource.Parameter.END;
import static org.radarcns.webapp.resource.Parameter.PROJECT_NAME;
import static org.radarcns.webapp.resource.Parameter.START;
import static org.radarcns.webapp.resource.Parameter.SUBJECT_ID;
import static org.radarcns.webapp.resource.Parameter.TIME_WINDOW;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.io.IOException;
import java.time.Instant;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import org.radarcns.auth.NeedsPermissionOnSubject;
import org.radarcns.domain.restapi.TimeWindow;
import org.radarcns.domain.restapi.dataset.AggregatedDataPoints;
import org.radarcns.domain.restapi.header.TimeFrame;
import org.radarcns.listener.managementportal.ManagementPortalClient;
import org.radarcns.service.DataSetService;
import org.radarcns.util.RadarConverter;
import org.radarcns.webapp.filter.Authenticated;
import org.radarcns.webapp.param.DataAggregateParam;
import org.radarcns.webapp.param.InstantParam;
import org.radarcns.webapp.validation.Alphanumeric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Authenticated
@Path("/" + AGGREGATE)
public class AggregatedDataPointsEndPoint {

    private static final int DEFAULT_NUMBER_OF_WINDOWS = 100;

    private static final Logger LOGGER = LoggerFactory
            .getLogger(AggregatedDataPointsEndPoint.class);

    @Inject
    private ManagementPortalClient mpClient;

    @Inject
    private DataSetService dataSetService;

    //--------------------------------------------------------------------------------------------//
    //                                    Aggregated Data Points API functions                    //
    //--------------------------------------------------------------------------------------------//

    /**
     * JSON function that returns aggregated volumes of source-data requested.
     */
    @POST
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    @Path("/{" + PROJECT_NAME + "}/{" + SUBJECT_ID + "}/" + DISTINCT)
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
    public AggregatedDataPoints getDistinctDataPoints(DataAggregateParam aggregateParam,
            @Alphanumeric @PathParam(PROJECT_NAME) String projectName,
            @Alphanumeric @PathParam(SUBJECT_ID) String subjectId,
            @QueryParam(TIME_WINDOW) TimeWindow interval,
            @QueryParam(START) InstantParam start,
            @QueryParam(END) InstantParam end) throws IOException {

        mpClient.checkSubjectInProject(projectName, subjectId);

        // Don't request future data
        Instant endTime = end != null ? end.getValue() : Instant.now();

        TimeWindow timeWindow = interval;
        Instant startTime = start != null ? start.getValue() : null;
        TimeFrame timeFrame = new TimeFrame(startTime, endTime);

        if (startTime != null && startTime.isAfter(endTime)) {
            // don't mix up time frame order
            timeFrame.setStartDateTime(endTime);
        } else if (startTime == null && timeWindow == null) {
            // default settings, 1 year with 1 week intervals
            timeFrame.setStartDateTime(endTime.minus(Period.ofYears(1)));
            timeWindow = ONE_WEEK;
        } else if (startTime == null) {
            // use a fixed number of windows.
            timeFrame.setStartDateTime(endTime.minus(
                    RadarConverter.getSecond(timeWindow) * DEFAULT_NUMBER_OF_WINDOWS,
                    ChronoUnit.SECONDS));
        } else if (timeWindow == null) {
            // use the fixed time frame with a time frame close to the number of windows.
            timeWindow = dataSetService.getFittingTimeWindow(timeFrame, DEFAULT_NUMBER_OF_WINDOWS);
        }

        AggregatedDataPoints dataSet = this.dataSetService.getDistinctData(projectName, subjectId,
                aggregateParam.getSources(), timeWindow, timeFrame);
        if (dataSet.getDataset().isEmpty()) {
            LOGGER.debug("No aggregated data available for the subject {} with source", subjectId);
            return emptyAggregatedData(projectName, subjectId, timeWindow,
                    new TimeFrame(startTime, endTime), aggregateParam.getSources());
        }
        return dataSet;
    }
}
