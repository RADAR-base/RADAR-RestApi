package org.radarcns.webapp.exception;

import javax.ws.rs.NotFoundException;
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
public class NotFoundExceptionMapper extends NotFoundException
        implements ExceptionMapper<NotFoundException> {
    private static final Logger logger = LoggerFactory.getLogger(NotFoundExceptionMapper.class);

    @Context
    private UriInfo uri;

    @Context
    private HttpHeaders headers;

    @Override
    public Response toResponse(NotFoundException exception) {
        logger.warn("[404] {}", uri.getAbsolutePath());

        return ResponseHandler.jsonStatus(headers.getMediaType(), Status.NOT_FOUND,
                "not_found", exception.getMessage() + ".")
                .build();
    }
}
