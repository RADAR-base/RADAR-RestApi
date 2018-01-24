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
import static org.radarcns.auth.authorization.RadarAuthorization.checkPermissionOnProject;
import static org.radarcns.security.utils.SecurityUtils.getRadarToken;
import static org.radarcns.webapp.util.BasePath.ANDROID;
import static org.radarcns.webapp.util.BasePath.AVRO_BINARY;
import static org.radarcns.webapp.util.BasePath.STATUS;
import static org.radarcns.webapp.util.Parameter.SOURCE_ID;
import static org.radarcns.webapp.util.Parameter.SUBJECT_ID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.io.IOException;
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
import org.radarcns.dao.AndroidAppDataAccessObject;
import org.radarcns.exception.TokenException;
import org.radarcns.listener.ContextResourceManager;
import org.radarcns.listener.managementportal.ManagementPortalClient;
import org.radarcns.listener.managementportal.ManagementPortalClientManager;
import org.radarcns.domain.managementportal.Subject;
import org.radarcns.restapi.app.Application;
import org.radarcns.security.Param;
import org.radarcns.security.exception.AccessDeniedException;
import org.radarcns.webapp.util.ResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Android application status web-app. Function set to access Android app status information.
 */
@Path("/" + ANDROID)
public class AppStatusEndPoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppStatusEndPoint.class);

    @Context
    private ServletContext context;
    @Context
    private HttpServletRequest request;

    //--------------------------------------------------------------------------------------------//
    //                                    REAL-TIME FUNCTIONS                                     //
    //--------------------------------------------------------------------------------------------//

    /**
     * JSON function that returns the status app of the given subject.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/" + STATUS + "/{" + SUBJECT_ID + "}/{" + SOURCE_ID + "}")
    @Operation(summary = "Return an Applications status",
            description = "The Android application periodically updates its current status")
    @ApiResponse(responseCode = "500", description = "An error occurs while executing, in the body"
            + "there is a message.avsc object with more details")
    @ApiResponse(responseCode = "204", description =
            "No value for the given parameters, in the body"
                    + "there is a message.avsc object with more details")
    @ApiResponse(responseCode = "200", description =
            "Return a application.avsc object containing last"
                    + "received status")
    @ApiResponse(responseCode = "401", description = "Access denied error occured")
    @ApiResponse(responseCode = "403", description = "Not Authorised error occured")
    public Response getLastReceivedAppStatusJson(
            @PathParam(SUBJECT_ID) String subjectId,
            @PathParam(SOURCE_ID) String sourceId) {
        try {
            ManagementPortalClient client = ManagementPortalClientManager
                    .getManagementPortalClient(context);
            Subject sub = client.getSubject(subjectId);
            checkPermissionOnProject(getRadarToken(request), SOURCE_READ,
                    sub.getProject().getProjectName());
            return ResponseHandler.getJsonResponse(request,
                    getLastReceivedAppStatusWorker(subjectId, sourceId));
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

    /**
     * AVRO function that returns the status app of the given subject.
     */
    @GET
    @Produces(AVRO_BINARY)
    @Path("/" + STATUS + "/{" + SUBJECT_ID + "}/{" + SOURCE_ID + "}")
    @Operation(summary = "Return an Applications status",
            description = "The Android application periodically updates its current status")
    @ApiResponse(responseCode = "500", description = "An error occurs while executing")
    @ApiResponse(responseCode = "204", description = "No value for the given parameters")
    @ApiResponse(responseCode = "200", description =
            "Return a application.avsc object containing last"
                    + "received status")
    @ApiResponse(responseCode = "401", description = "Access denied error occured")
    @ApiResponse(responseCode = "403", description = "Not Authorised error occured")
    public Response getLastReceivedAppStatusAvro(
            @PathParam(SUBJECT_ID) String subjectId,
            @PathParam(SOURCE_ID) String sourceId) {
        try {
            ManagementPortalClient client = ManagementPortalClientManager
                    .getManagementPortalClient(context);
            Subject sub = client.getSubject(subjectId);
            checkPermissionOnProject(getRadarToken(request), SOURCE_READ,
                    sub.getProject().getProjectName());
            return ResponseHandler.getAvroResponse(request,
                    getLastReceivedAppStatusWorker(subjectId, sourceId));
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

    /**
     * Actual implementation of AVRO and JSON getRealTimeUser.
     **/
    private Application getLastReceivedAppStatusWorker(String subject, String source)
            throws IOException, TokenException {
        Param.isValidInput(subject, source);

        Application application = new Application();

        if (ContextResourceManager.getSubjectDataAccessObject(context).exist(subject, context)) {
            application = AndroidAppDataAccessObject.getInstance().getStatus(
                    subject, source, context);
        }

        return application;
    }
}
