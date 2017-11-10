package org.radarcns.security.filter;

import org.apache.http.HttpHeaders;
import org.radarcns.auth.authentication.TokenValidator;
import org.radarcns.auth.config.ServerConfig;
import org.radarcns.auth.config.YamlServerConfig;
import org.radarcns.auth.exception.TokenValidationException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

/**
 * Created by dverbeec on 27/09/2017. Added to Rest-Api by yatharthranjan on 10/11/2017
 */
public class AuthenticationFilter implements Filter {
    private ServletContext context;

    private static SoftReference<TokenValidator> validator = new SoftReference<>(null);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.context = filterConfig.getServletContext();
        this.context.log("Authentication filter initialized");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String token = getToken(request);
        HttpServletResponse res = (HttpServletResponse) response;
        if (token == null) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.setHeader("WWW-Authenticate", "Bearer");
            return;
        }

        try {
            request.setAttribute("jwt", getValidator(context).validateAccessToken(token));
            chain.doFilter(request, response);
        } catch (TokenValidationException ex) {
            context.log(ex.getMessage(), ex);
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.setHeader("WWW-Authenticate", "Bearer");
        }
    }

    private static synchronized TokenValidator getValidator(ServletContext context) {
        TokenValidator localValidator = validator.get();
        if (localValidator == null) {
            ServerConfig config = null;
            String mpUrlString = context.getInitParameter("managementPortalUrl");
            if (mpUrlString != null) {
                try {
                    YamlServerConfig cfg = new YamlServerConfig();
                    cfg.setPublicKeyEndpoint(new URI(mpUrlString + "/oauth/token_key"));
                    config = cfg;
                } catch (URISyntaxException e) {
                    context.log("Failed to load Management Portal URL " + mpUrlString, e);
                }
            }

            localValidator = config == null ? new TokenValidator() : new TokenValidator(config);
            validator = new SoftReference<>(localValidator);
        }
        return localValidator;
    }

    @Override
    public void destroy() {
        // nothing to destroy
    }

    private String getToken(ServletRequest request) {
        HttpServletRequest req = (HttpServletRequest) request;
        String authorizationHeader = req.getHeader(HttpHeaders.AUTHORIZATION);

        // Check if the HTTP Authorization header is present and formatted correctly
        if (authorizationHeader == null
                || !authorizationHeader.toLowerCase(Locale.US).startsWith("bearer ")) {
            this.context.log("No authorization header provided in the request");
            return null;
        }

        // Extract the token from the HTTP Authorization header
        return authorizationHeader.substring("Bearer".length()).trim();
    }
}