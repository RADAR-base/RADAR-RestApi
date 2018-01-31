package org.radarcns.security.filter;

import static java.net.HttpURLConnection.HTTP_FORBIDDEN;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static org.radarcns.webapp.filter.AuthenticationFilter.TOKEN_ATTRIBUTE;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import org.radarcns.auth.authorization.Permission;
import org.radarcns.auth.token.RadarToken;
import org.radarcns.webapp.exception.StatusMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PermissionFilter implements ContainerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(PermissionFilter.class);

    @Context
    private ResourceInfo resourceInfo;

    @Context
    private HttpServletRequest request;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        NeedsPermission annotation = resourceInfo.getResourceMethod()
                .getAnnotation(NeedsPermission.class);
        Permission permission = new Permission(annotation.entity(), annotation.operation());

        RadarToken token = (RadarToken) request.getAttribute(TOKEN_ATTRIBUTE);
        if (!token.hasPermission(permission)) {
            abortWithForbidden(requestContext, "No permission " + permission);
        }
    }

    public static void abortWithForbidden(ContainerRequestContext requestContext, String message) {
        logger.warn("[403] {}: {}",
                requestContext.getUriInfo().getPath(), message);
        Response.ResponseBuilder builder = Response.status(HTTP_FORBIDDEN);
        if (requestContext.getMediaType().isCompatible(APPLICATION_JSON_TYPE)) {
            builder.entity(new StatusMessage("forbidden", message));
        }
        requestContext.abortWith(builder.build());
    }
}
