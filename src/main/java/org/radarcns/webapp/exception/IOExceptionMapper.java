package org.radarcns.webapp.exception;

import java.io.IOException;
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

        return ResponseHandler.jsonStatus(headers.getMediaType(), Status.INTERNAL_SERVER_ERROR,
                "temporary_server_error",
                exception.getClass() + ": " + exception.getMessage() + ".")
                .build();
    }
}
