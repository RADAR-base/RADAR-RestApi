package org.radarcns.security.filter;

import static org.radarcns.security.filter.PermissionFilter.abortWithForbidden;
import static org.radarcns.webapp.filter.AuthenticationFilter.TOKEN_ATTRIBUTE;

import java.io.IOException;
import java.lang.reflect.Method;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import org.radarcns.auth.authorization.Permission;
import org.radarcns.auth.token.RadarToken;

public class PermissionOnProjectFilter implements ContainerRequestFilter {
    @Context
    private ResourceInfo resourceInfo;

    @Context
    private HttpServletRequest request;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        Method method = resourceInfo.getResourceMethod();
        NeedsPermissionOnProject annotation = method.getAnnotation(NeedsPermissionOnProject.class);
        RadarToken token = (RadarToken) request.getAttribute(TOKEN_ATTRIBUTE);
        Permission permission = new Permission(annotation.entity(), annotation.operation());
        UriInfo uriInfo = requestContext.getUriInfo();
        String projectName = uriInfo.getPathParameters().getFirst(annotation.projectParam());

        if (!token.hasPermissionOnProject(permission, projectName)) {
            abortWithForbidden(requestContext,
                    "No permission " + permission + " on project " + projectName + '.');
        }
    }
}
