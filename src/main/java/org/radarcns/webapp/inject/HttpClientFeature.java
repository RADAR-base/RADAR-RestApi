package org.radarcns.webapp.inject;

import javax.inject.Singleton;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;
import okhttp3.OkHttpClient;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.radarcns.listener.HttpClientFactory;

@Provider
public class HttpClientFeature implements Feature {
    @Override
    public boolean configure(FeatureContext context) {
        context.register(HttpClientBinder.class);
        return true;
    }

    public static class HttpClientBinder extends AbstractBinder {
        @Override
        protected void configure() {
            bindFactory(HttpClientFactory.class)
                    .to(OkHttpClient.class)
                    .in(Singleton.class);
        }
    }
}
