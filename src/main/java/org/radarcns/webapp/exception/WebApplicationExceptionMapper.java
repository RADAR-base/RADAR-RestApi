package org.radarcns.webapp.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class WebApplicationExceptionMapper extends javax.ws.rs.WebApplicationException implements ExceptionMapper<javax.ws.rs.WebApplicationException> {
    private static final Logger logger = LoggerFactory.getLogger(WebApplicationExceptionMapper.class);

    @Override
    public Response toResponse(javax.ws.rs.WebApplicationException exception) {
        logger.error("Web application error", exception);
        return exception.getResponse();
    }
}
