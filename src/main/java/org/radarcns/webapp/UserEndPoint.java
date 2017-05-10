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
import org.radarcns.avro.restapi.user.Cohort;
import org.radarcns.dao.UserDataAccessObject;
import org.radarcns.dao.mongo.util.MongoHelper;
import org.radarcns.util.ResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User web-app. Function set to access users information.
 */
@Api
@Path("/user")
public class UserEndPoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserEndPoint.class);

    @Context private ServletContext context;
    @Context private HttpServletRequest request;

    //--------------------------------------------------------------------------------------------//
    //                                        ALL PATIENTS                                        //
    //--------------------------------------------------------------------------------------------//
    /**
     * JSON function that returns all available patient.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/getAllPatients/{studyID}")
    @ApiOperation(
            value = "Return a list of users",
            notes = "Each user can have multiple sourceID associated with him")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "An error occurs while executing, in the body"
                + "there is a message.avsc object with more details"),
            @ApiResponse(code = 204, message = "No value for the given parameters, in the body"
                + "there is a message.avsc object with more details"),
            @ApiResponse(code = 200, message = "Return a list of user.avsc objects")})
    public Response getAllPatientsJsonUser(
            @PathParam("studyID") String study
    ) {
        try {
            return ResponseHandler.getJsonResponse(request, getAllPatientsWorker());
        } catch (Exception exec) {
            LOGGER.error(exec.getMessage(), exec);
            return ResponseHandler.getJsonErrorResponse(request, "Your request cannot be"
                + "completed. If this error persists, please contact the service administrator.");
        }
    }

    /**
     * AVRO function that returns all available patient.
     */
    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Path("/avro/getAllPatients/{studyID}")
    @ApiOperation(
            value = "Return a list of users",
            notes = "Each user can have multiple sourceID associated with him")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "An error occurs while executing"),
            @ApiResponse(code = 204, message = "No value for the given parameters"),
            @ApiResponse(code = 200, message = "Return a byte array serialising a list of"
                + "user.avsc objects")})
    public Response getAllPatientsAvroUser(
            @PathParam("studyID") String study
    ) {
        try {
            return ResponseHandler.getAvroResponse(request, getAllPatientsWorker());
        } catch (Exception exec) {
            LOGGER.error(exec.getMessage(), exec);
            return ResponseHandler.getAvroErrorResponse(request);
        }
    }

    /**
     * Actual implementation of AVRO and JSON getAllPatients.
     **/
    private Cohort getAllPatientsWorker() throws ConnectException {
        MongoClient client = MongoHelper.getClient(context);

        Cohort cohort = UserDataAccessObject.findAllUsers(client);

        return cohort;
    }
}
