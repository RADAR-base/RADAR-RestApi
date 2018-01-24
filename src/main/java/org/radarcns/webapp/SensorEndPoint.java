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

import static org.radarcns.auth.authorization.Permission.MEASUREMENT_READ;
import static org.radarcns.auth.authorization.RadarAuthorization.checkPermissionOnProject;
import static org.radarcns.security.utils.SecurityUtils.getRadarToken;
import static org.radarcns.service.SubjectService.checkSourceAssignedToSubject;
import static org.radarcns.service.SubjectService.getSourceFromSubject;
import static org.radarcns.webapp.util.BasePath.AVRO_BINARY;
import static org.radarcns.webapp.util.BasePath.DATA;
import static org.radarcns.webapp.util.BasePath.REALTIME;
import static org.radarcns.webapp.util.Parameter.END;
import static org.radarcns.webapp.util.Parameter.INTERVAL;
import static org.radarcns.webapp.util.Parameter.SOURCEDATATYPE;
import static org.radarcns.webapp.util.Parameter.SOURCE_ID;
import static org.radarcns.webapp.util.Parameter.START;
import static org.radarcns.webapp.util.Parameter.STAT;
import static org.radarcns.webapp.util.Parameter.SUBJECT_ID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.io.IOException;
import java.util.LinkedList;
import java.util.stream.Collectors;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.radarcns.auth.exception.NotAuthorizedException;
import org.radarcns.catalogue.TimeWindow;
import org.radarcns.dao.SubjectDataAccessObject;
import org.radarcns.exception.TokenException;
import org.radarcns.listener.ContextResourceManager;
import org.radarcns.listener.managementportal.ManagementPortalClient;
import org.radarcns.listener.managementportal.ManagementPortalClientManager;
import org.radarcns.managementportal.Source;
import org.radarcns.managementportal.SourceData;
import org.radarcns.managementportal.SourceType;
import org.radarcns.managementportal.SourceTypeIdentifier;
import org.radarcns.managementportal.Subject;
import org.radarcns.restapi.dataset.Dataset;
import org.radarcns.restapi.header.DescriptiveStatistic;
import org.radarcns.security.Param;
import org.radarcns.security.exception.AccessDeniedException;
import org.radarcns.service.SourceTypeService;
import org.radarcns.webapp.exception.NotFoundException;
import org.radarcns.webapp.util.ResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sensor web-app. Function set to access all data data.
 */
