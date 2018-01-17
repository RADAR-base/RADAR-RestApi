package org.radarcns.security.utils;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import javax.servlet.ServletRequest;
import org.radarcns.security.exception.AccessDeniedException;
import org.radarcns.security.filter.AuthenticationFilter;

/**
 * Utility class for Rest-API Security.
 */
public final class SecurityUtils {

    private static final ObjectMapper mapper;

    static {
        mapper = new ObjectMapper();
    }


    /**
     * Parse the {@code "jwt"} attribute from given request.
     *
     * @param request servlet request
     * @return decoded JWT
     * @throws AccessDeniedException if the "jwt" attribute does not contain a valid decoded JWT
     *
     */
    public static DecodedJWT getJWT(ServletRequest request) throws AccessDeniedException {
        Object jwt = request.getAttribute(AuthenticationFilter.TOKEN_ATTRIBUTE);
        if (jwt == null) {
            // should not happen, the AuthenticationFilter would throw an exception first if it
            // can not decode the authorization header into a valid JWT
            throw new AccessDeniedException("No token was found in the request context.");
        }
        if (!(jwt instanceof DecodedJWT)) {
            // should not happen, the AuthenticationFilter will only set a DecodedJWT object
            throw new AccessDeniedException("Expected token to be of type DecodedJWT but was "
                    + jwt.getClass().getName());
        }
        return (DecodedJWT) jwt;
    }

    /**
     * Gets json object of given exception details.
     * @param message exception message
     * @param exc exception
     * @return jsonNode created
     */
    public static ObjectNode getJsonError(String message, Exception exc) {
        ObjectNode root = mapper.createObjectNode();

        TextNode msg = root.textNode(message);
        TextNode error = root.textNode(exc.getMessage());
        TextNode errorClass = root.textNode(exc.getClass().getName());

        root.set("message", msg);
        root.set("class", errorClass);
        root.set("error", error);

        return root;
    }
}
