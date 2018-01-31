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

import static org.radarcns.auth.authorization.Permission.Operation.READ;
import static org.radarcns.webapp.util.BasePath.AVRO_BINARY;
import static org.radarcns.webapp.util.BasePath.GET_ALL_SOURCES;
import static org.radarcns.webapp.util.BasePath.SOURCE;
import static org.radarcns.webapp.util.BasePath.SPECIFICATION;
import static org.radarcns.webapp.util.BasePath.STATE;
import static org.radarcns.webapp.util.Parameter.SOURCE_ID;
import static org.radarcns.webapp.util.Parameter.SOURCE_TYPE;
import static org.radarcns.webapp.util.Parameter.SUBJECT_ID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.io.IOException;
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
import org.radarcns.auth.authorization.Permission.Entity;
import org.radarcns.dao.SourceDataAccessObject;
import org.radarcns.dao.SubjectDataAccessObject;
import org.radarcns.monitor.Monitors;
import org.radarcns.restapi.source.Source;
import org.radarcns.restapi.spec.SourceSpecification;
import org.radarcns.restapi.subject.Subject;
import org.radarcns.security.Param;
import org.radarcns.security.filter.NeedsPermission;
import org.radarcns.security.filter.NeedsPermissionOnSubject;
import org.radarcns.webapp.util.ResponseHandler;

/**
 * SourceDefinition web-app. Function set to access source information.
 */
@Path("/" + SOURCE)
public class SourceEndPoint {
    @Context
    private ServletContext context;
    @Context
    private HttpServletRequest request;

    //--------------------------------------------------------------------------------------------//
    //                                       STATE FUNCTIONS                                      //
    //--------------------------------------------------------------------------------------------//

    /**
     * JSON function that returns the status of the given source.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/" + STATE + "/{" + SUBJECT_ID + "}/{" + SOURCE_ID + "}")
    @Operation(summary = "Return a SourceDefinition values",
            description = "Using the source sensors values arrived within last 60sec, it computes "
                    + "the"
                    + "sender status for the given subjectID and sourceID")
    @ApiResponse(responseCode = "500", description = "An error occurs while executing, in the body"
            + "there is a message.avsc object with more details")
    @ApiResponse(responseCode = "204", description = "No value for the given parameters, in the "
            + "body"
            + "there is a message.avsc object with more details")
    @ApiResponse(responseCode = "200", description = "Return a source.avsc object containing last"
            + "computed status")
    @ApiResponse(responseCode = "401", description = "Access denied error occurred")
    @ApiResponse(responseCode = "403", description = "Not Authorised error occurred")
    @ApiResponse(responseCode = "404", description = "Subject cannot be found")
    @NeedsPermissionOnSubject(entity = Entity.SOURCE, operation = READ)
    public Response getLastComputedSourceStatusJson(
            @PathParam(SUBJECT_ID) String subjectId,
            @PathParam(SOURCE_ID) String sourceId) throws IOException {
        return ResponseHandler.getJsonResponse(request,
                getLastComputedSourceStatus(subjectId, sourceId));
    }

    /**
     * AVRO function that returns the status of the given source.
     */
    @GET
    @Produces(AVRO_BINARY)
    @Path("/" + STATE + "/{" + SUBJECT_ID + "}/{" + SOURCE_ID + "}")
    @Operation(summary = "Return a SourceDefinition values",
            description = "Using the source sensors values arrived within last 60sec, it computes "
                    + "the"
                    + "sender status for the given subjectID and sourceID")
    @ApiResponse(responseCode = "500", description = "An error occurs while executing")
    @ApiResponse(responseCode = "204", description = "No value for the given parameters")
    @ApiResponse(responseCode = "200", description = "Return a byte array serialising source.avsc "
            + "object"
            + "containing last computed status")
    @ApiResponse(responseCode = "401", description = "Access denied error occurred")
    @ApiResponse(responseCode = "403", description = "Not Authorised error occurred")
    @ApiResponse(responseCode = "404", description = "Subject cannot be found")
    @NeedsPermissionOnSubject(entity = Entity.SOURCE, operation = READ)
    public Response getLastComputedSourceStatusAvro(
            @PathParam(SUBJECT_ID) String subjectId,
            @PathParam(SOURCE_ID) String sourceId) throws IOException {
        return ResponseHandler.getAvroResponse(request,
                getLastComputedSourceStatus(subjectId, sourceId));
    }

    /**
     * Actual implementation of AVRO and JSON getRTStateByUserDevice.
     **/
    private Source getLastComputedSourceStatus(String subject, String source)
            throws ConnectException {
        Param.isValidInput(subject, source);

        String sourceType = SourceDataAccessObject.getSourceType(source, context);

        if (sourceType == null) {
            return null;
        }

        return Monitors.getInstance().getState(subject, source, sourceType, context);
    }

    //--------------------------------------------------------------------------------------------//
    //                               SOURCE SPECIFICATION FUNCTIONS                               //
    //--------------------------------------------------------------------------------------------//

