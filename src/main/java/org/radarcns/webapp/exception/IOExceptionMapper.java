package org.radarcns.webapp.exception;

import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.io.IOException;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class IOExceptionMapper extends IOException implements ExceptionMapper<IOException> {
    private static final Logger logger = LoggerFactory.getLogger(UncaughtExceptionMapper.class);

    @Context
    private UriInfo uri;

    @Override
    @Produces(APPLICATION_JSON)
    public Response toResponse(IOException exception) {
        logger.error("[500] {} - {}: {}", uri.getPath(),
                exception.getClass(), exception.getMessage());

        return Response.status(HTTP_INTERNAL_ERROR)
                .entity(new StatusMessage("temporary_server_error",
                        exception.getClass() + ": " + exception.getMessage()))
                .build();
    }
}
