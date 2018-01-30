package org.radarcns.webapp.inject;

import javax.inject.Singleton;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.radarcns.listener.managementportal.ManagementPortalClient;
import org.radarcns.listener.managementportal.ManagementPortalClientFactory;

@Provider
public class ManagementPortalClientFeature implements Feature {
    @Override
    public boolean configure(FeatureContext context) {
        context.register(ManagementPortalClientBinder.class);
        return true;
    }

    public static class ManagementPortalClientBinder extends AbstractBinder {
        @Override
        protected void configure() {
            bindFactory(ManagementPortalClientFactory.class)
                    .to(ManagementPortalClient.class)
                    .in(Singleton.class);
        }
    }
}
