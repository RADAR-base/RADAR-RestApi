package org.radarcns.webapp;

import static org.radarcns.auth.authorization.Permission.SOURCETYPE_READ;
import static org.radarcns.auth.authorization.RadarAuthorization.checkPermission;
import static org.radarcns.security.utils.SecurityUtils.getRadarToken;
import static org.radarcns.webapp.util.BasePath.SOURCE_TYPES;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.io.IOException;
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
import org.radarcns.listener.managementportal.ManagementPortalClient;
import org.radarcns.listener.managementportal.ManagementPortalClientManager;
import org.radarcns.security.exception.AccessDeniedException;
import org.radarcns.webapp.exception.NotFoundException;
import org.radarcns.webapp.util.ResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/" + SOURCE_TYPES)
public class SourceTypeEndPoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectEndPoint.class);

    private static final String PRODUCER = "producer";

    private static final String MODEL = "model";

    private static final String CATALOGUE_VERSION = "catalogueVersion";

    @Context
    private ServletContext context;
    @Context
    private HttpServletRequest request;

    //--------------------------------------------------------------------------------------------//
    //                                       SOURCE-TYPES
    //
    //--------------------------------------------------------------------------------------------//

    /**
     * JSON function that returns all available projects.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Returns a list of source-types")
    @ApiResponse(responseCode = "500", description = "An error occurs while executing, in the body"
            + "there is a message.avsc object with more details")
    @ApiResponse(responseCode = "200", description = "Return a list of source-type objects")
    @ApiResponse(responseCode = "401", description = "Access denied error occurred")
    @ApiResponse(responseCode = "403", description = "Not Authorised error occurred")
    public Response getAllSourceTypesJson() {
        try {
            checkPermission(getRadarToken(request), SOURCETYPE_READ);
            ManagementPortalClient managementPortalClient = ManagementPortalClientManager
                    .getManagementPortalClient(context);
            Response response = Response.status(Status.OK)
                    .entity(managementPortalClient.getSourceTypes().values()).build();
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
     * JSON function that returns all available projects.
     */
    @GET
    @Path("/{"+PRODUCER+"}/{"+MODEL+"}/{"+CATALOGUE_VERSION+"}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Returns a list of projects",
            description = "Each project can have multiple source-types associated with it")
    @ApiResponse(responseCode = "500", description = "An error occurs while executing, in the body"
            + "there is a message.avsc object with more details")
    @ApiResponse(responseCode = "200", description = "Return a list of project objects")
    @ApiResponse(responseCode = "401", description = "Access denied error occurred")
    @ApiResponse(responseCode = "403", description = "Not Authorised error occurred")
    public Response getAllSourceTypesJson(@PathParam(PRODUCER) String producer,
            @PathParam(MODEL) String model, @PathParam(CATALOGUE_VERSION) String
            catalogVersion) {
        try {
            checkPermission(getRadarToken(request), SOURCETYPE_READ);
            ManagementPortalClient managementPortalClient = ManagementPortalClientManager
                    .getManagementPortalClient(context);
            Response response = Response.status(Status.OK)
                    .entity(managementPortalClient.getSourceType(producer, model, catalogVersion))
                    .build();
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
        } catch (NotFoundException exe) {
            return ResponseHandler.getJsonNotFoundResponse(request, exe.getMessage());
        }
    }
}