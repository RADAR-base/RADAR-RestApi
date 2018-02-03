package org.radarcns.webapp.exception;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.radarcns.webapp.util.ResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class UncaughtExceptionMapper extends Throwable implements ExceptionMapper<Throwable> {
    private static final Logger logger = LoggerFactory.getLogger(UncaughtExceptionMapper.class);

    @Context
    private UriInfo uri;

    @Context
    private HttpHeaders headers;

    @Override
    public Response toResponse(Throwable exception) {
        logger.error("[500] {}", uri.getAbsolutePath(), exception);

        return ResponseHandler.jsonStatus(headers.getMediaType(), Status.INTERNAL_SERVER_ERROR,
                "server_error", exception.getClass() + ": " + exception.getMessage() + ".")
                .build();
    }
}
