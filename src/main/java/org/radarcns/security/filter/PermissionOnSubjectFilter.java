package org.radarcns.security.filter;

import static org.radarcns.security.filter.PermissionFilter.abortWithForbidden;

import java.io.IOException;
import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import org.radarcns.auth.authorization.Permission;
import org.radarcns.auth.token.RadarToken;
import org.radarcns.listener.managementportal.ManagementPortalClient;
import org.radarcns.managementportal.Subject;
import org.radarcns.webapp.filter.AuthenticationFilter.RadarSecurityContext;

public class PermissionOnSubjectFilter implements ContainerRequestFilter {
    @Context
    private ResourceInfo resourceInfo;

    @Inject
    private ManagementPortalClient mpClient;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        NeedsPermissionOnSubject annotation = resourceInfo.getResourceMethod()
                .getAnnotation(NeedsPermissionOnSubject.class);
        Permission permission = new Permission(annotation.entity(), annotation.operation());

        MultivaluedMap<String, String> pathParams = requestContext.getUriInfo().getPathParameters();

        String subjectId = pathParams.getFirst(annotation.subjectParam());
        String projectName = pathParams.getFirst(annotation.projectParam());

        RadarToken token = ((RadarSecurityContext) requestContext.getSecurityContext()).getToken();
        if (!token.hasPermissionOnSubject(permission, projectName, subjectId)) {
            abortWithForbidden(requestContext, "No permission " + permission
                    + " on subject " + subjectId + " in project " + projectName);
        }

        Subject subject = mpClient.getSubject(subjectId);
        if (!projectName.equals(subject.getProject().getProjectName())) {
            throw new NotFoundException(
                    "Subject " + subjectId + " is not part of project " + projectName + ".");
        }
    }
}
