package org.radarcns.webapp.resource;

import static org.radarcns.auth.authorization.Permission.Entity.SOURCETYPE;
import static org.radarcns.auth.authorization.Permission.Operation.READ;
import static org.radarcns.webapp.util.BasePath.SOURCE_TYPES;
import static org.radarcns.webapp.util.Parameter.CATALOGUE_VERSION;
import static org.radarcns.webapp.util.Parameter.MODEL;
import static org.radarcns.webapp.util.Parameter.PRODUCER;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.io.IOException;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;
import org.radarcns.listener.managementportal.ManagementPortalClient;
import org.radarcns.security.filter.NeedsPermission;

@Provider
@Path("/" + SOURCE_TYPES)
public class SourceTypeEndPoint {
    @Inject
    private ManagementPortalClient mpClient;

    //--------------------------------------------------------------------------------------------//
    //                                       SOURCE-TYPES
    //
    //--------------------------------------------------------------------------------------------//

    /**
     * JSON function that returns all available source types.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Returns a list of source-types")
    @ApiResponse(responseCode = "500", description = "An error occurs while executing, in the body"
            + "there is a message.avsc object with more details")
    @ApiResponse(responseCode = "200", description = "Return a list of source-type objects")
    @ApiResponse(responseCode = "401", description = "Access denied error occurred")
    @ApiResponse(responseCode = "403", description = "Not Authorised error occurred")
    @NeedsPermission(entity = SOURCETYPE, operation = READ)
    public Response getAllSourceTypesJson() throws IOException {
        return Response.status(Status.OK)
                .entity(mpClient.getSourceTypes().values())
                .build();
    }

    /**
     * JSON function that returns a single source type.
     */
    @GET
    @Path("/{" + PRODUCER + "}/{" + MODEL + "}/{" + CATALOGUE_VERSION + "}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Returns a list of projects",
            description = "Each project can have multiple source-types associated with it")
    @ApiResponse(responseCode = "500", description = "An error occurs while executing, in the body"
            + "there is a message.avsc object with more details")
    @ApiResponse(responseCode = "200", description = "Return a list of project objects")
    @ApiResponse(responseCode = "401", description = "Access denied error occurred")
    @ApiResponse(responseCode = "403", description = "Not Authorised error occurred")
    @ApiResponse(responseCode = "404", description = "Source type not found")
    @NeedsPermission(entity = SOURCETYPE, operation = READ)
    public Response getSourceTypeJson(
            @PathParam(PRODUCER) String producer,
            @PathParam(MODEL) String model,
            @PathParam(CATALOGUE_VERSION) String catalogVersion) throws IOException {
        return Response.status(Status.OK)
                .entity(mpClient.getSourceType(producer, model, catalogVersion))
                .build();
    }
}