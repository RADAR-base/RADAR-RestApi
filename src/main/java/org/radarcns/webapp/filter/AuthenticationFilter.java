package org.radarcns.webapp.filter;

import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import javax.annotation.Priority;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import org.radarcns.auth.authentication.TokenValidator;
import org.radarcns.auth.config.ServerConfig;
import org.radarcns.auth.config.YamlServerConfig;
import org.radarcns.auth.exception.TokenValidationException;
import org.radarcns.config.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
@Priority(1000)
public class AuthenticationFilter implements ContainerRequestFilter {
    public static final String TOKEN_ATTRIBUTE = "radarToken";

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);
    private static TokenValidator validator;

    @Context
    private HttpServletRequest request;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String token = getToken();
        if (token == null) {
            logger.warn("[401] {}: No token header provided in the request",
                    requestContext.getUriInfo().getPath());
            requestContext.abortWith(
                    Response.status(HTTP_UNAUTHORIZED)
                            .header("WWW-Authenticate", "Bearer")
                            .build());
            return;
        }

        try {
            request.setAttribute(TOKEN_ATTRIBUTE, getValidator().validateAccessToken(token));
        } catch (TokenValidationException ex) {
            logger.warn("[401] {}: {}", requestContext.getUriInfo().getPath(), ex.getMessage());
            requestContext.abortWith(
                    Response.status(HTTP_UNAUTHORIZED)
                            .header("WWW-Authenticate", "Bearer")
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

    private String getToken() {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        // Check if the HTTP Authorization header is present and formatted correctly
        if (authorizationHeader == null
                || !authorizationHeader.toLowerCase(Locale.US).startsWith("bearer ")) {
            logger.error("No authorization header provided in the request");
            return null;
        }

        // Extract the token from the HTTP Authorization header
        return authorizationHeader.substring("Bearer".length()).trim();
    }
}
