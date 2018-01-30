package org.radarcns.webapp.exception;

import static java.net.HttpURLConnection.HTTP_FORBIDDEN;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import org.radarcns.auth.exception.NotAuthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotAuthorizedExceptionMapper extends NotAuthorizedException implements
        ExceptionMapper<NotAuthorizedException> {
    private static final Logger logger = LoggerFactory.getLogger(NotFoundExceptionMapper.class);

    @Context
    private HttpServletRequest request;

    @Override
    @Produces(APPLICATION_JSON)
    public Response toResponse(NotAuthorizedException exception) {
        logger.warn("[403] {}", request.getRequestURI());
        return Response.status(HTTP_FORBIDDEN)
                .entity(new StatusMessage("forbidden", exception.getMessage()))
                .build();
    }
}
