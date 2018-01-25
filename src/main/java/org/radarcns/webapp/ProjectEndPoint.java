package org.radarcns.webapp;

import static org.radarcns.auth.authorization.Permission.PROJECT_READ;
import static org.radarcns.auth.authorization.Permission.SUBJECT_READ;
import static org.radarcns.auth.authorization.RadarAuthorization.checkPermission;
import static org.radarcns.auth.authorization.RadarAuthorization.checkPermissionOnProject;
import static org.radarcns.security.utils.SecurityUtils.getRadarToken;
import static org.radarcns.webapp.util.BasePath.PROJECT;
import static org.radarcns.webapp.util.BasePath.SUBJECTS;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.radarcns.auth.exception.NotAuthorizedException;
import org.radarcns.exception.TokenException;
import org.radarcns.listener.ContextResourceManager;
import org.radarcns.listener.managementportal.ManagementPortalClient;
import org.radarcns.listener.managementportal.ManagementPortalClientManager;
import org.radarcns.domain.managementportal.Project;
import org.radarcns.security.exception.AccessDeniedException;
import org.radarcns.service.SubjectService;
import org.radarcns.webapp.exception.NotFoundException;
import org.radarcns.webapp.util.Parameter;
import org.radarcns.webapp.util.ResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/" + PROJECT)
public class ProjectEndPoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectEndPoint.class);

    private static final String PROJECT_NAME = "projectName";

    @Context
    private ServletContext context;
    @Context
    private HttpServletRequest request;

    //--------------------------------------------------------------------------------------------//
    //                                       PROJECTS                                             //
    //--------------------------------------------------------------------------------------------//

    /**
     * JSON function that returns all available projects.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Returns a list of projects",
            description = "Each project can have multiple sourceType-types associated with it")
    @ApiResponse(responseCode = "500", description = "An error occurs while executing, in the body"
            + "there is a message.avsc object with more details")
    @ApiResponse(responseCode = "200", description = "Return a list of project objects")
    @ApiResponse(responseCode = "401", description = "Access denied error occurred")
    @ApiResponse(responseCode = "403", description = "Not Authorised error occurred")
    public Response getAllProjectsJson() {
        try {
            checkPermission(getRadarToken(request), PROJECT_READ);
            ManagementPortalClient managementPortalClient = ManagementPortalClientManager
                    .getManagementPortalClient(context);
            Response response = Response.status(Status.OK)
                    .entity(managementPortalClient.getProjects().values()).build();
            LOGGER.info("Response : " + response.getEntity());
            return response;
        } catch (AccessDeniedException exc) {
            LOGGER.error(exc.getMessage(), exc);
            return ResponseHandler.getJsonAccessDeniedResponse(request, exc.getMessage());
        } catch (NotAuthorizedException exc) {
            LOGGER.error(exc.getMessage(), exc);
            return ResponseHandler.getJsonNotAuthorizedResponse(request, exc.getMessage());
        } catch (TokenException | IOException exe) {
            LOGGER.error(exe.getMessage(), exe);
            return ResponseHandler.getJsonErrorResponse(request, exe.getMessage());
        }
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
    @ApiResponse(responseCode = "401", description = "Access denied error occured")
    @ApiResponse(responseCode = "403", description = "Not Authorised error occured")
    public Response getProjectJson(@PathParam(PROJECT_NAME) String projectName) {
        try {
            checkPermissionOnProject(getRadarToken(request), PROJECT_READ, projectName);
            ManagementPortalClient managementPortalClient = ManagementPortalClientManager
                    .getManagementPortalClient(context);
            Project project = managementPortalClient.getProject(projectName);
            Response response = Response.status(Status.OK).entity(project).build();
            LOGGER.info("Response : " + response.toString());
            return response;
        } catch (AccessDeniedException exc) {
            return ResponseHandler.getJsonAccessDeniedResponse(request, exc.getMessage());
        } catch (NotAuthorizedException exc) {
            return ResponseHandler.getJsonNotAuthorizedResponse(request, exc.getMessage());
        } catch (TokenException exe) {
            return ResponseHandler.getJsonErrorResponse(request, exe.getMessage());
        } catch (MalformedURLException exe) {
            LOGGER.error(exe.getMessage(), exe);
            return ResponseHandler.getJsonErrorResponse(request, exe.getMessage());
        } catch (IOException exe) {
            LOGGER.error(exe.getMessage(), exe);
            return ResponseHandler.getJsonErrorResponse(request, "Cannot deserialize  "
                    + "project response received");
        } catch (NotFoundException exe) {
            return ResponseHandler.getJsonNotFoundResponse(request, exe.getMessage());
        }
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
    @ApiResponse(responseCode = "401", description = "Access denied error occured")
    @ApiResponse(responseCode = "403", description = "Not Authorised error occured")
    public Response getAllSubjectsJsonFromStudy(
            @PathParam(Parameter.PROJECT_NAME) String projectName) {
        try {
            checkPermissionOnProject(getRadarToken(request), SUBJECT_READ, projectName);
            SubjectService subjectService = ContextResourceManager.getSubjectService(context);
            Response response = Response.status(Status.OK).entity(
                   subjectService.getAllSubjectsFromProject(projectName)).build();
            LOGGER.info("Response : " + response.toString());
            return response;
        } catch (AccessDeniedException exc) {
            LOGGER.error(exc.getMessage(), exc);
            return ResponseHandler.getJsonAccessDeniedResponse(request, exc.getMessage());
        } catch (NotAuthorizedException exc) {
            LOGGER.error(exc.getMessage(), exc);
            return ResponseHandler.getJsonNotAuthorizedResponse(request, exc.getMessage());
        } catch (IOException exec) {
            LOGGER.error(exec.getMessage(), exec);
            return ResponseHandler.getJsonErrorResponse(request, "Your request cannot be"
                    + "completed. If this error persists, please contact "
                    + "the service administrator. \n " + exec.getMessage());
        } catch (TokenException exe) {
            return ResponseHandler.getJsonErrorResponse(request, exe.getMessage());
        } catch (NotFoundException exe) {
            return ResponseHandler.getJsonNotFoundResponse(request, exe.getMessage());
        }
    }
}
