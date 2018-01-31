package org.radarcns.security.filter;

import static java.net.HttpURLConnection.HTTP_FORBIDDEN;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static org.radarcns.security.filter.PermissionFilter.abortWithForbidden;
import static org.radarcns.webapp.filter.AuthenticationFilter.TOKEN_ATTRIBUTE;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import org.radarcns.auth.authorization.Permission;
import org.radarcns.auth.token.RadarToken;
import org.radarcns.listener.managementportal.ManagementPortalClient;
import org.radarcns.managementportal.Subject;
import org.radarcns.webapp.exception.NotFoundException;
import org.radarcns.webapp.exception.StatusMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PermissionOnSubjectFilter implements ContainerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(PermissionOnSubjectFilter.class);

    @Context
    private ResourceInfo resourceInfo;

    @Context
    private ResourceContext resourceContext;

    @Context
    private HttpServletRequest request;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        NeedsPermissionOnSubject annotation = resourceInfo.getResourceMethod()
                .getAnnotation(NeedsPermissionOnSubject.class);
        Permission permission = new Permission(annotation.entity(), annotation.operation());

        MultivaluedMap<String, String> pathParams = requestContext.getUriInfo().getPathParameters();

        String subjectId = pathParams.getFirst(annotation.subjectParam());

        String projectName;
        String projectParam = annotation.projectParam();
        if (projectParam.equals(NeedsPermissionOnSubject.PROJECT_OF_SUBJECT)) {
            ManagementPortalClient mpClient = resourceContext
                    .getResource(ManagementPortalClient.class);
            try {
                Subject subject = mpClient.getSubject(subjectId);
                projectName = subject.getProject().getProjectName();
            } catch (NotFoundException e) {
                logger.warn("[404] {}: {}", requestContext.getUriInfo().getPath(), e.getMessage());
                Response.ResponseBuilder builder = Response.status(HTTP_FORBIDDEN);
                if (requestContext.getMediaType().isCompatible(APPLICATION_JSON_TYPE)) {
                    builder.entity(new StatusMessage("not_found", e.getMessage()));
                }
                requestContext.abortWith(builder.build());
                return;
            }
        } else {
            projectName = pathParams.getFirst(projectParam);
        }

        RadarToken token = (RadarToken) request.getAttribute(TOKEN_ATTRIBUTE);
        if (!token.hasPermissionOnSubject(permission, projectName, subjectId)) {
            abortWithForbidden(requestContext, "No permission " + permission
                    + " on subject " + subjectId + " in project " + projectName);
        }
    }
}
