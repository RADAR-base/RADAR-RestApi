package org.radarcns.auth;

import static org.radarcns.webapp.filter.AuthenticationFilter.ERROR;
import static org.radarcns.webapp.filter.AuthenticationFilter.ERROR_DESCRIPTION;
import static org.radarcns.webapp.filter.AuthenticationFilter.REALM;
import static org.radarcns.webapp.filter.AuthenticationFilter.REALM_VALUE;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.radarcns.auth.authorization.Permission;
import org.radarcns.webapp.filter.AuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Check that the token has given permissions.
 */
public class PermissionFilter implements ContainerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(PermissionFilter.class);

    @Context
    private ResourceInfo resourceInfo;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        NeedsPermission annotation = resourceInfo.getResourceMethod()
                .getAnnotation(NeedsPermission.class);
        Permission permission = new Permission(annotation.entity(), annotation.operation());

        if (!AuthenticationFilter.getToken(requestContext).hasPermission(permission)) {
            abortWithForbidden(requestContext, "No permission " + permission);
        }
    }

    /**
     * Abort the request with a forbidden status. The caller must ensure that no other changes are
     * made to the context (i.e., make a quick return).
     *
     * @param requestContext context to abort
     * @param message message to log and pass as a status message to clients.
     */
    public static void abortWithForbidden(ContainerRequestContext requestContext, String message) {
        logger.warn("[403] {}: {}",
                requestContext.getUriInfo().getPath(), message);
        Response.ResponseBuilder builder = Response.status(Status.FORBIDDEN);
        builder.header("WWW-Authenticate", "Bearer "
                + REALM + "=" + REALM_VALUE + ", "
                + ERROR + "=insufficient_scope" + ", "
                + ERROR_DESCRIPTION + "=" + message);
        requestContext.abortWith(builder.build());
    }
}
