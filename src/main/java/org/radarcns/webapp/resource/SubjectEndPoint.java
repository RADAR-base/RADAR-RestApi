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

import static org.radarcns.auth.authorization.Permission.Operation.READ;
import static org.radarcns.webapp.util.BasePath.AVRO_BINARY;
import static org.radarcns.webapp.util.BasePath.GET_ALL_SUBJECTS;
import static org.radarcns.webapp.util.BasePath.GET_SUBJECT;
import static org.radarcns.webapp.util.BasePath.SUBJECT;
import static org.radarcns.webapp.util.Parameter.PROJECT_NAME;
import static org.radarcns.webapp.util.Parameter.SUBJECT_ID;

import com.mongodb.MongoClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.io.IOException;
import java.net.ConnectException;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import org.radarcns.auth.authorization.Permission.Entity;
import org.radarcns.dao.SubjectDataAccessObject;
import org.radarcns.restapi.subject.Cohort;
import org.radarcns.restapi.subject.Subject;
import org.radarcns.security.Param;
import org.radarcns.security.filter.NeedsPermissionOnProject;
import org.radarcns.security.filter.NeedsPermissionOnSubject;
import org.radarcns.webapp.util.ResponseHandler;

/**
 * Subject web-app. Function set to access subject information. A subject is a person enrolled for
 * in a study.
 */
@Provider
@Path("/" + SUBJECT)
public class SubjectEndPoint {
    @Inject
    private MongoClient mongoClient;

    //--------------------------------------------------------------------------------------------//
    //                                        ALL SUBJECTS                                        //
    //--------------------------------------------------------------------------------------------//

    /**
     * JSON function that returns all available subject.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/" + GET_ALL_SUBJECTS + "/{" + PROJECT_NAME + "}")
    @Operation(summary = "Return a list of subjects",
            description = "Each subject can have multiple sourceID associated with him")
    @ApiResponse(responseCode = "500", description = "An error occurs while executing, in the body"
            + "there is a message.avsc object with more details")
    @ApiResponse(responseCode = "204", description =
            "No value for the given parameters, in the body"
                    + "there is a message.avsc object with more details")
    @ApiResponse(responseCode = "200", description = "Return a list of subject.avsc objects")
    @ApiResponse(responseCode = "401", description = "Access denied error occurred")
    @ApiResponse(responseCode = "403", description = "Not Authorised error occurred")
    @NeedsPermissionOnProject(entity = Entity.SUBJECT, operation = READ)
    public Response getAllSubjectsJson(@PathParam(PROJECT_NAME) String study) throws IOException {
        return ResponseHandler.getJsonResponse(getAllSubjectsWorker());
    }

    /**
     * AVRO function that returns all available subject.
     */
    @GET
    @Produces(AVRO_BINARY)
    @Path("/" + GET_ALL_SUBJECTS + "/{" + PROJECT_NAME + "}")
    @Operation(summary = "Return a list of subjects",
            description = "Each subject can have multiple sourceID associated with him")
    @ApiResponse(responseCode = "500", description = "An error occurs while executing")
    @ApiResponse(responseCode = "204", description = "No value for the given parameters")
    @ApiResponse(responseCode = "200", description = "Return a byte array serialising a list of"
            + "subject.avsc objects")
    @ApiResponse(responseCode = "401", description = "Access denied error occurred")
    @ApiResponse(responseCode = "403", description = "Not Authorised error occurred")
    @NeedsPermissionOnProject(entity = Entity.SUBJECT, operation = READ)
    public Response getAllSubjectsAvro(@PathParam(PROJECT_NAME) String study) throws IOException {
        return ResponseHandler.getAvroResponse(getAllSubjectsWorker());
    }

    /**
     * Actual implementation of AVRO and JSON getAllSubjects.
     **/
    private Cohort getAllSubjectsWorker() throws ConnectException {
        return SubjectDataAccessObject.getAllSubjects(mongoClient);
    }

    //--------------------------------------------------------------------------------------------//
    //                                        SUBJECT INFO                                        //
    //--------------------------------------------------------------------------------------------//

    /**
     * JSON function that returns all information related to the given subject identifier.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{" + PROJECT_NAME + "}/" + GET_SUBJECT + "/{" + SUBJECT_ID + "}")
    @Operation(summary = "Return the information related to given subject identifier",
            description = "Some information are not implemented yet. The returned values are "
                    + "hardcoded.")
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
    @ApiResponse(responseCode = "404", description = "Subject cannot be found")
    @NeedsPermissionOnSubject(entity = Entity.SUBJECT, operation = READ)
    public Response getSubjectJson(
            @PathParam(PROJECT_NAME) String projectName,
            @PathParam(SUBJECT_ID) String subjectId) throws IOException {
        return ResponseHandler.getJsonResponse(getSubjectWorker(subjectId));
    }

    /**
     * AVRO function that returns all information related to the given subject identifier.
     */
    @GET
    @Produces(AVRO_BINARY)
    @Path("{" + PROJECT_NAME + "}/" + GET_SUBJECT + "/{" + SUBJECT_ID + "}")
    @Operation(
            summary = "Return the information related to given subject identifier",
            description = "Some information are not implemented yet. The returned values are "
                    + "hardcoded.")
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
    @ApiResponse(responseCode = "404", description = "Subject cannot be found")
    @NeedsPermissionOnProject(entity = Entity.SUBJECT, operation = READ)
    public Response getSubjectAvro(
            @PathParam(PROJECT_NAME) String projectName,
            @PathParam(SUBJECT_ID) String subjectId) throws IOException {
        return ResponseHandler.getAvroResponse(getSubjectWorker(subjectId));
    }

    /**
     * Actual implementation of AVRO and JSON getSubject.
     **/
    private Subject getSubjectWorker(String subjectId) throws ConnectException {
        Param.isValidSubject(subjectId);

        Subject subject = new Subject();

        if (SubjectDataAccessObject.exist(subjectId, mongoClient)) {
            subject = SubjectDataAccessObject.getSubject(subjectId, mongoClient);
        }

        return subject;
    }

}
