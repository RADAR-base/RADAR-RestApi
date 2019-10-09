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
import static org.radarcns.webapp.resource.BasePath.APPLICATION_STATUS;
import static org.radarcns.webapp.resource.BasePath.AVRO_BINARY;
import static org.radarcns.webapp.resource.Parameter.PROJECT_NAME;
import static org.radarcns.webapp.resource.Parameter.SOURCE_ID;
import static org.radarcns.webapp.resource.Parameter.SUBJECT_ID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.io.IOException;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.radarbase.jersey.auth.Authenticated;
import org.radarbase.jersey.auth.NeedsPermission;
import org.radarcns.domain.restapi.monitor.MonitorData;
import org.radarcns.listener.managementportal.ManagementPortalClient;
import org.radarcns.mongo.util.MongoWrapper;
import org.radarcns.service.SourceStatusMonitorService;
import org.radarcns.service.SubjectService;
import org.radarcns.webapp.validation.Alphanumeric;

/**
 * Android application status web-app. Function set to access Android app status information.
 */
@Authenticated
@Path('/' + APPLICATION_STATUS)
public class AppStatusEndPoint {

    @Inject
    private MongoWrapper mongoClient;

    @Inject
    private ManagementPortalClient mpClient;

    @Inject
    private SubjectService subjectService;

    @Inject
    private SourceStatusMonitorService sourceStatusMonitorService;
    //--------------------------------------------------------------------------------------------//
    //                                    APPLICATION_STATUS FUNCTIONS                            //
    //--------------------------------------------------------------------------------------------//

    /**
     * JSON function that returns the status app of the given subject.
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON, AVRO_BINARY})
    @Path("/{" + PROJECT_NAME + "}/{" + SUBJECT_ID + "}/{" + SOURCE_ID + "}")
    @Operation(summary = "Return an Applications status",
            description = "The Android application periodically updates its current status")
    @ApiResponse(responseCode = "500", description = "An error occurs while executing")
    @ApiResponse(responseCode = "200", description =
            "Return a application object containing last received status")
    @ApiResponse(responseCode = "401", description = "Access denied error occurred")
    @ApiResponse(responseCode = "403", description = "Not Authorised error occurred")
    @ApiResponse(responseCode = "404", description = "project, subject or source not found.")
    @NeedsPermission(entity = SOURCE, operation = READ,
        projectPathParam = PROJECT_NAME, userPathParam = SUBJECT_ID)
    public MonitorData getLastReceivedAppStatusJson(
            @Alphanumeric @PathParam(PROJECT_NAME) String projectName,
            @Alphanumeric @PathParam(SUBJECT_ID) String subjectId,
            @Alphanumeric @PathParam(SOURCE_ID) String sourceId) throws IOException {
        mpClient.checkSubjectInProject(projectName, subjectId);
        subjectService.checkSourceAssignedToSubject(subjectId, sourceId);

        return sourceStatusMonitorService.getStatus(projectName, subjectId, sourceId, mongoClient);
    }
}
