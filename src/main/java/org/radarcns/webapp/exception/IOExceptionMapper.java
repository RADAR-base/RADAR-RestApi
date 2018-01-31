package org.radarcns.webapp.exception;

import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

import java.io.IOException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
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
        logger.error("[500] {} - {}: {}", uri.getPath(),
                exception.getClass(), exception.getMessage());

        Response.ResponseBuilder builder = Response.status(HTTP_INTERNAL_ERROR);
        if (headers.getMediaType().isCompatible(APPLICATION_JSON_TYPE)) {
            builder.entity(new StatusMessage("temporary_server_error",
                    exception.getClass() + ": " + exception.getMessage()));
        }
        return builder.build();
    }
}
