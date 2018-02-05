package org.radarcns.webapp.exception;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles uncaught exceptions with HTTP status code 500, a JSON error body and a log statement.
 */
@Provider
public class UncaughtExceptionMapper extends Throwable implements ExceptionMapper<Throwable> {
    public static final String APPLICATION_JSON_UTF8 = "application/json; charset=utf-8";
    private static final Logger logger = LoggerFactory.getLogger(UncaughtExceptionMapper.class);

    @Context
    private UriInfo uri;

    @Context
    private HttpHeaders headers;

    @Override
    public Response toResponse(Throwable exception) {
        logger.error("[500] {}", uri.getAbsolutePath(), exception);

        return jsonStatus(headers.getMediaType(), Status.INTERNAL_SERVER_ERROR,
                "server_error", exception.getClass() + ": " + exception.getMessage() + ".")
                .build();
    }


    /**
     * Constructs a response builder with given properties. If the response type can be compatible
     * with application/json, a JSON body response will be added.
     * @param type expected media type
     * @param status HTTP status code
     * @param error identifiable error string
     * @param message error description.
     * @return response builder, ready to be built.
     */
    public static ResponseBuilder jsonStatus(
            MediaType type, Status status, String error, String message) {
        ResponseBuilder builder = Response.status(status);
        if (type == null || type.isCompatible(MediaType.APPLICATION_JSON_TYPE)) {
            builder.header("Content-Type", APPLICATION_JSON_UTF8)
                    .entity(new StatusMessage(error, message));
        }
        return builder;
    }
}