@Path("/" + DATA)
public class SensorEndPoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(SensorEndPoint.class);

    @Context
    private ServletContext context;
    @Context
    private HttpServletRequest request;

    //--------------------------------------------------------------------------------------------//
    //                                    REAL-TIME FUNCTIONS                                     //
    //--------------------------------------------------------------------------------------------//

    /**
     * JSON function that returns the last seen data value if available.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/" + REALTIME + "/{" + SOURCEDATATYPE + "}/{" + STAT + "}/{" + INTERVAL + "}/{"
            + SUBJECT_ID
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
    @ApiResponse(responseCode = "401", description = "Access denied error occured")
    @ApiResponse(responseCode = "403", description = "Not Authorised error occured")
    public Response getLastReceivedSampleJson(
            @PathParam(SOURCEDATATYPE) String sensor,
            @PathParam(STAT) DescriptiveStatistic stat,
            @PathParam(INTERVAL) TimeWindow interval,
            @PathParam(SUBJECT_ID) String subjectId,
            @PathParam(SOURCE_ID) String sourceId) {
        try {
            ManagementPortalClient client = ManagementPortalClientManager
                    .getManagementPortalClient(context);
            Subject sub = client.getSubject(subjectId);
            checkPermissionOnProject(getRadarToken(request), MEASUREMENT_READ,
                    sub.getProject().getProjectName());
            checkSourceAssignedToSubject(sub, sourceId);
            SourceTypeIdentifier sourceTypeIdentifier= SourceTypeService.getSourceTypeIdFromSource
                    (getSourceFromSubject(sub, sourceId));
            return ResponseHandler.getJsonResponse(request,
                    getLastReceivedSampleWorker(sub, sourceId, sensor, stat,
                            interval , sourceTypeIdentifier.toString()));
        } catch (AccessDeniedException exc) {
            LOGGER.error(exc.getMessage(), exc);
            return ResponseHandler.getJsonAccessDeniedResponse(request, exc.getMessage());
        } catch (NotAuthorizedException exc) {
            LOGGER.error(exc.getMessage(), exc);
            return ResponseHandler.getJsonNotAuthorizedResponse(request, exc.getMessage());
        } catch (Exception exec) {
            LOGGER.error(exec.getMessage(), exec);
            return ResponseHandler.getJsonErrorResponse(request, "Your request cannot be "
                    + "completed. If this error persists, please contact the service "
                    + "administrator.");
        }
    }

    /**
     * AVRO function that returns the last seen data value if available.
     */
    @GET
    @Produces(AVRO_BINARY)
    @Path("/" + REALTIME + "/{" + SOURCEDATATYPE + "}/{" + STAT + "}/{" + INTERVAL
            + "}/{" + SUBJECT_ID + "}/{" + SOURCE_ID + "}")
    @Operation(summary = "Returns a dataset object formatted in Apache AVRO.",
            description =
                    "Each collected sample is aggregated to provide near real-time statistical "
                            + "results. This end-point returns the last computed result of type "
                            + "stat for the "
                            + "given subjectID, sourceID, and sensor. Data can be queried using "
                            + "different "
                            + "time-frame resolutions. The response is formatted in Apache AVRO.")
    @ApiResponse(responseCode = "500", description = "An error occurs while executing")
    @ApiResponse(responseCode = "204", description = "No value for the given parameters.")
    @ApiResponse(responseCode = "200", description = "Returns a byte array serialising a "
            + "dataset.avsc object containing last computed sample for the given inputs formatted "
            + "either Acceleration.avsc or DoubleValue.avsc")
    @ApiResponse(responseCode = "401", description = "Access denied error occured")
    @ApiResponse(responseCode = "403", description = "Not Authorised error occured")
    public Response getLastReceivedSampleAvro(
            @PathParam(SOURCEDATATYPE) String sensor,
            @PathParam(STAT) DescriptiveStatistic stat,
            @PathParam(INTERVAL) TimeWindow interval,
            @PathParam(SUBJECT_ID) String subjectId,
            @PathParam(SOURCE_ID) String sourceId) {
        try {
            ManagementPortalClient client = ManagementPortalClientManager
                    .getManagementPortalClient(context);
            Subject sub = client.getSubject(subjectId);
            checkPermissionOnProject(getRadarToken(request), MEASUREMENT_READ,
                    sub.getProject().getProjectName());
            checkSourceAssignedToSubject(sub, sourceId);
            SourceTypeIdentifier sourceTypeIdentifier= SourceTypeService.getSourceTypeIdFromSource
                    (getSourceFromSubject(sub, sourceId));
            return ResponseHandler.getAvroResponse(request,
                    getLastReceivedSampleWorker(sub, sourceId, sensor, stat,
                            interval , sourceTypeIdentifier.toString()));
        } catch (AccessDeniedException exc) {
            LOGGER.error(exc.getMessage(), exc);
            return ResponseHandler.getJsonAccessDeniedResponse(request, exc.getMessage());
        } catch (NotAuthorizedException exc) {
            LOGGER.error(exc.getMessage(), exc);
            return ResponseHandler.getJsonNotAuthorizedResponse(request, exc.getMessage());
        } catch (Exception exec) {
            LOGGER.error(exec.getMessage(), exec);
            return ResponseHandler.getAvroErrorResponse(request);
        }
    }

    //--------------------------------------------------------------------------------------------//
    //                                   WHOLE-DATA FUNCTIONS                                     //
    //--------------------------------------------------------------------------------------------//

    /**
     * JSON function that returns all available samples for the given data.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{" + SOURCEDATATYPE + "}/{" + STAT + "}/{" + INTERVAL + "}/{" + SUBJECT_ID + "}/{"
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
    @ApiResponse(responseCode = "401", description = "Access denied error occured")
    @ApiResponse(responseCode = "403", description = "Not Authorised error occured")
    public Response getSamplesJson(
            @PathParam(SOURCEDATATYPE) String sensor,
            @PathParam(STAT) DescriptiveStatistic stat,
            @PathParam(INTERVAL) TimeWindow interval,
            @PathParam(SUBJECT_ID) String subjectId,
            @PathParam(SOURCE_ID) String sourceId) {
        try {
            ManagementPortalClient client = ManagementPortalClientManager
                    .getManagementPortalClient(context);
            Subject sub = client.getSubject(subjectId);
            checkPermissionOnProject(getRadarToken(request), MEASUREMENT_READ,
                    sub.getProject().getProjectName());
            checkSourceAssignedToSubject(sub, sourceId);
            SourceTypeIdentifier sourceTypeIdentifier= SourceTypeService.getSourceTypeIdFromSource
                    (getSourceFromSubject(sub, sourceId));
            return ResponseHandler.getJsonResponse(request,
                    getSamplesWorker(sub, sourceId, stat, interval, sensor , sourceTypeIdentifier
                            .toString()));
        } catch (AccessDeniedException exc) {
            LOGGER.error(exc.getMessage(), exc);
            return ResponseHandler.getJsonAccessDeniedResponse(request, exc.getMessage());
        } catch (NotAuthorizedException exc) {
            LOGGER.error(exc.getMessage(), exc);
            return ResponseHandler.getJsonNotAuthorizedResponse(request, exc.getMessage());
        } catch (Exception exec) {
            LOGGER.error(exec.getMessage(), exec);
            return ResponseHandler.getJsonErrorResponse(request, "Your request cannot be "
                    + "completed. If this error persists, please contact the service "
                    + "administrator.");
        }
    }

    /**
     * AVRO function that returns all available samples for the given data.
     */
    @GET
    @Produces(AVRO_BINARY)
    @Path("/{" + SOURCEDATATYPE + "}/{" + STAT + "}/{" + INTERVAL + "}/{" + SUBJECT_ID + "}/{"
            + SOURCE_ID + "}")
    @Operation(summary = "Returns a dataset object formatted in Apache AVRO.",
            description = "Each collected sample is aggregated to provide near real-time "
                    + "statistical "
                    + "results. This end-point returns all available results of type stat for the "
                    + "given subjectID, sourceID, and sensor. Data can be queried using different "
                    + "time-frame resolutions. The response is formatted in Apache AVRO.")
    @ApiResponse(responseCode = "500", description = "An error occurs while executing.")
    @ApiResponse(responseCode = "204", description = "No value for the given parameters.")
    @ApiResponse(responseCode = "200", description = "Returns a byte array serialising a "
            + "dataset.avsc object containing all available samples for the given inputs formatted "
            + "either Acceleration.avsc or DoubleValue.avsc")
    @ApiResponse(responseCode = "401", description = "Access denied error occured")
    @ApiResponse(responseCode = "403", description = "Not Authorised error occured")
    public Response getSamplesAvro(
            @PathParam(SOURCEDATATYPE) String sourceDataType,
            @PathParam(STAT) DescriptiveStatistic stat,
            @PathParam(INTERVAL) TimeWindow interval,
            @PathParam(SUBJECT_ID) String subjectId,
            @PathParam(SOURCE_ID) String sourceId) {
        try {
            ManagementPortalClient client = ManagementPortalClientManager
                    .getManagementPortalClient(context);
            Subject sub = client.getSubject(subjectId);
            checkPermissionOnProject(getRadarToken(request), MEASUREMENT_READ,
                    sub.getProject().getProjectName());
            checkSourceAssignedToSubject(sub, sourceId);
            SourceTypeIdentifier sourceTypeIdentifier= SourceTypeService.getSourceTypeIdFromSource
                    (getSourceFromSubject(sub, sourceId));
            return ResponseHandler.getAvroResponse(request,
                    getSamplesWorker(sub, sourceId, stat, interval, sourceDataType ,
                            sourceTypeIdentifier.toString()));
        } catch (AccessDeniedException exc) {
            LOGGER.error(exc.getMessage(), exc);
            return ResponseHandler.getJsonAccessDeniedResponse(request, exc.getMessage());
        } catch (NotAuthorizedException exc) {
            LOGGER.error(exc.getMessage(), exc);
            return ResponseHandler.getJsonNotAuthorizedResponse(request, exc.getMessage());
        } catch (Exception exec) {
            LOGGER.error(exec.getMessage(), exec);
            return ResponseHandler.getAvroErrorResponse(request);
        }
    }

    //--------------------------------------------------------------------------------------------//
    //                                 WINDOWED-DATA FUNCTIONS                                    //
    //--------------------------------------------------------------------------------------------//

    /**
     * JSON function that returns all data value inside the time-window [start-end].
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{" + SOURCEDATATYPE + "}/{" + STAT + "}/{" + INTERVAL + "}/{" + SUBJECT_ID + "}/{"
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
    @ApiResponse(responseCode = "401", description = "Access denied error occured")
    @ApiResponse(responseCode = "403", description = "Not Authorised error occured")
    public Response getSamplesWithinWindowJson(
            @PathParam(SOURCEDATATYPE) String sensor,
            @PathParam(STAT) DescriptiveStatistic stat,
            @PathParam(SUBJECT_ID) String subjectId,
            @PathParam(SOURCE_ID) String sourceId,
            @PathParam(INTERVAL) TimeWindow interval,
            @PathParam(START) long start,
            @PathParam(END) long end) {
        try {
            ManagementPortalClient client = ManagementPortalClientManager
                    .getManagementPortalClient(context);
            Subject sub = client.getSubject(subjectId);
            checkPermissionOnProject(getRadarToken(request), MEASUREMENT_READ,
                    sub.getProject().getProjectName());
            checkSourceAssignedToSubject(sub, sourceId);
            SourceTypeIdentifier sourceTypeIdentifier= SourceTypeService.getSourceTypeIdFromSource
                    (getSourceFromSubject(sub, sourceId));
            return ResponseHandler
                    .getJsonResponse(request, getSamplesWithinWindowWorker(sub, sourceId, stat,
                            interval, sensor, start, end , sourceTypeIdentifier.toString()));
        } catch (AccessDeniedException exc) {
            LOGGER.error(exc.getMessage(), exc);
            return ResponseHandler.getJsonAccessDeniedResponse(request, exc.getMessage());
        } catch (NotAuthorizedException exc) {
            LOGGER.error(exc.getMessage(), exc);
            return ResponseHandler.getJsonNotAuthorizedResponse(request, exc.getMessage());
        } catch (Exception exec) {
            LOGGER.error(exec.getMessage(), exec);
            return ResponseHandler.getJsonErrorResponse(request, "Your request cannot be "
                    + "completed. If this error persists, please contact the service "
                    + "administrator.");
        }
    }

    /**
     * AVRO function that returns all data value inside the time-window [start-end].
     */
    @GET
    @Produces(AVRO_BINARY)
    @Path("/{" + SOURCEDATATYPE + "}/{" + STAT + "}/{" + INTERVAL + "}/{" + SUBJECT_ID + "}/{"
            + SOURCE_ID + "}/{" + START + "}/{" + END + "}")
    @Operation(summary = "Returns a dataset object formatted in Apache AVRO.",
            description = "Each collected sample is aggregated to provide near real-time "
                    + "statistical "
                    + "results. This end-point returns all available results of type stat for the "
                    + "given subjectID, sourceID, and sensor belonging to the time window "
                    + "[start - end]. Data can be queried using different time-frame resolutions. "
                    + "The response is formatted in Apache AVRO.")
    @ApiResponse(responseCode = "500", description = "An error occurs while executing")
    @ApiResponse(responseCode = "204", description = "No value for the given parameters")
    @ApiResponse(responseCode = "200", description =
            "Returns a byte array serialising a dataset.avsc "
                    + "object containing samples belonging to the time window [start - end] for "
                    + "the "
                    + "given inputs formatted either Acceleration.avsc or DoubleValue.avsc.")
    @ApiResponse(responseCode = "401", description = "Access denied error occured")
    @ApiResponse(responseCode = "403", description = "Not Authorised error occured")
    public Response getSamplesWithinWindowAvro(
            @PathParam(SOURCEDATATYPE) String sensor,
            @PathParam(STAT) DescriptiveStatistic stat,
            @PathParam(INTERVAL) TimeWindow interval,
            @PathParam(SUBJECT_ID) String subjectId,
            @PathParam(SOURCE_ID) String sourceId,
            @PathParam(START) long start,
            @PathParam(END) long end) {
        try {
            ManagementPortalClient client = ManagementPortalClientManager
                    .getManagementPortalClient(context);
            Subject sub = client.getSubject(subjectId);
            checkPermissionOnProject(getRadarToken(request), MEASUREMENT_READ,
                    sub.getProject().getProjectName());
            checkSourceAssignedToSubject(sub, sourceId);
            SourceTypeIdentifier sourceTypeIdentifier= SourceTypeService.getSourceTypeIdFromSource
                    (getSourceFromSubject(sub, sourceId));
            return ResponseHandler
                    .getAvroResponse(request, getSamplesWithinWindowWorker(sub, sourceId, stat,
                            interval, sensor, start, end, sourceTypeIdentifier.toString()));
        } catch (AccessDeniedException exc) {
            LOGGER.error(exc.getMessage(), exc);
            return ResponseHandler.getJsonAccessDeniedResponse(request, exc.getMessage());
        } catch (NotAuthorizedException exc) {
            LOGGER.error(exc.getMessage(), exc);
            return ResponseHandler.getJsonNotAuthorizedResponse(request, exc.getMessage());
        } catch (Exception exec) {
            LOGGER.error(exec.getMessage(), exec);
            return ResponseHandler.getAvroErrorResponse(request);
        }
    }


    public Dataset getSamplesWorker(Subject subject, String source, DescriptiveStatistic stat,
            TimeWindow interval, String sensor , String sourceTypeId)
            throws IOException, NotFoundException, TokenException {

        Dataset dataset = new Dataset(null, new LinkedList<>());
        SourceData sourceData = this.getSourceDataForRequest(subject, source, sensor);
        if (ContextResourceManager.getSubjectDataAccessObject(context).exist(subject.getId(), context)) {
            dataset = ContextResourceManager.getSensorDataAccessObject(context)
                    .getSamples(subject.getId(),
                            source, stat, interval, sourceData, context , sourceTypeId);

            if (dataset.getDataset().isEmpty()) {
                LOGGER.debug("No data for the subject {} with source {}", subject, source);
            }
        }

        return dataset;
    }

    private SourceData getSourceDataForRequest(Subject subject, String sourceId, String
            sourceDataType) throws IOException, TokenException, NotFoundException {
        Source source = subject.getSources().stream().filter(p -> p.getSourceId().equals
                (sourceId))
                .collect(Collectors.toList()).get(0);
        SourceType sourceType = ContextResourceManager.getSourceCatalogue(context)
                .getSourceType(source.getSourceTypeProducer(), source
                        .getSourceTypeModel(), source.getSourceTypeCatalogVersion());
        // assuming only one would fit, having sourceDataName as the param is better since it
        // is unique. TODO discuss this
        return sourceType.getSourceData().stream().filter(p -> p
                .getSourceDataType().equals(sourceDataType)).collect(Collectors.toList()).get(0);
    }

    /**
     * Actual implementation of AVRO and JSON getBySubjectForWindow.
     **/
    public Dataset getSamplesWithinWindowWorker(Subject subject, String sourceId,
            DescriptiveStatistic stat, TimeWindow interval, String sensor, long start,
            long end , String sourceTypeId) throws IOException, TokenException, NotFoundException {
        // assuming only one would fit

        SourceData sourceData = this.getSourceDataForRequest(subject, sourceId, sensor);
        Param.isValidInput(subject.getId(), sourceId);

        Dataset dataset = new Dataset(null, new LinkedList<>());

        dataset = ContextResourceManager.getSensorDataAccessObject(context).getSamples(
                subject.getId(), sourceId, stat, interval, start, end, sourceData, context , sourceTypeId);

        if (dataset.getDataset().isEmpty()) {
            LOGGER.debug("No data for the subject {} with source {}", subject, sourceId);
        }

        return dataset;
    }

    /**
     * Actual implementation of AVRO and JSON getRealTimeSubject.
     **/
    public Dataset getLastReceivedSampleWorker(Subject subject, String source,
            String sourceDataType,
            DescriptiveStatistic stat, TimeWindow interval , String sourceTypeId)
            throws IOException, NotFoundException, TokenException {
        Param.isValidInput(subject.getId(), source);

        Dataset dataset = new Dataset(null, new LinkedList<>());
        SourceData sourceData = this.getSourceDataForRequest(subject, source, sourceDataType);
        if (ContextResourceManager.getSubjectDataAccessObject(context).exist(subject.getId(), context)) {
            dataset = ContextResourceManager.getSensorDataAccessObject(context)
                    .getLastReceivedSample(subject.getId(), source,
                            stat, interval, sourceData, context , sourceTypeId);

            if (dataset.getDataset().isEmpty()) {
                LOGGER.debug("No data for the subject {} with source {}", subject, source);
            }
        }

        return dataset;
    }


}
