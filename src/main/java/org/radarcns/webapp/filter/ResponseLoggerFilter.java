package org.radarcns.webapp.filter;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class ResponseLoggerFilter implements ContainerResponseFilter {
    private static final Logger logger = LoggerFactory.getLogger(ResponseLoggerFilter.class);

    @Override
    public void filter(ContainerRequestContext requestContext,
            ContainerResponseContext responseContext) {
        if (responseContext.getStatus() < 400) {
            logger.debug("[{}] {} - {}", responseContext.getStatus(),
                    requestContext.getUriInfo().getAbsolutePath(), responseContext.getLength());
        }
    }
}
