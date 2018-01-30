package org.radarcns.webapp.inject;

import javax.inject.Singleton;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.radarcns.source.SourceCatalog;

@Provider
public class SourceCatalogFeature implements Feature {
    @Override
    public boolean configure(FeatureContext context) {
        context.register(SourceCatalogueBinder.class);
        return true;
    }

    public static class SourceCatalogueBinder extends AbstractBinder {
        @Override
        protected void configure() {
            bind(SourceCatalog.class)
                    .in(Singleton.class);
        }
    }
}
