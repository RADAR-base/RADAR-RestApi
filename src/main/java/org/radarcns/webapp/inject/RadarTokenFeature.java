package org.radarcns.webapp.inject;

import javax.servlet.ServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.radarcns.auth.token.RadarToken;
import org.radarcns.security.filter.AuthenticationFilter;

public class RadarTokenFeature implements Feature {
    @Override
    public boolean configure(FeatureContext context) {
        context.register(SourceCatalogFeature.SourceCatalogueBinder.class);
        return true;
    }

    public static class RadarTokenBinder extends AbstractBinder {
        @Override
        protected void configure() {
            bindFactory(RadarTokenFactory.class)
                    .to(RadarToken.class);
        }
    }

    public static class RadarTokenFactory implements Factory<RadarToken> {
        @Context
        private ServletRequest request;

        @Override
        public RadarToken provide() {
            Object jwt = request.getAttribute(AuthenticationFilter.TOKEN_ATTRIBUTE);
            if (jwt == null) {
                // should not happen, the AuthenticationFilter would throw an exception first if it
                // can not decode the authorization header into a valid JWT
                throw new IllegalStateException("No token was found in the request context.");
            }
            if (!(jwt instanceof RadarToken)) {
                // should not happen, the AuthenticationFilter will only set a RadarToken object
                throw new IllegalStateException("Expected token to be of type RadarToken but was "
                        + jwt.getClass().getName());
            }
            return (RadarToken) jwt;
        }

        @Override
        public void dispose(RadarToken instance) {
            // no disposal needed
        }
    }
}
