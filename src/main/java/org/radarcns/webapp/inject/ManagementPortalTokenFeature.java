package org.radarcns.webapp.inject;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.radarcns.listener.managementportal.ManagementPortalTokenFactory;
import org.radarcns.oauth.OAuth2AccessTokenDetails;

@Provider
public class ManagementPortalTokenFeature implements Feature {
    @Override
    public boolean configure(FeatureContext context) {
        context.register(ManagementPortalTokenBinder.class);
        return true;
    }

    public static class ManagementPortalTokenBinder extends AbstractBinder {
        @Override
        protected void configure() {
            bindFactory(ManagementPortalTokenFactory.class)
                    .to(OAuth2AccessTokenDetails.class)
                    .proxy(true)
                    .proxyForSameScope(false);
        }
    }
}
