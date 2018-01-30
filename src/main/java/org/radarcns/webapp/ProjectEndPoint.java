package org.radarcns.webapp;

import static org.radarcns.auth.authorization.Permission.PROJECT_READ;
import static org.radarcns.auth.authorization.Permission.SUBJECT_READ;
import static org.radarcns.auth.authorization.RadarAuthorization.checkPermission;
import static org.radarcns.auth.authorization.RadarAuthorization.checkPermissionOnProject;
import static org.radarcns.webapp.util.BasePath.PROJECT;
import static org.radarcns.webapp.util.BasePath.SUBJECTS;

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
import org.radarcns.webapp.exception.NotFoundException;
import org.radarcns.webapp.util.Parameter;

@Path("/" + PROJECT)
public class ProjectEndPoint {
    private static final String PROJECT_NAME = "projectName";

    @Inject
    private RadarToken token;

    @Inject
    private ManagementPortalClient mpClient;

    //--------------------------------------------------------------------------------------------//
    //                                       PROJECTS                                             //
    //--------------------------------------------------------------------------------------------//

    /**
     * JSON function that returns all available projects.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Returns a list of projects",
            description = "Each project can have multiple source-types associated with it")
    @ApiResponse(responseCode = "500", description = "An error occurs while executing, in the body"
            + "there is a message.avsc object with more details")
    @ApiResponse(responseCode = "200", description = "Return a list of project objects")
    @ApiResponse(responseCode = "401", description = "Access denied error occurred")
    @ApiResponse(responseCode = "403", description = "Not Authorised error occurred")
    public Response getAllProjectsJson()
            throws IOException, GeneralSecurityException {
        checkPermission(token, PROJECT_READ);
        return Response.status(Status.OK)
                .entity(mpClient.getProjects().values())
                .build();
    }

    /**
     * JSON function that returns all information related to the given project identifier.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{" + PROJECT_NAME + "}")
    @Operation(summary = "Return the information related to given project identifier",
            description = "Each project can have multiple deviceID associated with it")
    @ApiResponse(responseCode = "500", description = "An error occurs while executing, in the body"
            + "there is a message.avsc object with more details")
    @ApiResponse(responseCode = "404", description =
            "No value for the given parameters, in the body"
                    + "there is a message.avsc object with more details")
    @ApiResponse(responseCode = "200", description =
            "Return the project object associated with the "
                    + "given project identifier")
    @ApiResponse(responseCode = "401", description = "Access denied error occurred")
    @ApiResponse(responseCode = "403", description = "Not Authorised error occurred")
    public Response getProjectJson(@PathParam(PROJECT_NAME) String projectName)
            throws IOException, GeneralSecurityException, NotFoundException {
        checkPermissionOnProject(token, PROJECT_READ, projectName);
        return Response.status(Status.OK)
                .entity(mpClient.getProject(projectName))
                .build();
    }


    /**
     * JSON function that returns all available subject based on the Study ID (Project ID).
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{" + Parameter.PROJECT_NAME + "}" + "/" + SUBJECTS)
    @Operation(summary = "Return a list of subjects contained within a study",
            description = "Each subject can have multiple sourceID associated with him")
    @ApiResponse(responseCode = "500", description = "An error occurs while executing, in the body"
            + "there is a message.avsc object with more details")
    @ApiResponse(responseCode = "404", description =
            "No value for the given parameters, in the body"
                    + "there is a message.avsc object with more details")
    @ApiResponse(responseCode = "200", description = "Return a list of subjects objects")
    @ApiResponse(responseCode = "401", description = "Access denied error occurred")
    @ApiResponse(responseCode = "403", description = "Not Authorised error occurred")
    public Response getAllSubjectsJsonFromStudy(
            @PathParam(Parameter.PROJECT_NAME) String projectName)
            throws IOException, GeneralSecurityException, NotFoundException {
        checkPermissionOnProject(token, SUBJECT_READ, projectName);
        return Response.status(Status.OK)
                .entity(mpClient.getAllSubjectsFromProject(projectName))
                .build();
    }
}
