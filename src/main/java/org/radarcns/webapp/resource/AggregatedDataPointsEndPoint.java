package org.radarcns.webapp.resource;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.radarcns.auth.authorization.Permission.Entity.MEASUREMENT;
import static org.radarcns.auth.authorization.Permission.Operation.READ;
import static org.radarcns.service.DataSetService.emptyAggregatedData;
import static org.radarcns.webapp.param.TimeScaleParser.MAX_NUMBER_OF_WINDOWS;
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
import org.radarcns.listener.managementportal.ManagementPortalClient;
import org.radarcns.service.DataSetService;
import org.radarcns.util.TimeScale;
import org.radarcns.webapp.filter.Authenticated;
import org.radarcns.webapp.param.DataAggregateParam;
import org.radarcns.webapp.param.InstantParam;
import org.radarcns.webapp.param.TimeScaleParser;
import org.radarcns.webapp.validation.Alphanumeric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Authenticated
@Path("/" + AGGREGATE)
public class AggregatedDataPointsEndPoint {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(AggregatedDataPointsEndPoint.class);

    @Inject
    private ManagementPortalClient mpClient;

    @Inject
    private DataSetService dataSetService;

    @Inject
    private TimeScaleParser timeScaleParser;

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
                    + " Data can be queried using different time-frame resolutions. If the "
                    + "startTime is not provided startTime will be calculated based on default "
                    + "number of windows and given timeWindow. If no timeWindow is provided, a "
                    + "best fitting timeWindow will be calculated. If none of the parameters are "
                    + "provided, API will return data for a period of 1 year with ONE_WEEK of "
                    + "timeWindow (~52 records) from current timestamp.")
    @ApiResponse(responseCode = "500", description = "An error occurs while executing")
    @ApiResponse(responseCode = "200", description = "Returns a dataset object containing latest "
            + "available record for the given inputs")
    @ApiResponse(responseCode = "400", description = "startTime should not be after endTime in "
            + "query and the maximum number of time windows should not exceed "
            + MAX_NUMBER_OF_WINDOWS + ".")
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

        TimeScale timeScale = timeScaleParser.parse(start, end, interval);

        AggregatedDataPoints dataSet = dataSetService.getDistinctData(
                projectName, subjectId, aggregateParam.getSources(), timeScale);

        if (dataSet.getDataset().isEmpty()) {
            LOGGER.debug("No aggregated data available for the subject {} with source", subjectId);
            return emptyAggregatedData(projectName, subjectId, timeScale,
                    aggregateParam.getSources());
        }
        return dataSet;
    }
}
