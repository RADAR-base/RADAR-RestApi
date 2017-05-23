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

import static org.radarcns.webapp.util.BasePath.ANDROID;
import static org.radarcns.webapp.util.BasePath.AVRO;
import static org.radarcns.webapp.util.BasePath.STATUS;
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
import org.radarcns.avro.restapi.app.Application;
import org.radarcns.dao.AndroidAppDataAccessObject;
import org.radarcns.dao.SubjectDataAccessObject;
import org.radarcns.security.Param;
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
                + "received status")})
    public Response getLastReceivedAppStatusJson(
            @PathParam(SUBJECT_ID) String subject,
            @PathParam(SOURCE_ID) String source) {
        try {
            return ResponseHandler.getJsonResponse(request,
                getLastReceivedAppStatusWorker(subject, source));
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
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Path("/" + AVRO + "/" + STATUS + "/{" + SUBJECT_ID + "}/{" + SOURCE_ID + "}")
    @ApiOperation(
            value = "Return an Applications status",
            notes = "The Android application periodically updates its current status")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "An error occurs while executing"),
            @ApiResponse(code = 204, message = "No value for the given parameters"),
            @ApiResponse(code = 200, message = "Return a application.avsc object containing last"
                + "received status")})
    public Response getLastReceivedAppStatusAvro(
            @PathParam(SUBJECT_ID) String subject,
            @PathParam(SOURCE_ID) String source) {
        try {
            return ResponseHandler.getAvroResponse(request,
                getLastReceivedAppStatusWorker(subject, source));
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
