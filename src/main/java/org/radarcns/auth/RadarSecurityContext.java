package org.radarcns.auth;

import java.security.Principal;
import java.util.Collections;
import javax.ws.rs.core.SecurityContext;
import org.radarcns.auth.token.RadarToken;

/**
 * Security context from a {@link RadarToken}.
 */
public class RadarSecurityContext implements SecurityContext {

    private final RadarToken token;

    /**
     * Constructs a SecurityContext from a RadarToken.
     */
    public RadarSecurityContext(RadarToken token) {
        this.token = token;
    }

    @Override
    public Principal getUserPrincipal() {
        return token::getSubject;
    }

    /**
     * Maps roles in the shape {@code "project:role"} to a Management Portal role. Global roles take
     * the shape of {@code ":global_role"}. This allows for example a {@code
     *
     * @param role role to be mapped
     * @return {@code true} if the RadarToken contains given project/role, {@code false} otherwise
     * @RolesAllowed(":SYS_ADMIN")} annotation to resolve correctly.
     */
    @Override
    public boolean isUserInRole(String role) {
        String[] projectRole = role.split(":");
        return projectRole.length == 2 && token.getRoles()
                .getOrDefault(projectRole[0], Collections.emptyList())
                .contains(projectRole[1]);
    }

    @Override
    public boolean isSecure() {
        return true;
    }

    @Override
    public String getAuthenticationScheme() {
        return "JWT";
    }

    /**
     * Get the RadarToken parsed from the bearer token.
     */
    public RadarToken getToken() {
        return token;
    }
}
