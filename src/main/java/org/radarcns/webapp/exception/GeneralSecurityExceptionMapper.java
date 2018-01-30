package org.radarcns.webapp.exception;

import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;

import java.security.GeneralSecurityException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class GeneralSecurityExceptionMapper extends GeneralSecurityException implements
        ExceptionMapper<GeneralSecurityException> {
    private static final Logger logger = LoggerFactory
            .getLogger(GeneralSecurityExceptionMapper.class);

    @Context
    private HttpServletRequest request;

    @Override
    public Response toResponse(GeneralSecurityException exception) {
        logger.warn("[401] {}", request.getRequestURI());
        return Response.status(HTTP_UNAUTHORIZED)
                .header("WWW-Authenticate", "Bearer")
                .build();
    }
}
