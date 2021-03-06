package org.radarcns.webapp.filter;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Locale;
import javax.annotation.Priority;
import javax.inject.Singleton;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;

import org.radarcns.auth.RadarSecurityContext;
import org.radarcns.auth.authentication.TokenValidator;
import org.radarcns.auth.config.YamlServerConfig;
import org.radarcns.auth.exception.TokenValidationException;
import org.radarcns.auth.token.RadarToken;
import org.radarcns.config.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Checks the presence and validity of a Management Portal JWT. Will return a 401 HTTP status code
 * otherwise.
 */
@Provider
@Authenticated
@Singleton
@Priority(1000)
public class AuthenticationFilter implements ContainerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);
    public static final String REALM = "realm";
    public static final String REALM_VALUE = "api";
    public static final String ERROR = "error";
    public static final String ERROR_DESCRIPTION = "error_description";
    private TokenValidator validator;

    /** Constructs a filter with a fixed validator. */
    public AuthenticationFilter() {

        try {
            validator = new TokenValidator();
            logger.debug("Failed to create default TokenValidator");
        } catch (RuntimeException ex) {
            String mpUrlString =
                    Properties.getApiConfig().getManagementPortalConfig().getManagementPortalUrl()
                            .toString();
            if (mpUrlString != null) {
                try {
                    YamlServerConfig cfg = new YamlServerConfig();
                    cfg.setResourceName("res_RestApi");
                    cfg.setPublicKeyEndpoints(
                            Collections.singletonList(new URI(mpUrlString + "oauth/token_key")));
                    validator = new TokenValidator(cfg);
                } catch (URISyntaxException exc) {
                    logger.error("Failed to load Management Portal URL " + mpUrlString, exc);
                }
            }
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String token = extractBearerToken(requestContext);
        if (token == null) {
            logger.warn("[401] {}: No token bearer header provided in the request",
                    requestContext.getUriInfo().getPath());
            requestContext.abortWith(
                    Response.status(Status.UNAUTHORIZED)
                            .header("WWW-Authenticate", "Bearer "
                                    + REALM + "=" + REALM_VALUE)
                            .build());
            return;
        }

        try {
            RadarToken radarToken = validator.validateAccessToken(token);
            requestContext.setSecurityContext(new RadarSecurityContext(radarToken));
        } catch (TokenValidationException ex) {
            logger.warn("[401] {}: {}", requestContext.getUriInfo().getPath(), ex.getMessage());
            requestContext.abortWith(
                    Response.status(Status.UNAUTHORIZED)
                            .header("WWW-Authenticate", "Bearer "
                                    + REALM + "=" + REALM_VALUE + ", "
                                    + ERROR + "=invalid_token" + ", "
                                    + ERROR_DESCRIPTION + "=" + ex.getMessage())
                            .build());
        }
    }

    private String extractBearerToken(ContainerRequestContext requestContext) {
        String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        // Check if the HTTP Authorization header is present and formatted correctly
        if (authorizationHeader == null
                || !authorizationHeader.toLowerCase(Locale.US).startsWith("bearer ")) {
            logger.error("No authorization bearer header provided in the request");
            return null;
        }

        // Extract the token from the HTTP Authorization header
        return authorizationHeader.substring("Bearer".length()).trim();
    }

    /**
     * Get the token from a request context.
     *
     * @throws IllegalStateException if the method or path was not annotated with {@link
     * Authenticated}.
     */
    public static RadarToken getToken(ContainerRequestContext context) {
        SecurityContext secContext = context.getSecurityContext();
        if (secContext instanceof RadarSecurityContext) {
            return ((RadarSecurityContext) context.getSecurityContext()).getToken();
        } else {
            throw new IllegalStateException(
                    "Permission requested but no authentication performed yet.");
        }
    }
}
