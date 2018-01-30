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

import static org.radarcns.auth.authorization.Permission.SUBJECT_READ;
import static org.radarcns.auth.authorization.RadarAuthorization.checkPermission;
import static org.radarcns.auth.authorization.RadarAuthorization.checkPermissionOnProject;
import static org.radarcns.webapp.util.BasePath.SUBJECTS;
import static org.radarcns.webapp.util.Parameter.SUBJECT_ID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.io.IOException;
import java.security.GeneralSecurityException;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.radarcns.auth.token.RadarToken;
import org.radarcns.listener.managementportal.ManagementPortalClient;
import org.radarcns.managementportal.Subject;
import org.radarcns.webapp.exception.NotFoundException;

/**
 * Management Portal web-app. Function set to access subject and source information from MP. A
 * subject is a person enrolled for in a study. A source is a device linked to the subject.
 */
@Path("/mp")
public class ManagementPortalEndPoint {
    @Inject
    private RadarToken token;

    @Inject
    private ManagementPortalClient client;

    //--------------------------------------------------------------------------------------------//
    //                                        SUBJECTS                                            //
    //--------------------------------------------------------------------------------------------//

    /**
     * JSON function that returns all available subject.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/" + SUBJECTS)
    @Operation(summary = "Return a list of subjects from the management portal",
            description = "Each subject can have multiple sourceID associated with him")
    @ApiResponse(responseCode = "500", description = "An error occurs while executing, in the body"
            + "there is a message.avsc object with more details")
    @ApiResponse(responseCode = "204", description =
            "No value for the given parameters, in the body"
                    + "there is a message.avsc object with more details")
    @ApiResponse(responseCode = "200", description = "Return a list of subject.avsc objects")
    @ApiResponse(responseCode = "401", description = "Access denied error occurred")
    @ApiResponse(responseCode = "403", description = "Not Authorised error occurred")
    public Response getAllSubjectsJson()
            throws IOException, GeneralSecurityException {
        checkPermission(token, SUBJECT_READ);
        return Response.status(Status.OK)
                .entity(client.getSubjects().values())
                .build();
    }

    /**
     * JSON function that returns all information related to the given subject identifier.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/" + SUBJECTS + "/{" + SUBJECT_ID + "}")
    @Operation(summary = "Return the information related to given subject identifier",
            description = "Source infomation not present right now")
    @ApiResponse(responseCode = "500", description = "An error occurs while executing, in the body"
            + "there is a message.avsc object with more details")
    @ApiResponse(responseCode = "204", description =
            "No value for the given parameters, in the body"
                    + "there is a message.avsc object with more details")
    @ApiResponse(responseCode = "200", description =
            "Return the subject.avsc object associated with the "
                    + "given subject identifier")
    @ApiResponse(responseCode = "401", description = "Access denied error occurred")
    @ApiResponse(responseCode = "403", description = "Not Authorised error occurred")
    @ApiResponse(responseCode = "404", description = "Subject not found")
    public Response getSubjectJson(@PathParam(SUBJECT_ID) String subjectId)
            throws IOException, GeneralSecurityException, NotFoundException {
        Subject subject = client.getSubject(subjectId);
        checkPermissionOnProject(token, SUBJECT_READ, subject.getProject().getProjectName());
        return Response.status(Status.OK)
                .entity(subject)
                .build();
    }
}