    /**
     * JSON function that returns the specification of the given source.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/" + SPECIFICATION + "/{" + SOURCE_TYPE + "}")
    @Operation(summary = "Return a SourceDefinition specification",
            description = "Return the data specification of all on-board sensors for the given"
                    + "source type")
    @ApiResponse(responseCode = "500", description = "An error occurs while executing, in the body"
            + "there is a message.avsc object with more details")
    @ApiResponse(responseCode = "204", description = "No value for the given parameters, in the "
            + "body"
            + "there is a message.avsc object with more details")
    @ApiResponse(responseCode = "200", description = "Return a source_specification.avsc object"
            + "containing last computed status")
    @ApiResponse(responseCode = "401", description = "Access denied error occurred")
    @ApiResponse(responseCode = "403", description = "Not Authorised error occurred")
    @NeedsPermission(entity = Entity.SOURCE, operation = READ)
    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    public Response getSourceSpecificationJson(
            @PathParam(SOURCE_TYPE) String source) throws IOException {
        return ResponseHandler.getJsonResponse(request, getSourceSpecificationWorker(source));
    }

    /**
     * AVRO function that returns the status of the given data.
     */
    @GET
    @Produces(AVRO_BINARY)
    @Path("/" + SPECIFICATION + "/{" + SOURCE_TYPE + "}")
    @Operation(summary = "Return a SourceDefinition specification",
            description = "Return the data specification of all on-board sensors for the given"
                    + "source type")
    @ApiResponse(responseCode = "500", description = "An error occurs while executing")
    @ApiResponse(responseCode = "204", description = "No value for the given parameters")
    @ApiResponse(responseCode = "200", description = "Return a source_specification.avsc object"
            + "containing last computed status")
    @ApiResponse(responseCode = "401", description = "Access denied error occurred")
    @ApiResponse(responseCode = "403", description = "Not Authorised error occurred")
    @NeedsPermission(entity = Entity.SOURCE, operation = READ)
    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    public Response getSourceSpecificationAvro(
            @PathParam(SOURCE_TYPE) String source) throws IOException {
        return ResponseHandler.getAvroResponse(request, getSourceSpecificationWorker(source));
    }

    /**
     * Actual implementation of AVRO and JSON getSpecification.
     **/
    private SourceSpecification getSourceSpecificationWorker(String source) {
        return Monitors.getInstance().getSpecification(source);
    }

    //--------------------------------------------------------------------------------------------//
    //                                         ALL SOURCES                                        //
    //--------------------------------------------------------------------------------------------//

    /**
     * JSON function that returns all known sources for the given subject.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/" + GET_ALL_SOURCES + "/{" + SUBJECT_ID + "}")
    @Operation(summary = "Return a User value",
            description = "Return all known sources associated with the give subjectID")
    @ApiResponse(responseCode = "500", description = "An error occurs while executing, in the body"
            + "there is a message.avsc object with more details")
    @ApiResponse(responseCode = "204", description = "No value for the given parameters, in the "
            + "body"
            + "there is a message.avsc object with more details")
    @ApiResponse(responseCode = "200", description = "Return a subject.avsc object")
    @ApiResponse(responseCode = "401", description = "Access denied error occurred")
    @ApiResponse(responseCode = "403", description = "Not Authorised error occurred")
    @ApiResponse(responseCode = "404", description = "Subject cannot be found")
    @NeedsPermissionOnSubject(entity = Entity.SOURCE, operation = READ)
    public Response getAllSourcesJson(
            @PathParam(SUBJECT_ID) String subjectId) throws IOException {
        return ResponseHandler.getJsonResponse(request, getAllSourcesWorker(subjectId));
    }

    /**
     * AVRO function that returns all known sources for the given subject.
     */
    @GET
    @Produces(AVRO_BINARY)
    @Path("/" + GET_ALL_SOURCES + "/{" + SUBJECT_ID + "}")
    @Operation(summary = "Return a User value",
            description = "Return all known sources associated with the give subjectID")
    @ApiResponse(responseCode = "500", description = "An error occurs while executing")
    @ApiResponse(responseCode = "204", description = "No value for the given parameters")
    @ApiResponse(responseCode = "200", description = "Return a subject.avsc object")
    @ApiResponse(responseCode = "401", description = "Access denied error occurred")
    @ApiResponse(responseCode = "403", description = "Not Authorised error occurred")
    @ApiResponse(responseCode = "404", description = "Subject cannot be found")
    @NeedsPermissionOnSubject(entity = Entity.SOURCE, operation = READ)
    public Response getAllSourcesAvro(
            @PathParam(SUBJECT_ID) String subjectId) throws IOException {
        return ResponseHandler.getAvroResponse(request, getAllSourcesWorker(subjectId));
    }

    /**
     * Actual implementation of AVRO and JSON getAllSources.
     **/
    private Subject getAllSourcesWorker(String subjectId) throws ConnectException {
        Param.isValidSubject(subjectId);

        Subject subject = new Subject();

        if (SubjectDataAccessObject.exist(subjectId, context)) {
            subject = SourceDataAccessObject.findAllSourcesByUser(subjectId, context);
        }

        return subject;
    }
}
