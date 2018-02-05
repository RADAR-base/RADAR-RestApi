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

import static org.radarcns.auth.authorization.Permission.Entity.SUBJECT;
import static org.radarcns.auth.authorization.Permission.Operation.READ;
import static org.radarcns.webapp.util.BasePath.SUBJECTS;
import static org.radarcns.webapp.util.Parameter.PROJECT_NAME;
import static org.radarcns.webapp.util.Parameter.SUBJECT_ID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.io.IOException;
import java.util.Collection;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;
import org.radarcns.auth.NeedsPermission;
import org.radarcns.auth.NeedsPermissionOnSubject;
import org.radarcns.listener.managementportal.ManagementPortalClient;
import org.radarcns.managementportal.Subject;

/**
 * Management Portal web-app. Function set to access subject and source information from MP. A
 * subject is a person enrolled for in a study. A source is a device linked to the subject.
 */
@Provider
@Path("/mp")
public class ManagementPortalEndPoint {
    @Inject
    private ManagementPortalClient mpClient;

    //--------------------------------------------------------------------------------------------//
    //                                        SUBJECTS                                            //
    //--------------------------------------------------------------------------------------------//

    /**
     * JSON function that returns all available subject.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{" + PROJECT_NAME + "}/" + SUBJECTS)
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
    @NeedsPermission(entity = SUBJECT, operation = READ)
    public Collection<Subject> getAllSubjectsJson() throws IOException {
        return mpClient.getSubjects().values();
    }

    /**
     * JSON function that returns all information related to the given subject identifier.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{" + PROJECT_NAME + "}/" + SUBJECTS + "/{" + SUBJECT_ID + "}")
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
    @NeedsPermissionOnSubject(entity = SUBJECT, operation = READ)
    public Subject getSubjectJson(
            @PathParam(PROJECT_NAME) String projectName,
            @PathParam(SUBJECT_ID) String subjectId) throws IOException {
        return mpClient.getSubject(subjectId);
    }
}
