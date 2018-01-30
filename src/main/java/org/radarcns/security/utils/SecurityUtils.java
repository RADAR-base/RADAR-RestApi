package org.radarcns.security.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;
import javax.servlet.ServletRequest;
import org.radarcns.auth.exception.NotAuthorizedException;
import org.radarcns.auth.token.RadarToken;
import org.radarcns.security.filter.AuthenticationFilter;
import org.radarcns.util.RadarConverter;
import org.radarcns.webapp.exception.StatusMessage;

/**
 * Utility class for Rest-API Security.
 */
public final class SecurityUtils {
    private static final ObjectWriter statusWriter = RadarConverter.writerFor(StatusMessage.class);

    /**
     * Parse the {@code "jwt"} attribute from given request.
     *
     * @param request servlet request
     * @return decoded JWT
     * @throws NotAuthorizedException if the "jwt" attribute does not contain a valid decoded JWT
     *
     */
    public static RadarToken getRadarToken(ServletRequest request) throws NotAuthorizedException {
        Object jwt = request.getAttribute(AuthenticationFilter.TOKEN_ATTRIBUTE);
        if (jwt == null) {
            // should not happen, the AuthenticationFilter would throw an exception first if it
            // can not decode the authorization header into a valid JWT
            throw new NotAuthorizedException("No token was found in the request context.");
        }
        if (!(jwt instanceof RadarToken)) {
            // should not happen, the AuthenticationFilter will only set a RadarToken object
            throw new NotAuthorizedException("Expected token to be of type RadarToken but was "
                    + jwt.getClass().getName());
        }
        return (RadarToken) jwt;
    }

    /**
     * Gets json object of given exception details.
     * @param message exception message
     * @return jsonNode created
     */
    public static String getJsonError(String error, String message) {
        StatusMessage statusMessage = new StatusMessage(error, message);
        try {
            return statusWriter.writeValueAsString(statusMessage);
        } catch (JsonProcessingException e) {
            return "{\"error\": \"server_error\"}";
        }
    }
}
