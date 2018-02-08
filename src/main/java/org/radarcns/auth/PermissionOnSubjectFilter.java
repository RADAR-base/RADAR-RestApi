package org.radarcns.auth;

import static org.radarcns.auth.PermissionFilter.abortWithForbidden;

import java.io.IOException;
import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import org.radarcns.auth.authorization.Permission;
import org.radarcns.listener.managementportal.ManagementPortalClient;
import org.radarcns.webapp.filter.AuthenticationFilter;

/**
 * Check that the token has access to given subject in given project.
 */
public class PermissionOnSubjectFilter implements ContainerRequestFilter {
    @Context
    private ResourceInfo resourceInfo;

    @Inject
    private ManagementPortalClient client;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        NeedsPermissionOnSubject annotation = resourceInfo.getResourceMethod()
                .getAnnotation(NeedsPermissionOnSubject.class);
        Permission permission = new Permission(annotation.entity(), annotation.operation());

        MultivaluedMap<String, String> pathParams = requestContext.getUriInfo().getPathParameters();

        String subjectId = pathParams.getFirst(annotation.subjectParam());
        String projectName = pathParams.getFirst(annotation.projectParam());

        if (!AuthenticationFilter.getToken(requestContext)
                .hasPermissionOnSubject(permission, projectName, subjectId)) {
            abortWithForbidden(requestContext, "No permission " + permission
                    + " on subject " + subjectId + " in project " + projectName);
        }

        client.getSubject(subjectId);
    }
}
