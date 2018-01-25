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

import static org.radarcns.auth.authorization.Permission.SOURCE_READ;
import static org.radarcns.auth.authorization.RadarAuthorization.checkPermission;
import static org.radarcns.auth.authorization.RadarAuthorization.checkPermissionOnProject;
import static org.radarcns.security.utils.SecurityUtils.getRadarToken;
import static org.radarcns.webapp.util.BasePath.AVRO_BINARY;
import static org.radarcns.webapp.util.BasePath.GET_ALL_SOURCES;
import static org.radarcns.webapp.util.BasePath.SOURCE;
import static org.radarcns.webapp.util.BasePath.SPECIFICATION;
import static org.radarcns.webapp.util.BasePath.STATE;
import static org.radarcns.webapp.util.Parameter.SOURCE_ID;
import static org.radarcns.webapp.util.Parameter.SOURCE_TYPE;
import static org.radarcns.webapp.util.Parameter.SUBJECT_ID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.io.IOException;
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
import javax.ws.rs.core.Response.Status;
import org.radarcns.auth.exception.NotAuthorizedException;
import org.radarcns.domain.managementportal.SourceType;
import org.radarcns.domain.restapi.Source;
import org.radarcns.domain.restapi.Subject;
import org.radarcns.exception.TokenException;
import org.radarcns.listener.ContextResourceManager;
import org.radarcns.listener.managementportal.ManagementPortalClient;
import org.radarcns.listener.managementportal.ManagementPortalClientManager;
import org.radarcns.monitor.Monitors;
import org.radarcns.security.Param;
import org.radarcns.security.exception.AccessDeniedException;
import org.radarcns.webapp.util.ResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SourceDefinition web-app. Function set to access sourceType information.
 */
