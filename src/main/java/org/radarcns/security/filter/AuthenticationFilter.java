package org.radarcns.security.filter;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.radarcns.auth.authentication.TokenValidator;
import org.radarcns.auth.config.ServerConfig;
import org.radarcns.auth.config.YamlServerConfig;
import org.radarcns.auth.exception.TokenValidationException;
import org.radarcns.config.managementportal.config.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by dverbeec on 27/09/2017. Updated in Rest-Api by yatharthranjan on 10/11/2017.
 */
public class AuthenticationFilter implements Filter {

    private static Logger log = LoggerFactory.getLogger(AuthenticationFilter.class);
    public static final String TOKEN_ATTRIBUTE = "jwt";

    private static SoftReference<TokenValidator> validator = new SoftReference<>(null);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        //this.context = filterConfig.getServletContext();
        log.info("Authentication filter initialized");
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
            request.setAttribute(TOKEN_ATTRIBUTE, getValidator().validateAccessToken(token));
            chain.doFilter(request, response);
        } catch (TokenValidationException ex) {
            log.error(ex.getMessage(), ex);
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.setHeader("WWW-Authenticate", "Bearer");
            res.setHeader("Error", "Invalid Token!");
        }
    }

    private static synchronized TokenValidator getValidator() {
        TokenValidator localValidator = validator.get();
        if (localValidator == null) {
            ServerConfig config = null;
            String mpUrlString = Properties.validateMpUrl().toString();
            if (mpUrlString != null) {
                try {
                    YamlServerConfig cfg = new YamlServerConfig();
                    cfg.setResourceName("res_RestApi");
                    cfg.setPublicKeyEndpoint(new URI(mpUrlString + "oauth/token_key"));
                    config = cfg;
                } catch (URISyntaxException exc) {
                    log.error("Failed to load Management Portal URL " + mpUrlString, exc);
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
        String authorizationHeader = req.getHeader("Authorization");

        // Check if the HTTP Authorization header is present and formatted correctly
        if (authorizationHeader == null
                || !authorizationHeader.toLowerCase(Locale.US).startsWith("bearer ")) {
            log.error("No authorization header provided in the request");
            return null;
        }

        // Extract the token from the HTTP Authorization header
        return authorizationHeader.substring("Bearer".length()).trim();
    }
}