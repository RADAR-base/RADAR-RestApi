package org.radarcns.webapp;

import com.mongodb.MongoClient;
import javax.inject.Singleton;
import okhttp3.OkHttpClient;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.radarcns.catalog.SourceCatalog;
import org.radarcns.listener.HttpClientFactory;
import org.radarcns.listener.MongoFactory;
import org.radarcns.listener.managementportal.ManagementPortalClient;
import org.radarcns.service.DataSetService;
import org.radarcns.service.SourceMonitorService;
import org.radarcns.service.SourceService;
import org.radarcns.service.SourceStatusMonitorService;
import org.radarcns.service.SubjectService;
import org.radarcns.webapp.filter.AuthenticationFilter;
import org.radarcns.webapp.filter.AuthorizationFeature;
import org.radarcns.webapp.param.TimeScaleParser;

/**
 * Radar application configuration.
 *
 * <p>Replaces previous {@code web.xml}.
 */
public class RadarApplication extends ResourceConfig {

    /**
     * Radar application configuration.
     */
    public RadarApplication() {
        packages(
                "io.swagger.v3.jaxrs2.integration.resources",
                "org.radarcns.webapp.resource",
                "org.radarcns.webapp.exception",
                "org.radarcns.webapp.media");

        register(new AbstractBinder() {
            // IDEA complains about redundant bind(C.class).to(C.class) bindings, but they are
            // necessary for the injection to work.
            @SuppressWarnings("RedundantToBinding")
            @Override
            protected void configure() {
                bind(new TimeScaleParser())
                        .to(TimeScaleParser.class);

                bindFactory(HttpClientFactory.class)
                        .to(OkHttpClient.class)
                        .in(Singleton.class);

                bind(ManagementPortalClient.class)
                        .to(ManagementPortalClient.class)
                        .in(Singleton.class);

                bindFactory(MongoFactory.class)
                        .to(MongoClient.class)
                        .in(Singleton.class);

                bind(SourceCatalog.class)
                        .to(SourceCatalog.class)
                        .in(Singleton.class);

                bind(SourceMonitorService.class)
                        .to(SourceMonitorService.class)
                        .in(Singleton.class);

                bind(SubjectService.class)
                        .to(SubjectService.class)
                        .in(Singleton.class);

                bind(SourceService.class)
                        .to(SourceService.class)
                        .in(Singleton.class);

                bind(DataSetService.class)
                        .to(DataSetService.class)
                        .in(Singleton.class);

                bind(SourceStatusMonitorService.class)
                        .to(SourceStatusMonitorService.class)
                        .in(Singleton.class);
            }
        });

        register(AuthenticationFilter.class);
        register(AuthorizationFeature.class);
    }
}
