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

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.radarcns.auth.authorization.Permission.Operation.READ;
import static org.radarcns.webapp.resource.BasePath.AVRO_BINARY;
import static org.radarcns.webapp.resource.BasePath.PROJECTS;
import static org.radarcns.webapp.resource.BasePath.SUBJECTS;
import static org.radarcns.webapp.resource.Parameter.PROJECT_NAME;
import static org.radarcns.webapp.resource.Parameter.SUBJECT_ID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.io.IOException;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import org.radarcns.auth.NeedsPermissionOnProject;
import org.radarcns.auth.NeedsPermissionOnSubject;
import org.radarcns.auth.authorization.Permission.Entity;
import org.radarcns.domain.restapi.Subject;
import org.radarcns.service.SubjectService;
import org.radarcns.webapp.filter.Authenticated;
import org.radarcns.webapp.validation.Alphanumeric;

/**
 * Subject web-app. Function set to access subject information. A subject is a person enrolled
 * for in a study.
 */
@Authenticated
@Path("/" + PROJECTS)
public class SubjectEndPoint {

    @Inject
    private SubjectService subjectService;

    /**
     * JSON function that returns all available subject based on the Study ID (Project ID).
     */
    @GET
    @Produces(APPLICATION_JSON)
    @Path("/{" + PROJECT_NAME + "}" + "/" + SUBJECTS)
    @Operation(summary = "Return a list of subjects contained within a study")
    @ApiResponse(responseCode = "500", description = "An error occurs while executing")
    @ApiResponse(responseCode = "200", description = "Return a list of subjects objects")
    @ApiResponse(responseCode = "401", description = "Access denied error occurred")
    @ApiResponse(responseCode = "403", description = "Not Authorised error occurred")
    @ApiResponse(responseCode = "404", description = "Project not found")
    @NeedsPermissionOnProject(entity = Entity.SUBJECT, operation = READ)
    public List<Subject> getAllSubjectsJsonFromStudy(
            @PathParam(PROJECT_NAME) String projectName) throws IOException {
        return subjectService.getAllSubjectsFromProject(projectName);
    }

    //--------------------------------------------------------------------------------------------//
    //                                        SUBJECT INFO                                        //
    //--------------------------------------------------------------------------------------------//

    /**
     * JSON function that returns all information related to the given subject identifier.
     */
    @GET
    @Produces({APPLICATION_JSON, AVRO_BINARY})
    @Path("/{" + PROJECT_NAME + "}" + "/" + SUBJECTS + "/{" + SUBJECT_ID + "}")
    @Operation(summary = "Return the information related to given subject identifier")
    @ApiResponse(responseCode = "500", description = "An error occurs while executing")
    @ApiResponse(responseCode = "200", description =
            "Return the subject object associated with the subject identifier")
    @ApiResponse(responseCode = "401", description = "Access denied error occurred")
    @ApiResponse(responseCode = "403", description = "Not Authorised error occurred")
    @ApiResponse(responseCode = "404", description = "SubjectDTO cannot be found")
    @NeedsPermissionOnSubject(entity = Entity.SUBJECT, operation = READ)
    public Subject getSubjectJson(
            @Alphanumeric @PathParam(PROJECT_NAME) String projectName,
            @Alphanumeric @PathParam(SUBJECT_ID) String subjectId) throws IOException {
        return subjectService.getSubjectBySubjectId(projectName, subjectId);
    }
}
