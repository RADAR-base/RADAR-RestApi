package org.radarcns.webapp;

import com.mongodb.MongoClient;
import javax.inject.Singleton;
import okhttp3.OkHttpClient;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.radarcns.catalog.SourceCatalog;
import org.radarcns.dao.SensorDataAccessObject;
import org.radarcns.listener.HttpClientFactory;
import org.radarcns.listener.MongoFactory;
import org.radarcns.listener.managementportal.ManagementPortalClient;
import org.radarcns.listener.managementportal.ManagementPortalClientFactory;
import org.radarcns.service.SourceMonitorService;
import org.radarcns.service.SourceService;
import org.radarcns.service.SubjectService;
import org.radarcns.webapp.filter.AuthenticationFilter;
import org.radarcns.webapp.filter.AuthorizationFeature;

/**
 * Radar application configuration.
 *
 * <p>Replaces previous {@code web.xml}.
 */
public class RadarApplication extends ResourceConfig {

    /** Radar application configuration. */
    public RadarApplication() {
        packages(
                "io.swagger.v3.jaxrs2.integration.resources",
                "org.radarcns.webapp.resource",
                "org.radarcns.webapp.exception",
                "org.radarcns.webapp.media");

        register(AuthenticationFilter.class);
        register(AuthorizationFeature.class);

        register(new AbstractBinder() {
            @Override
            protected void configure() {
                bindFactory(HttpClientFactory.class)
                        .to(OkHttpClient.class)
                        .in(Singleton.class);

                bindFactory(ManagementPortalClientFactory.class, Singleton.class)
                        .to(ManagementPortalClient.class);

                bindFactory(MongoFactory.class)
                        .to(MongoClient.class)
                        .in(Singleton.class);

                bind(SourceCatalog.class)
                        .to(SourceCatalog.class)
                        .in(Singleton.class);

                bind(SensorDataAccessObject.class)
                        .to(SensorDataAccessObject.class)
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
            }
        });
    }
}
