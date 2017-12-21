package org.radarcns.webapp.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class UncaughtExceptionMapper extends Throwable implements ExceptionMapper<Throwable> {
    private static final Logger logger = LoggerFactory.getLogger(UncaughtExceptionMapper.class);

    @Override
    public Response toResponse(Throwable exception) {
        logger.error("Uncaught exception", exception);
        return Response.status(500)
                .entity(new StatusMessage("server_error",
                        exception.getClass() + ": "+ exception.getMessage()))
                .build();
    }
}