@Path("/" + SOURCE)
public class SourceEndPoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(SourceEndPoint.class);

    @Context
    private ServletContext context;
    @Context
    private HttpServletRequest request;

    //--------------------------------------------------------------------------------------------//
    //                                       STATE FUNCTIONS                                      //
    //--------------------------------------------------------------------------------------------//

    /**
     * JSON function that returns the status of the given sourceType.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/" + STATE + "/{" + SUBJECT_ID + "}/{" + SOURCE_ID + "}")
    @Operation(summary = "Return a SourceDefinition values",
            description = "Using the sourceType sensors values arrived within last 60sec, it computes "
                    + "the"
                    + "sender status for the given subjectID and sourceID")
    @ApiResponse(responseCode = "500", description = "An error occurs while executing, in the body"
            + "there is a message.avsc object with more details")
    @ApiResponse(responseCode = "204", description = "No value for the given parameters, in the "
            + "body"
            + "there is a message.avsc object with more details")
    @ApiResponse(responseCode = "200", description = "Return a sourceType.avsc object containing last"
            + "computed status")
    @ApiResponse(responseCode = "401", description = "Access denied error occured")
    @ApiResponse(responseCode = "403", description = "Not Authorised error occured")
    public Response getLastComputedSourceStatusJson(
            @PathParam(SUBJECT_ID) String subjectId,
            @PathParam(SOURCE_ID) String sourceId) {
        try {
            ManagementPortalClient client = ManagementPortalClientManager
                    .getManagementPortalClient(context);
            org.radarcns.domain.managementportal.Subject sub = client.getSubject(subjectId);
            checkPermissionOnProject(getRadarToken(request), SOURCE_READ,
                    sub.getProject().getProjectName());
            return Response.status(Status.OK).entity(
                    getLastComputedSourceStatus(subjectId, sourceId)).build();
        } catch (AccessDeniedException exc) {
            LOGGER.error(exc.getMessage(), exc);
            return ResponseHandler.getJsonAccessDeniedResponse(request, exc.getMessage());
        } catch (NotAuthorizedException exc) {
            LOGGER.error(exc.getMessage(), exc);
            return ResponseHandler.getJsonNotAuthorizedResponse(request, exc.getMessage());
        } catch (Exception exec) {
            LOGGER.error(exec.getMessage(), exec);
            return ResponseHandler.getJsonErrorResponse(request, "Your request cannot be"
                    + "completed. If this error persists, please contact the service "
                    + "administrator.");
        }
    }

//    /**
//     * AVRO function that returns the status of the given sourceType.
//     */
//    @GET
//    @Produces(AVRO_BINARY)
//    @Path("/" + STATE + "/{" + SUBJECT_ID + "}/{" + SOURCE_ID + "}")
//    @Operation(summary = "Return a SourceDefinition values",
//            description = "Using the sourceType sensors values arrived within last 60sec, it computes "
//                    + "the"
//                    + "sender status for the given subjectID and sourceID")
//    @ApiResponse(responseCode = "500", description = "An error occurs while executing")
//    @ApiResponse(responseCode = "204", description = "No value for the given parameters")
//    @ApiResponse(responseCode = "200", description = "Return a byte array serialising sourceType.avsc "
//            + "object"
//            + "containing last computed status")
//    @ApiResponse(responseCode = "401", description = "Access denied error occured")
//    @ApiResponse(responseCode = "403", description = "Not Authorised error occured")
//    public Response getLastComputedSourceStatusAvro(
//            @PathParam(SUBJECT_ID) String subjectId,
//            @PathParam(SOURCE_ID) String sourceId) {
//        try {
//            ManagementPortalClient client = ManagementPortalClientManager
//                    .getManagementPortalClient(context);
//            org.radarcns.domain.managementportal.Subject sub = client.getSubject(subjectId);
//            checkPermissionOnProject(getRadarToken(request), SOURCE_READ,
//                    sub.getProject().getProjectName());
//            return ResponseHandler.getAvroResponse(request,
//                    getLastComputedSourceStatus(subjectId, sourceId));
//        } catch (AccessDeniedException exc) {
//            LOGGER.error(exc.getMessage(), exc);
//            return ResponseHandler.getJsonAccessDeniedResponse(request, exc.getMessage());
//        } catch (NotAuthorizedException exc) {
//            LOGGER.error(exc.getMessage(), exc);
//            return ResponseHandler.getJsonNotAuthorizedResponse(request, exc.getMessage());
//        } catch (Exception exec) {
//            LOGGER.error(exec.getMessage(), exec);
//            return ResponseHandler.getAvroErrorResponse(request);
//        }
//    }

    /**
     * Actual implementation of AVRO and JSON getRTStateByUserDevice.
     **/
    private Source getLastComputedSourceStatus(String subject, String source)
            throws IOException, TokenException {
        Param.isValidInput(subject, source);

        String sourceType = ContextResourceManager.getSourceDataAccessObject(context).getSourceType
                (source, context);

        if (sourceType == null) {
            return null;
        }

        Source device = Monitors.getInstance().getState(subject, source, sourceType, context);

        return device;
    }

    //--------------------------------------------------------------------------------------------//
    //                               SOURCE SPECIFICATION FUNCTIONS                               //
    //--------------------------------------------------------------------------------------------//

    /**
     * JSON function that returns the specification of the given sourceType.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/" + SPECIFICATION + "/{" + SOURCE_TYPE + "}")
    @Operation(summary = "Return a SourceDefinition specification",
            description = "Return the data specification of all on-board sensors for the given"
                    + "sourceType type")
    @ApiResponse(responseCode = "500", description = "An error occurs while executing, in the body"
            + "there is a message.avsc object with more details")
    @ApiResponse(responseCode = "204", description = "No value for the given parameters, in the "
            + "body"
            + "there is a message.avsc object with more details")
    @ApiResponse(responseCode = "200", description = "Return a source_specification.avsc object"
            + "containing last computed status")
    @ApiResponse(responseCode = "401", description = "Access denied error occured")
    @ApiResponse(responseCode = "403", description = "Not Authorised error occured")
    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    public Response getSourceSpecificationJson(
            @PathParam(SOURCE_TYPE) String source) {
        try {
            checkPermission(getRadarToken(request), SOURCE_READ);
            return Response.status(Status.OK).entity(getSourceSpecificationWorker(source)).build();
        } catch (AccessDeniedException exc) {
            LOGGER.error(exc.getMessage(), exc);
            return ResponseHandler.getJsonAccessDeniedResponse(request, exc.getMessage());
        } catch (NotAuthorizedException exc) {
            LOGGER.error(exc.getMessage(), exc);
            return ResponseHandler.getJsonNotAuthorizedResponse(request, exc.getMessage());
        } catch (Exception exec) {
            LOGGER.error(exec.getMessage(), exec);
            return ResponseHandler.getJsonErrorResponse(request, "Your request cannot be"
                    + "completed. If this error persists, please contact the service "
                    + "administrator.");
        }
    }

//    /**
//     * AVRO function that returns the status of the given data.
//     */
//    @GET
//    @Produces(AVRO_BINARY)
//    @Path("/" + SPECIFICATION + "/{" + SOURCE_TYPE + "}")
//    @Operation(summary = "Return a SourceDefinition specification",
//            description = "Return the data specification of all on-board sensors for the given"
//                    + "sourceType type")
//    @ApiResponse(responseCode = "500", description = "An error occurs while executing")
//    @ApiResponse(responseCode = "204", description = "No value for the given parameters")
//    @ApiResponse(responseCode = "200", description = "Return a source_specification.avsc object"
//            + "containing last computed status")
//    @ApiResponse(responseCode = "401", description = "Access denied error occured")
//    @ApiResponse(responseCode = "403", description = "Not Authorised error occured")
//    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
//    public Response getSourceSpecificationAvro(
//            @PathParam(SOURCE_TYPE) String source) {
//        try {
//            checkPermission(getRadarToken(request), SOURCE_READ);
//            return ResponseHandler.getAvroResponse(request, getSourceSpecificationWorker(source));
//        } catch (AccessDeniedException exc) {
//            LOGGER.error(exc.getMessage(), exc);
//            return ResponseHandler.getJsonAccessDeniedResponse(request, exc.getMessage());
//        } catch (NotAuthorizedException exc) {
//            LOGGER.error(exc.getMessage(), exc);
//            return ResponseHandler.getJsonNotAuthorizedResponse(request, exc.getMessage());
//        } catch (Exception exec) {
//            LOGGER.error(exec.getMessage(), exec);
//            return ResponseHandler.getAvroErrorResponse(request);
//        }
//    }

    /**
     * Actual implementation of AVRO and JSON getSpecification.
     **/
    private SourceType getSourceSpecificationWorker(String source)
            throws ConnectException {
        return Monitors.getInstance().getSpecification(source);
    }

    //--------------------------------------------------------------------------------------------//
    //                                         ALL SOURCES                                        //
    //--------------------------------------------------------------------------------------------//

    /**
     * JSON function that returns all known sources for the given subject.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/" + GET_ALL_SOURCES + "/{" + SUBJECT_ID + "}")
    @Operation(summary = "Return a User value",
            description = "Return all known sources associated with the give subjectID")
    @ApiResponse(responseCode = "500", description = "An error occurs while executing, in the body"
            + "there is a message.avsc object with more details")
    @ApiResponse(responseCode = "204", description = "No value for the given parameters, in the "
            + "body"
            + "there is a message.avsc object with more details")
    @ApiResponse(responseCode = "200", description = "Return a subject.avsc object")
    @ApiResponse(responseCode = "401", description = "Access denied error occured")
    @ApiResponse(responseCode = "403", description = "Not Authorised error occured")
    public Response getAllSourcesJson(
            @PathParam(SUBJECT_ID) String subjectId) {
        try {
            ManagementPortalClient client = ManagementPortalClientManager
                    .getManagementPortalClient(context);
            org.radarcns.domain.managementportal.Subject sub = client.getSubject(subjectId);
            checkPermissionOnProject(getRadarToken(request), SOURCE_READ,
                    sub.getProject().getProjectName());
            return Response.ok().entity( getAllSourcesWorker(subjectId)).build();
        } catch (AccessDeniedException exc) {
            LOGGER.error(exc.getMessage(), exc);
            return ResponseHandler.getJsonAccessDeniedResponse(request, exc.getMessage());
        } catch (NotAuthorizedException exc) {
            LOGGER.error(exc.getMessage(), exc);
            return ResponseHandler.getJsonNotAuthorizedResponse(request, exc.getMessage());
        } catch (Exception exec) {
            LOGGER.error(exec.getMessage(), exec);
            return ResponseHandler.getJsonErrorResponse(request, "Your request cannot be"
                    + "completed. If this error persists, please contact the service"
                    + "administrator.");
        }
    }

//    /**
//     * AVRO function that returns all known sources for the given subject.
//     */
//    @GET
//    @Produces(AVRO_BINARY)
//    @Path("/" + GET_ALL_SOURCES + "/{" + SUBJECT_ID + "}")
//    @Operation(summary = "Return a User value",
//            description = "Return all known sources associated with the give subjectID")
//    @ApiResponse(responseCode = "500", description = "An error occurs while executing")
//    @ApiResponse(responseCode = "204", description = "No value for the given parameters")
//    @ApiResponse(responseCode = "200", description = "Return a subject.avsc object")
//    @ApiResponse(responseCode = "401", description = "Access denied error occured")
//    @ApiResponse(responseCode = "403", description = "Not Authorised error occured")
//    public Response getAllSourcesAvro(
//            @PathParam(SUBJECT_ID) String subjectId) {
//        try {
//            ManagementPortalClient client = ManagementPortalClientManager
//                    .getManagementPortalClient(context);
//            org.radarcns.domain.managementportal.Subject sub = client.getSubject(subjectId);
//            checkPermissionOnProject(getRadarToken(request), SOURCE_READ,
//                    sub.getProject().getProjectName());
//            return ResponseHandler.getAvroResponse(request, getAllSourcesWorker(subjectId));
//        } catch (AccessDeniedException exc) {
//            LOGGER.error(exc.getMessage(), exc);
//            return ResponseHandler.getJsonAccessDeniedResponse(request, exc.getMessage());
//        } catch (NotAuthorizedException exc) {
//            LOGGER.error(exc.getMessage(), exc);
//            return ResponseHandler.getJsonNotAuthorizedResponse(request, exc.getMessage());
//        } catch (Exception exec) {
//            LOGGER.error(exec.getMessage(), exec);
//            return ResponseHandler.getAvroErrorResponse(request);
//        }
//    }

    /**
     * Actual implementation of AVRO and JSON getAllSources.
     **/
    private Subject getAllSourcesWorker(String subjectId) throws IOException, TokenException {
        Param.isValidSubject(subjectId);

        Subject subject = new Subject();

        if (ContextResourceManager.getSubjectDataAccessObject(context).exist(subjectId, context)) {
            subject = ContextResourceManager.getSubjectDataAccessObject(context).findAllSourcesByUser
                    (subjectId, context);
        }

        return subject;
    }
}
