package org.radarcns.webapp;

/*
 *  Copyright 2016 King's College London and The Hyve
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

import com.mongodb.MongoClient;
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
import org.radarcns.avro.restapi.user.Patient;
import org.radarcns.dao.SourceDataAccessObject;
import org.radarcns.dao.mongo.util.MongoHelper;
import org.radarcns.monitor.Monitors;
import org.radarcns.security.Param;
import org.radarcns.util.ResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SourceDefinition web-app. Function set to access source information.
 */
@Api
@Path("/source")
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
    @Path("/state/{patientID}/{sourceID}")
    @ApiOperation(
            value = "Return a SourceDefinition values",
            notes = "Using the source sensors values arrived within last 60sec, it computes the"
                + "sender status for the given patientID and sourceID")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "An error occurs while executing, in the body"
                + "there is a message.avsc object with more details"),
            @ApiResponse(code = 204, message = "No value for the given parameters, in the body"
                + "there is a message.avsc object with more details"),
            @ApiResponse(code = 200, message = "Return a source.avsc object containing last"
                + "computed status")})
    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    public Response getRTStateByUserDeviceJsonDevStatus(
            @PathParam("patientID") String user,
            @PathParam("sourceID") String source) {
        try {
            return ResponseHandler.getJsonResponse(request,
                getRTStateByUserSourceWorker(user, source));
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
    @Path("/avro/state/{patientID}/{sourceID}")
    @ApiOperation(
            value = "Return a SourceDefinition values",
            notes = "Using the source sensors values arrived within last 60sec, it computes the"
                + "sender status for the given patientID and sourceID")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "An error occurs while executing"),
            @ApiResponse(code = 204, message = "No value for the given parameters"),
            @ApiResponse(code = 200, message = "Return a byte array serialising source.avsc object"
                + "containing last computed status")})
    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    public Response getRTStateByUserDeviceAvroDevStatus(
            @PathParam("patientID") String user,
            @PathParam("sourceID") String source) {
        try {
            return ResponseHandler.getAvroResponse(request,
                getRTStateByUserSourceWorker(user, source));
        } catch (Exception exec) {
            LOGGER.error(exec.getMessage(), exec);
            return ResponseHandler.getAvroErrorResponse(request);
        }
    }

    /**
     * Actual implementation of AVRO and JSON getRTStateByUserDevice.
     **/
    private Source getRTStateByUserSourceWorker(String user, String source)
            throws ConnectException {
        Param.isValidInput(user, source);

        MongoClient client = MongoHelper.getClient(context);

        SourceType sourceType = SourceDataAccessObject.getSourceType(source, client);

        if (sourceType == null) {
            return null;
        }

        Source device = Monitors.getInstance().getState(user, source, sourceType, client);

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
    @Path("/specification/{sourceType}")
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
            @PathParam("sourceType") SourceType source) {
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
    @Path("/avro/specification/{sourceType}")
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
            @PathParam("sourceType") SourceType source) {
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
     * JSON function that returns all known sources for the given user.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/getAllSources/{patientID}")
    @ApiOperation(
            value = "Return a User value",
            notes = "Return all known sources associated with the give patientID")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "An error occurs while executing, in the body"
                + "there is a message.avsc object with more details"),
            @ApiResponse(code = 204, message = "No value for the given parameters, in the body"
                + "there is a message.avsc object with more details"),
            @ApiResponse(code = 200, message = "Return a user.avsc object")})
    public Response getAllSourcesJsonUser(
            @PathParam("patientID") String user) {
        try {
            return ResponseHandler.getJsonResponse(request, getAllSourcesWorker(user));
        } catch (Exception exec) {
            LOGGER.error(exec.getMessage(), exec);
            return ResponseHandler.getJsonErrorResponse(request, "Your request cannot be"
                + "completed. If this error persists, please contact the service administrator.");
        }
    }

    /**
     * AVRO function that returns all known sources for the given user.
     */
    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Path("/avro/getAllSources/{patientID}")
    @ApiOperation(
            value = "Return a User value",
            notes = "Return all known sources associated with the give patientID")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "An error occurs while executing"),
            @ApiResponse(code = 204, message = "No value for the given parameters"),
            @ApiResponse(code = 200, message = "Return a user.avsc object")})
    public Response getAllSourcesAvroUser(
            @PathParam("patientID") String user) {
        try {
            return ResponseHandler.getAvroResponse(request, getAllSourcesWorker(user));
        } catch (Exception exec) {
            LOGGER.error(exec.getMessage(), exec);
            return ResponseHandler.getAvroErrorResponse(request);
        }
    }

    /**
     * Actual implementation of AVRO and JSON getAllSources.
     **/
    private Patient getAllSourcesWorker(String user) throws ConnectException {
        Param.isValidUser(user);

        MongoClient client = MongoHelper.getClient(context);

        Patient patient = SourceDataAccessObject.findAllSourcesByUser(user, client);

        return patient;
    }
}
