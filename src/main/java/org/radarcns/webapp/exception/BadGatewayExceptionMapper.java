package org.radarcns.webapp.exception;

import java.io.IOException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BadGatewayExceptionMapper extends BadGatewayException implements ExceptionMapper<BadGatewayException> {

    private static final Logger logger = LoggerFactory.getLogger(BadGatewayExceptionMapper.class);

    @Context
    private UriInfo uri;

    @Context
    private HttpHeaders headers;

    @Override
    public Response toResponse(BadGatewayException exception) {
        logger.error("[504] {} - {}: {}", uri.getAbsolutePath(),
                exception.getClass(), exception.getMessage());

        return UncaughtExceptionMapper.jsonStatus(headers.getMediaType(),
                Status.BAD_GATEWAY, "bad_gateway",
                exception.getClass() + ": " + exception.getMessage() + ".")
                .build();
    }
}
