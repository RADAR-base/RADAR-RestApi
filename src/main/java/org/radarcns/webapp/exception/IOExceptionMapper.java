package org.radarcns.webapp.exception;

import java.io.IOException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles IOException with HTTP status code 500, a JSON error body and a log statement.
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
@Provider
public class IOExceptionMapper extends IOException implements ExceptionMapper<IOException> {
    private static final Logger logger = LoggerFactory.getLogger(UncaughtExceptionMapper.class);

    @Context
    private UriInfo uri;

    @Context
    private HttpHeaders headers;

    @Override
    public Response toResponse(IOException exception) {
        logger.error("[500] {} - {}: {}", uri.getAbsolutePath(),
                exception.getClass(), exception.getMessage());

        return UncaughtExceptionMapper.jsonStatus(headers.getMediaType(), Status.INTERNAL_SERVER_ERROR,
                "temporary_server_error",
                exception.getClass() + ": " + exception.getMessage() + ".")
                .build();
    }
}
