package org.radarcns.webapp.exception;

import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
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
        logger.warn("[404] {}", uri.getPath());

        Response.ResponseBuilder builder = Response.status(HTTP_NOT_FOUND);
        if (headers.getMediaType().isCompatible(APPLICATION_JSON_TYPE)) {
            builder.entity(new StatusMessage("not_found",
                    exception.getMessage()));
        }
        return builder.build();
    }
}
