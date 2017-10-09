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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.radarcns.managementportal.MpClient;
import org.radarcns.managementportal.Project;
import org.radarcns.managementportal.Subject;
import org.radarcns.webapp.util.ResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.radarcns.webapp.util.BasePath.PROJECT;
import static org.radarcns.webapp.util.BasePath.SUBJECTS;
import static org.radarcns.webapp.util.Parameter.STUDY_ID;
import static org.radarcns.webapp.util.Parameter.SUBJECT_ID;

/**
 *  Management Portal web-app. Function set to access subject and source information from MP.
 *  A subject is a person enrolled for in a study. A source is a device linked to the subject.
 */
@Api
@Path("/" + "mp")
public class ManagementPortalEndPoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManagementPortalEndPoint.class);

    private static final String PROJECT_ID = "projectId";

    @Context
    private ServletContext context;
    @Context private HttpServletRequest request;

    //--------------------------------------------------------------------------------------------//
    //                                        SUBJECTS                                            //
    //--------------------------------------------------------------------------------------------//
    /**
     * JSON function that returns all available subject.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/" + SUBJECTS)
    @ApiOperation(
            value = "Return a list of subjects",
            notes = "Each subject can have multiple sourceID associated with him")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "An error occurs while executing, in the body"
                    + "there is a message.avsc object with more details"),
            @ApiResponse(code = 204, message = "No value for the given parameters, in the body"
                    + "there is a message.avsc object with more details"),
            @ApiResponse(code = 200, message = "Return a list of subject.avsc objects")})
    public Response getAllSubjectsJson() {
        try {
            MpClient mpClient = new MpClient(context);
            Response response = MpClient.getJsonResponse(mpClient.getSubjects());
            LOGGER.info("Response : " + response.toString());
            return response;
        } catch (Exception exec) {
            LOGGER.error(exec.getMessage(), exec);
            return ResponseHandler.getJsonErrorResponse(request, "Your request cannot be"
                    + "completed. If this error persists, please contact " +
                    "the service administrator.");
        }
    }


    /**
     * JSON function that returns all available subject based on the Study ID (Project ID).
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/" + PROJECT + "/{" + STUDY_ID + "}" + "/" + SUBJECTS)
    @ApiOperation(
            value = "Return a list of subjects",
            notes = "Each subject can have multiple sourceID associated with him")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "An error occurs while executing, in the body"
                    + "there is a message.avsc object with more details"),
            @ApiResponse(code = 204, message = "No value for the given parameters, in the body"
                    + "there is a message.avsc object with more details"),
            @ApiResponse(code = 200, message = "Return a list of subject.avsc objects")})
    public Response getAllSubjectsJsonFromStudy(
            @PathParam(STUDY_ID) String study
    ) {
        try {
            MpClient mpClient = new MpClient(context);
            Response response = MpClient.getJsonResponse(
                    mpClient.getAllSubjectsFromStudy(Integer.parseInt(study)));
            LOGGER.info("Response : " + response.toString());
            return response;
        } catch (Exception exec) {
            LOGGER.error(exec.getMessage(), exec);
            return ResponseHandler.getJsonErrorResponse(request, "Your request cannot be"
                    + "completed. If this error persists, please contact the service administrator.");
        }
    }


    /**
     * JSON function that returns all information related to the given subject identifier.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/" + SUBJECTS + "/{" + SUBJECT_ID + "}")
    @ApiOperation(
            value = "Return the information related to given subject identifier",
            notes = "Some information are not implemented yet. The returned values are hardcoded.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "An error occurs while executing, in the body"
                    + "there is a message.avsc object with more details"),
            @ApiResponse(code = 204, message = "No value for the given parameters, in the body"
                    + "there is a message.avsc object with more details"),
            @ApiResponse(code = 200, message = "Return the subject.avsc object associated with the "
                    + "given subject identifier")})
    public Response getSubjectJson(
            @PathParam(SUBJECT_ID) String subjectId
    ) {
        try {
            MpClient mpClient = new MpClient(context);
            Subject subject = mpClient.getSubject(subjectId);
            Response response = MpClient.getJsonResponse(subject);
            LOGGER.info("Response : " + response.toString());
            return response;
        } catch (Exception exec) {
            LOGGER.error(exec.getMessage(), exec);
            return ResponseHandler.getAvroErrorResponse(request);
        }
    }


    //--------------------------------------------------------------------------------------------//
    //                                       PROJECTS                                             //
    //--------------------------------------------------------------------------------------------//

    /**
     * JSON function that returns all available projects.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/" + PROJECT)
    @ApiOperation(
            value = "Return a list of subjects",
            notes = "Each subject can have multiple sourceID associated with him")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "An error occurs while executing, in the body"
                    + "there is a message.avsc object with more details"),
            @ApiResponse(code = 204, message = "No value for the given parameters, in the body"
                    + "there is a message.avsc object with more details"),
            @ApiResponse(code = 200, message = "Return a list of subject.avsc objects")})
    public Response getAllProjectsJson() {
        try {
            MpClient mpClient = new MpClient(context);
            Response response = MpClient.getJsonResponse(mpClient.getAllProjects(context));
            LOGGER.info("Response : " + response.getEntity());
            return response;
        } catch (Exception exec) {
            LOGGER.error(exec.getMessage(), exec);
            return ResponseHandler.getJsonErrorResponse(request, "Your request cannot be"
                    + "completed. If this error persists, please contact the service administrator.");
        }
    }


    /**
     * JSON function that returns all information related to the given project identifier.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/" + PROJECT + "/{" + PROJECT_ID + "}")
    @ApiOperation(
            value = "Return the information related to given subject identifier",
            notes = "Some information are not implemented yet. The returned values are hardcoded.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "An error occurs while executing, in the body"
                    + "there is a message.avsc object with more details"),
            @ApiResponse(code = 204, message = "No value for the given parameters, in the body"
                    + "there is a message.avsc object with more details"),
            @ApiResponse(code = 200, message = "Return the subject.avsc object associated with the "
                    + "given subject identifier")})
    public Response getProjectJson(
            @PathParam(PROJECT_ID) String projectId
    ) {
        try {
            MpClient mpClient = new MpClient(context);
            Project project = mpClient.getProject(projectId, context);
            Response response = MpClient.getJsonResponse(project);
            LOGGER.info("Response : " + response.toString());
            return response;
        } catch (Exception exec) {
            LOGGER.error(exec.getMessage(), exec);
            return ResponseHandler.getAvroErrorResponse(request);
        }
    }
}
