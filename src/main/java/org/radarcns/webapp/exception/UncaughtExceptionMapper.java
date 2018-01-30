package org.radarcns.webapp.exception;

import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
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
public class UncaughtExceptionMapper extends Throwable implements ExceptionMapper<Throwable> {
    private static final Logger logger = LoggerFactory.getLogger(UncaughtExceptionMapper.class);

    @Context
    private HttpServletRequest request;

    @Override
    @Produces(APPLICATION_JSON)
    public Response toResponse(Throwable exception) {
        logger.error("[500] {}", request.getRequestURI(), exception);
        return Response.status(HTTP_INTERNAL_ERROR)
                .entity(new StatusMessage("server_error",
                        exception.getClass() + ": " + exception.getMessage()))
                .build();
    }
}
