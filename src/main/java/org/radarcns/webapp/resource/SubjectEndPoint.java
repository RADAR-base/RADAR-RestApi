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
import static org.radarcns.webapp.resource.BasePath.GET_ALL_SUBJECTS;
import static org.radarcns.webapp.resource.BasePath.GET_SUBJECT;
import static org.radarcns.webapp.resource.BasePath.SUBJECT;
import static org.radarcns.webapp.resource.Parameter.PROJECT_NAME;
import static org.radarcns.webapp.resource.Parameter.SUBJECT_ID;

import com.mongodb.MongoClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import org.radarcns.auth.NeedsPermissionOnProject;
import org.radarcns.auth.NeedsPermissionOnSubject;
import org.radarcns.auth.authorization.Permission.Entity;
import org.radarcns.dao.SubjectDataAccessObject;
import org.radarcns.domain.managementportal.Subject;
import org.radarcns.listener.managementportal.ManagementPortalClient;
import org.radarcns.webapp.filter.Authenticated;
import org.radarcns.webapp.validation.Alphanumeric;

/**
 * Subject web-app. Function set to access subject information. A subject is a person enrolled for
 * in a study.
 */
@Authenticated
@Path("/" + SUBJECT)
public class SubjectEndPoint {
    @Inject
    private MongoClient mongoClient;

    @Inject
    private ManagementPortalClient mpClient;

    @Inject
    private SubjectDataAccessObject subjectDataAccessObject;

    //--------------------------------------------------------------------------------------------//
    //                                        ALL SUBJECTS                                        //
    //--------------------------------------------------------------------------------------------//

    /**
     * JSON function that returns all available subject.
     */
    @GET
    @Produces({APPLICATION_JSON, AVRO_BINARY})
    @Path("/" + GET_ALL_SUBJECTS + "/{" + PROJECT_NAME + "}")
    @Operation(summary = "Return a list of subjects",
            description = "Each subject can have multiple sourceID associated with him")
    @ApiResponse(responseCode = "500", description = "An error occurs while executing, in the body"
            + "there is a message.avsc object with more details")
    @ApiResponse(responseCode = "200", description = "Return a list of subject.avsc objects")
    @ApiResponse(responseCode = "401", description = "Access denied error occurred")
    @ApiResponse(responseCode = "403", description = "Not Authorised error occurred")
    @ApiResponse(responseCode = "404", description = "Project not found.")
    @NeedsPermissionOnProject(entity = Entity.SUBJECT, operation = READ)
    public List<Subject> getAllSubjectsJson(
            @Alphanumeric @PathParam(PROJECT_NAME) String study) throws IOException {
        // TODO: actually use the current study
        // throws on not found
        mpClient.getProject(study);
        return mpClient.getAllSubjectsFromProject(study);
    }

    //--------------------------------------------------------------------------------------------//
    //                                        SUBJECT INFO                                        //
    //--------------------------------------------------------------------------------------------//

    /**
     * JSON function that returns all information related to the given subject identifier.
     */
    @GET
    @Produces({APPLICATION_JSON, AVRO_BINARY})
    @Path("{" + PROJECT_NAME + "}/" + GET_SUBJECT + "/{" + SUBJECT_ID + "}")
    @Operation(summary = "Return the information related to given subject identifier",
            description = "Some information are not implemented yet. The returned values are "
                    + "hardcoded.")
    @ApiResponse(responseCode = "500", description = "An error occurs while executing, in the body"
            + "there is a message.avsc object with more details")
    @ApiResponse(responseCode = "200", description =
            "Return the subject.avsc object associated with the "
                    + "given subject identifier")
    @ApiResponse(responseCode = "401", description = "Access denied error occurred")
    @ApiResponse(responseCode = "403", description = "Not Authorised error occurred")
    @ApiResponse(responseCode = "404", description = "Subject cannot be found")
    @NeedsPermissionOnSubject(entity = Entity.SUBJECT, operation = READ)
    public org.radarcns.domain.restapi.Subject getSubjectJson(
            @Alphanumeric @PathParam(PROJECT_NAME) String projectName,
            @Alphanumeric @PathParam(SUBJECT_ID) String subjectId) throws IOException {
        // check that the project and subject exist
        mpClient.getProject(projectName);
        mpClient.getSubject(subjectId);
        if (subjectDataAccessObject.exist(subjectId, mongoClient)) {
            return subjectDataAccessObject.getSubject(subjectId, mongoClient);
        } else {
            String now = Instant.now().toString();
            return new org.radarcns.domain.restapi.Subject(subjectId, false,
                    Collections.emptyList());
        }
    }
}
