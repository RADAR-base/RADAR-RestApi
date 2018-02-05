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

import static org.radarcns.auth.authorization.Permission.Entity.SOURCE;
import static org.radarcns.auth.authorization.Permission.Operation.READ;
import static org.radarcns.webapp.resource.BasePath.ANDROID;
import static org.radarcns.webapp.resource.BasePath.AVRO_BINARY;
import static org.radarcns.webapp.resource.BasePath.STATUS;
import static org.radarcns.webapp.resource.Parameter.PROJECT_NAME;
import static org.radarcns.webapp.resource.Parameter.SOURCE_ID;
import static org.radarcns.webapp.resource.Parameter.SUBJECT_ID;

import com.mongodb.MongoClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.io.IOException;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.radarcns.auth.NeedsPermissionOnSubject;
import org.radarcns.dao.AndroidAppDataAccessObject;
import org.radarcns.dao.SubjectDataAccessObject;
import org.radarcns.listener.managementportal.ManagementPortalClient;
import org.radarcns.monitor.application.ServerStatus;
import org.radarcns.restapi.app.Application;
import org.radarcns.webapp.filter.Authenticated;
import org.radarcns.webapp.validation.Alphanumeric;

/**
 * Android application status web-app. Function set to access Android app status information.
 */
@Authenticated
@Path("/" + ANDROID)
public class AppStatusEndPoint {
    @Inject
    private MongoClient mongoClient;

    @Inject
    private ManagementPortalClient mpClient;

    //--------------------------------------------------------------------------------------------//
    //                                    REAL-TIME FUNCTIONS                                     //
    //--------------------------------------------------------------------------------------------//

    /**
     * JSON function that returns the status app of the given subject.
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON, AVRO_BINARY})
    @Path("/" + STATUS + "/{" + PROJECT_NAME + "}/{" + SUBJECT_ID + "}/{" + SOURCE_ID + "}")
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
    @ApiResponse(responseCode = "401", description = "Access denied error occurred")
    @ApiResponse(responseCode = "403", description = "Not Authorised error occurred")
    @NeedsPermissionOnSubject(entity = SOURCE, operation = READ)
    public Application getLastReceivedAppStatusJson(
            @Alphanumeric @PathParam(PROJECT_NAME) String projectName,
            @Alphanumeric @PathParam(SUBJECT_ID) String subjectId,
            @Alphanumeric @PathParam(SOURCE_ID) String sourceId) throws IOException {
        mpClient.checkSubjectInProject(projectName, subjectId);
        Application application = new Application(
                null, 0d, ServerStatus.UNKNOWN, -1, -1, -1);

        if (SubjectDataAccessObject.exist(subjectId, mongoClient)) {
            application = AndroidAppDataAccessObject.getInstance().getStatus(
                    subjectId, sourceId, mongoClient);
        }

        return application;
    }
}
