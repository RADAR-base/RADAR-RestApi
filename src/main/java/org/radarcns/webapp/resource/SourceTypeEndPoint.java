package org.radarcns.webapp.resource;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.radarcns.auth.authorization.Permission.Entity.SOURCETYPE;
import static org.radarcns.auth.authorization.Permission.Operation.READ;
import static org.radarcns.webapp.resource.BasePath.SOURCE_TYPES;
import static org.radarcns.webapp.resource.Parameter.CATALOGUE_VERSION;
import static org.radarcns.webapp.resource.Parameter.MODEL;
import static org.radarcns.webapp.resource.Parameter.PRODUCER;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.io.IOException;
import java.util.Collection;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import org.radarbase.jersey.auth.Authenticated;
import org.radarbase.jersey.auth.NeedsPermission;
import org.radarcns.catalog.SourceCatalog;
import org.radarcns.domain.managementportal.SourceTypeDTO;
import org.radarcns.webapp.validation.Alphanumeric;

@Authenticated
@Path('/' + SOURCE_TYPES)
public class SourceTypeEndPoint {

    @Inject
    private SourceCatalog sourceCatalog;

    //--------------------------------------------------------------------------------------------//
    //                                       SOURCE-TYPES
    //
    //--------------------------------------------------------------------------------------------//

    /**
     * JSON function that returns all available source types.
     */
    @GET
    @Produces(APPLICATION_JSON)
    @Operation(summary = "Returns a list of source-types")
    @ApiResponse(responseCode = "500", description = "An error occurs while executing")
    @ApiResponse(responseCode = "200", description = "Return a list of source-type objects")
    @ApiResponse(responseCode = "401", description = "Access denied error occurred")
    @ApiResponse(responseCode = "403", description = "Not Authorised error occurred")
    @NeedsPermission(entity = SOURCETYPE, operation = READ)
    public Collection<SourceTypeDTO> getAllSourceTypesJson() throws IOException {
        return sourceCatalog.getSourceTypes();
    }

    /**
     * JSON function that returns a single source type.
     */
    @GET
    @Path("/{" + PRODUCER + "}/{" + MODEL + "}/{" + CATALOGUE_VERSION + "}")
    @Produces(APPLICATION_JSON)
    @Operation(summary = "Returns a source-type requested by the parameters")
    @ApiResponse(responseCode = "500", description = "An error occurs while executing")
    @ApiResponse(responseCode = "200", description = "Return the source-type requested")
    @ApiResponse(responseCode = "401", description = "Access denied error occurred")
    @ApiResponse(responseCode = "403", description = "Not Authorised error occurred")
    @ApiResponse(responseCode = "404", description = "Source type not found")
    @NeedsPermission(entity = SOURCETYPE, operation = READ)
    public SourceTypeDTO getSourceTypeJson(
            @Alphanumeric @PathParam(PRODUCER) String producer,
            @Alphanumeric @PathParam(MODEL) String model,
            @Alphanumeric @PathParam(CATALOGUE_VERSION) String catalogVersion) throws IOException {
        return sourceCatalog.getSourceType(producer, model, catalogVersion);
    }
}