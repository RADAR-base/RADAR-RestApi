package org.radarcns.webapp.resource;

import static org.radarcns.auth.authorization.Permission.Entity.PROJECT;
import static org.radarcns.auth.authorization.Permission.Operation.READ;
import static org.radarcns.webapp.resource.BasePath.PROJECTS;
import static org.radarcns.webapp.resource.Parameter.PROJECT_NAME;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.io.IOException;
import java.util.Collection;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.radarcns.auth.NeedsPermission;
import org.radarcns.listener.managementportal.ManagementPortalClient;
import org.radarcns.management.service.dto.ProjectDTO;
import org.radarcns.webapp.filter.Authenticated;

@Authenticated
@Path("/" + PROJECTS)
public class ProjectEndPoint {

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
    @ApiResponse(responseCode = "500", description = "An error occurs while executing")
    @ApiResponse(responseCode = "200", description = "Return a list of project objects")
    @ApiResponse(responseCode = "401", description = "Access denied error occurred")
    @ApiResponse(responseCode = "403", description = "Not Authorised error occurred")
    @NeedsPermission(entity = PROJECT, operation = READ)
    public Collection<ProjectDTO> getAllProjectsJson() throws IOException {
        return mpClient.getProjects().values();
    }

    /**
     * JSON function that returns all information related to the given project identifier.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{" + PROJECT_NAME + "}")
    @Operation(summary = "Return the information related to given project identifier",
            description = "Each project can have multiple source-types associated with it")
    @ApiResponse(responseCode = "500", description = "An error occurs while executing")
    @ApiResponse(responseCode = "200", description = "Return the project object associated with "
            + "the given project identifier")
    @ApiResponse(responseCode = "401", description = "Access denied error occurred")
    @ApiResponse(responseCode = "403", description = "Not Authorised error occurred")
    @ApiResponse(responseCode = "404", description = "Project cannot be found")
    @NeedsPermission(entity = PROJECT, operation = READ)
    public ProjectDTO getProjectJson(
            @PathParam(PROJECT_NAME) String projectName) throws IOException {
        return mpClient.getProject(projectName);
    }

}
