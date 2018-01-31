package org.radarcns.webapp.inject;

import java.util.Objects;
import javax.servlet.ServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.radarcns.auth.token.RadarToken;
import org.radarcns.webapp.filter.AuthenticationFilter;

@Provider
public class RadarTokenFeature implements Feature {
    @Override
    public boolean configure(FeatureContext context) {
        context.register(RadarTokenBinder.class);
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
            return (RadarToken) Objects.requireNonNull(
                    request.getAttribute(AuthenticationFilter.TOKEN_ATTRIBUTE));
        }

        @Override
        public void dispose(RadarToken instance) {
            // no disposal needed
        }
    }
}
