package org.radarcns.auth;

import static org.radarcns.auth.PermissionFilter.abortWithForbidden;

import java.io.IOException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import org.radarcns.auth.authorization.Permission;
import org.radarcns.auth.token.RadarToken;

/**
 * Check that the token has access to given project.
 */
public class PermissionOnProjectFilter implements ContainerRequestFilter {
    @Context
    private ResourceInfo resourceInfo;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        NeedsPermissionOnProject annotation = resourceInfo.getResourceMethod()
                .getAnnotation(NeedsPermissionOnProject.class);
        Permission permission = new Permission(annotation.entity(), annotation.operation());
        UriInfo uriInfo = requestContext.getUriInfo();
        String projectName = uriInfo.getPathParameters().getFirst(annotation.projectParam());

        RadarToken token = ((RadarSecurityContext) requestContext.getSecurityContext()).getToken();
        if (!token.hasPermissionOnProject(permission, projectName)) {
            abortWithForbidden(requestContext,
                    "No permission " + permission + " on project " + projectName + '.');
        }
    }
}
