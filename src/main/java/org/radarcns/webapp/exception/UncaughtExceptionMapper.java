package org.radarcns.webapp.exception;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class UncaughtExceptionMapper extends Throwable implements ExceptionMapper<Throwable> {
    private static final Logger logger = LoggerFactory.getLogger(UncaughtExceptionMapper.class);

    @Override
    @Produces(APPLICATION_JSON)
    public Response toResponse(Throwable exception) {
        logger.error("Uncaught exception", exception);
        return Response.status(500)
                .header("Content-Type", APPLICATION_JSON + "; charset=utf-8")
                .entity(new StatusMessage("server_error",
                        exception.getClass() + ": " + exception.getMessage()))
                .build();
    }
}
