package org.radarcns.webapp.filter;

import static org.radarcns.webapp.exception.UncaughtExceptionMapper.APPLICATION_JSON_UTF8;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import org.radarcns.auth.RadarSecurityContext;
import org.radarcns.auth.authentication.TokenValidator;
import org.radarcns.auth.config.ServerConfig;
import org.radarcns.auth.config.YamlServerConfig;
import org.radarcns.auth.exception.TokenValidationException;
import org.radarcns.auth.token.RadarToken;
import org.radarcns.config.Properties;
import org.radarcns.webapp.exception.StatusMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Checks the presence and validity of a Management Portal JWT. Will return a 401 HTTP status code
 * otherwise.
 */
@Authenticated
@Priority(1000)
public class AuthenticationFilter implements ContainerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);
    private static TokenValidator validator;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String token = extractBearerToken(requestContext);
        if (token == null) {
            logger.warn("[401] {}: No token bearer header provided in the request",
                    requestContext.getUriInfo().getPath());
            requestContext.abortWith(
                    Response.status(Status.UNAUTHORIZED)
                            .header("WWW-Authenticate", "Bearer")
                            .header("Content-Type", APPLICATION_JSON_UTF8)
                            .entity(new StatusMessage("token_missing",
                                    "No bearer token provided in request."))
                            .build());
            return;
        }

        try {
            RadarToken radarToken = getValidator().validateAccessToken(token);
            requestContext.setSecurityContext(new RadarSecurityContext(radarToken));
        } catch (TokenValidationException ex) {
            logger.warn("[401] {}: {}", requestContext.getUriInfo().getPath(), ex.getMessage());
            requestContext.abortWith(
                    Response.status(Status.UNAUTHORIZED)
                            .header("WWW-Authenticate", "Bearer")
                            .header("Content-Type", APPLICATION_JSON_UTF8)
                            .entity(new StatusMessage("token_invalid",
                                    "Token is invalid: " + ex.getMessage()))
                            .build());
        }
    }


    private static synchronized TokenValidator getValidator() {
        if (validator == null) {
            ServerConfig config = null;
            String mpUrlString = Properties.getApiConfig().getManagementPortalConfig()
                    .getManagementPortalUrl().toString();
            if (mpUrlString != null) {
                try {
                    YamlServerConfig cfg = new YamlServerConfig();
                    cfg.setResourceName("res_RestApi");
                    cfg.setPublicKeyEndpoint(new URI(mpUrlString + "oauth/token_key"));
                    config = cfg;
                } catch (URISyntaxException exc) {
                    logger.error("Failed to load Management Portal URL " + mpUrlString, exc);
                }
            }
            validator = config == null ? new TokenValidator() : new TokenValidator(config);
        }
        return validator;
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
