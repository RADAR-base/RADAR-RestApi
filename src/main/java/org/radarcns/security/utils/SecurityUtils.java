package org.radarcns.security.utils;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.radarcns.security.exception.AccessDeniedException;
import org.radarcns.security.filter.AuthenticationFilter;

import javax.servlet.ServletRequest;

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
     * @param request servlet request
     * @return decoded JWT
     * @throws AccessDeniedException if the {@code "jwt"} attribute is missing or does not contain a
     *                               decoded JWT
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

    public static ObjectNode getJsonError(String message, Exception exc) {
        ObjectNode root = mapper.createObjectNode();

        TextNode msg = root.textNode(message);
        TextNode error = root.textNode(exc.getMessage());

        root.set("message", msg);
        root.set("error",error);

        return root;
    }
}
