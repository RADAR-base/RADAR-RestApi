package org.radarcns.webapp;

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

import static org.radarcns.auth.authorization.Permission.SOURCE_READ;
import static org.radarcns.auth.authorization.RadarAuthorization.checkPermissionOnProject;
import static org.radarcns.security.utils.SecurityUtils.getJWT;
import static org.radarcns.webapp.util.BasePath.*;
import static org.radarcns.webapp.util.Parameter.SOURCE_ID;
import static org.radarcns.webapp.util.Parameter.SUBJECT_ID;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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

import org.radarcns.auth.exception.NotAuthorizedException;
import org.radarcns.avro.restapi.app.Application;
import org.radarcns.dao.AndroidAppDataAccessObject;
import org.radarcns.dao.SubjectDataAccessObject;
import org.radarcns.managementportal.MpClient;
import org.radarcns.managementportal.Subject;
import org.radarcns.security.Param;
import org.radarcns.security.exception.AccessDeniedException;
import org.radarcns.webapp.util.ResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Android application status web-app. Function set to access Android app status information.
 */
@Api
@Path("/" + ANDROID)
public class AppStatusEndPoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppStatusEndPoint.class);

    @Context private ServletContext context;
    @Context private HttpServletRequest request;

    //--------------------------------------------------------------------------------------------//
    //                                    REAL-TIME FUNCTIONS                                     //
    //--------------------------------------------------------------------------------------------//
    /**
     * JSON function that returns the status app of the given subject.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/" + STATUS + "/{" + SUBJECT_ID + "}/{" + SOURCE_ID + "}")
    @ApiOperation(
            value = "Return an Applications status",
            notes = "The Android application periodically updates its current status")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "An error occurs while executing, in the body"
                + "there is a message.avsc object with more details"),
            @ApiResponse(code = 204, message = "No value for the given parameters, in the body"
                + "there is a message.avsc object with more details"),
            @ApiResponse(code = 200, message = "Return a application.avsc object containing last"
                + "received status"),
            @ApiResponse(code = 401, message = "Access denied error occured"),
            @ApiResponse(code = 403, message = "Not Authorised error occured")})
    public Response getLastReceivedAppStatusJson(
            @PathParam(SUBJECT_ID) String subjectId,
            @PathParam(SOURCE_ID) String sourceId) {
        try {
            MpClient client = new MpClient(context);
            Subject sub = client.getSubject(subjectId);
            checkPermissionOnProject(getJWT(request), SOURCE_READ,
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
                + "completed. If this error persists, please contact the service administrator.");
        }
    }

    /**
     * AVRO function that returns the status app of the given subject.
     */
    @GET
    @Produces(AVRO_BINARY)
    @Path("/" + STATUS + "/{" + SUBJECT_ID + "}/{" + SOURCE_ID + "}")
    @ApiOperation(
            value = "Return an Applications status",
            notes = "The Android application periodically updates its current status")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "An error occurs while executing"),
            @ApiResponse(code = 204, message = "No value for the given parameters"),
            @ApiResponse(code = 200, message = "Return a application.avsc object containing last"
                + "received status"),
            @ApiResponse(code = 401, message = "Access denied error occured"),
            @ApiResponse(code = 403, message = "Not Authorised error occured")})
    public Response getLastReceivedAppStatusAvro(
            @PathParam(SUBJECT_ID) String subjectId,
            @PathParam(SOURCE_ID) String sourceId) {
        try {
            MpClient client = new MpClient(context);
            Subject sub = client.getSubject(subjectId);
            checkPermissionOnProject(getJWT(request), SOURCE_READ,
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
            throws ConnectException {
        Param.isValidInput(subject, source);

        Application application = new Application();

        if (SubjectDataAccessObject.exist(subject, context)) {
            application = AndroidAppDataAccessObject.getInstance().getStatus(
                subject, source, context);
        }

        return application;
    }
}
