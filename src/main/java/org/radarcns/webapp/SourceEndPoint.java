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

import static org.radarcns.webapp.util.BasePath.AVRO;
import static org.radarcns.webapp.util.BasePath.GET_ALL_SOURCES;
import static org.radarcns.webapp.util.BasePath.SOURCE;
import static org.radarcns.webapp.util.BasePath.SPECIFICATION;
import static org.radarcns.webapp.util.BasePath.STATE;
import static org.radarcns.webapp.util.Parameter.SOURCE_ID;
import static org.radarcns.webapp.util.Parameter.SOURCE_TYPE;
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
import org.radarcns.avro.restapi.source.Source;
import org.radarcns.avro.restapi.source.SourceSpecification;
import org.radarcns.avro.restapi.source.SourceType;
import org.radarcns.avro.restapi.subject.Subject;
import org.radarcns.dao.SourceDataAccessObject;
import org.radarcns.dao.SubjectDataAccessObject;
import org.radarcns.monitor.Monitors;
import org.radarcns.security.Param;
import org.radarcns.webapp.util.ResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SourceDefinition web-app. Function set to access source information.
 */
@Api
@Path("/" + SOURCE)
public class SourceEndPoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(SourceEndPoint.class);

    @Context private ServletContext context;
    @Context private HttpServletRequest request;

    //--------------------------------------------------------------------------------------------//
    //                                       STATE FUNCTIONS                                      //
    //--------------------------------------------------------------------------------------------//
    /**
     * JSON function that returns the status of the given source.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/" + STATE + "/{" + SUBJECT_ID + "}/{" + SOURCE_ID + "}")
    @ApiOperation(
            value = "Return a SourceDefinition values",
            notes = "Using the source sensors values arrived within last 60sec, it computes the"
                + "sender status for the given subjectID and sourceID")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "An error occurs while executing, in the body"
                + "there is a message.avsc object with more details"),
            @ApiResponse(code = 204, message = "No value for the given parameters, in the body"
                + "there is a message.avsc object with more details"),
            @ApiResponse(code = 200, message = "Return a source.avsc object containing last"
                + "computed status")})
    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    public Response getRTStateByUserDeviceJsonDevStatus(
            @PathParam(SUBJECT_ID) String subject,
            @PathParam(SOURCE_ID) String source) {
        try {
            return ResponseHandler.getJsonResponse(request,
                getRTStateByUserSourceWorker(subject, source));
        } catch (Exception exec) {
            LOGGER.error(exec.getMessage(), exec);
            return ResponseHandler.getJsonErrorResponse(request, "Your request cannot be"
                + "completed. If this error persists, please contact the service administrator.");
        }
    }

    /**
     * AVRO function that returns the status of the given source.
     */
    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Path("/" + AVRO + "/" + STATE + "/{" + SUBJECT_ID + "}/{" + SOURCE_ID + "}")
    @ApiOperation(
            value = "Return a SourceDefinition values",
            notes = "Using the source sensors values arrived within last 60sec, it computes the"
                + "sender status for the given subjectID and sourceID")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "An error occurs while executing"),
            @ApiResponse(code = 204, message = "No value for the given parameters"),
            @ApiResponse(code = 200, message = "Return a byte array serialising source.avsc object"
                + "containing last computed status")})
    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    public Response getRTStateByUserDeviceAvroDevStatus(
            @PathParam(SUBJECT_ID) String subject,
            @PathParam(SOURCE_ID) String source) {
        try {
            return ResponseHandler.getAvroResponse(request,
                getRTStateByUserSourceWorker(subject, source));
        } catch (Exception exec) {
            LOGGER.error(exec.getMessage(), exec);
            return ResponseHandler.getAvroErrorResponse(request);
        }
    }

    /**
     * Actual implementation of AVRO and JSON getRTStateByUserDevice.
     **/
    private Source getRTStateByUserSourceWorker(String subject, String source)
            throws ConnectException {
        Param.isValidInput(subject, source);

        SourceType sourceType = SourceDataAccessObject.getSourceType(source, context);

        if (sourceType == null) {
            return null;
        }

        Source device = Monitors.getInstance().getState(subject, source, sourceType, context);

        return device;
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
    @ApiOperation(
            value = "Return a SourceDefinition specification",
            notes = "Return the data specification of all on-board sensors for the given"
                + "source type")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "An error occurs while executing, in the body"
                + "there is a message.avsc object with more details"),
            @ApiResponse(code = 204, message = "No value for the given parameters, in the body"
                + "there is a message.avsc object with more details"),
            @ApiResponse(code = 200, message = "Return a source_specification.avsc object"
                + "containing last computed status")})
    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    public Response getSourceSpecificationJson(
            @PathParam(SOURCE_TYPE) SourceType source) {
        try {
            return ResponseHandler.getJsonResponse(request, getSourceSpecificationWorker(source));
        } catch (Exception exec) {
            LOGGER.error(exec.getMessage(), exec);
            return ResponseHandler.getJsonErrorResponse(request, "Your request cannot be"
                + "completed. If this error persists, please contact the service administrator.");
        }
    }

    /**
     * AVRO function that returns the status of the given data.
     */
    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Path("/" + AVRO + "/" + SPECIFICATION + "/{" + SOURCE_TYPE + "}")
    @ApiOperation(
            value = "Return a SourceDefinition specification",
            notes = "Return the data specification of all on-board sensors for the given"
                + "source type")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "An error occurs while executing"),
            @ApiResponse(code = 204, message = "No value for the given parameters"),
            @ApiResponse(code = 200, message = "Return a source_specification.avsc object"
                + "containing last computed status")})
    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    public Response getSourceSpecificationAvro(
            @PathParam(SOURCE_TYPE) SourceType source) {
        try {
            return ResponseHandler.getAvroResponse(request, getSourceSpecificationWorker(source));
        } catch (Exception exec) {
            LOGGER.error(exec.getMessage(), exec);
            return ResponseHandler.getAvroErrorResponse(request);
        }
    }

    /**
     * Actual implementation of AVRO and JSON getSpecification.
     **/
    private SourceSpecification getSourceSpecificationWorker(SourceType source)
            throws ConnectException {
        SourceSpecification device = Monitors.getInstance().getSpecification(source);

        return device;
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
    @ApiOperation(
            value = "Return a User value",
            notes = "Return all known sources associated with the give subjectID")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "An error occurs while executing, in the body"
                + "there is a message.avsc object with more details"),
            @ApiResponse(code = 204, message = "No value for the given parameters, in the body"
                + "there is a message.avsc object with more details"),
            @ApiResponse(code = 200, message = "Return a subject.avsc object")})
    public Response getAllSourcesJsonUser(
            @PathParam(SUBJECT_ID) String subject) {
        try {
            return ResponseHandler.getJsonResponse(request, getAllSourcesWorker(subject));
        } catch (Exception exec) {
            LOGGER.error(exec.getMessage(), exec);
            return ResponseHandler.getJsonErrorResponse(request, "Your request cannot be"
                    + "completed. If this error persists, please contact the service"
                    + "administrator.");
        }
    }

    /**
     * AVRO function that returns all known sources for the given subject.
     */
    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Path("/" + AVRO + "/" + GET_ALL_SOURCES + "/{" + SUBJECT_ID + "}")
    @ApiOperation(
            value = "Return a User value",
            notes = "Return all known sources associated with the give subjectID")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "An error occurs while executing"),
            @ApiResponse(code = 204, message = "No value for the given parameters"),
            @ApiResponse(code = 200, message = "Return a subject.avsc object")})
    public Response getAllSourcesAvroUser(
            @PathParam(SUBJECT_ID) String subject) {
        try {
            return ResponseHandler.getAvroResponse(request, getAllSourcesWorker(subject));
        } catch (Exception exec) {
            LOGGER.error(exec.getMessage(), exec);
            return ResponseHandler.getAvroErrorResponse(request);
        }
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
