package org.radarcns.webapp.exception;

import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class NotFoundExceptionMapper extends NotFoundException
        implements ExceptionMapper<NotFoundException> {
    private static final Logger logger = LoggerFactory.getLogger(NotFoundExceptionMapper.class);

    @Context
    private HttpServletRequest request;

    @Override
    @Produces(APPLICATION_JSON)
    public Response toResponse(NotFoundException exception) {
        logger.warn("[404] {}", request.getRequestURI());
        return Response.status(HTTP_NOT_FOUND)
                .entity(new StatusMessage("not_found", exception.getMessage()))
                .build();
    }
}
