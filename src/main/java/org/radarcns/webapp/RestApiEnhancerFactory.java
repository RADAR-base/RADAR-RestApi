package org.radarcns.webapp;

import java.util.Arrays;
import java.util.List;
import javax.inject.Singleton;
import okhttp3.OkHttpClient;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.jetbrains.annotations.NotNull;
import org.radarbase.jersey.auth.AuthConfig;
import org.radarbase.jersey.config.EnhancerFactory;
import org.radarbase.jersey.config.GeneralExceptionResourceEnhancer;
import org.radarbase.jersey.config.HttpExceptionResourceEnhancer;
import org.radarbase.jersey.config.JerseyResourceEnhancer;
import org.radarbase.jersey.config.ManagementPortalResourceEnhancer;
import org.radarbase.jersey.config.RadarJerseyResourceEnhancer;
import org.radarcns.catalog.SourceCatalog;
import org.radarcns.config.ApplicationConfig;
import org.radarcns.listener.HttpClientFactory;
import org.radarcns.listener.MongoFactory;
import org.radarcns.listener.managementportal.ManagementPortalClient;
import org.radarcns.mongo.util.MongoWrapper;
import org.radarcns.service.DataSetService;
import org.radarcns.service.SourceMonitorService;
import org.radarcns.service.SourceService;
import org.radarcns.service.SourceStatusMonitorService;
import org.radarcns.service.SubjectService;
import org.radarcns.webapp.param.TimeScaleParser;

public class RestApiEnhancerFactory implements EnhancerFactory {

  private final ApplicationConfig config;

  public RestApiEnhancerFactory(ApplicationConfig config) {
    this.config = config;
  }

  @NotNull
  @Override
  public List<JerseyResourceEnhancer> createEnhancers() {
    return Arrays.asList(
      new RestApiEnhancer(),
        new RadarJerseyResourceEnhancer(new AuthConfig(
            config.getManagementPortalConfig().getManagementPortalUrl().toString(),
            "res_RestApi",
            null,
            null,
            null,
            null,
            null,
            null
        )),
        new ManagementPortalResourceEnhancer(),
        new HttpExceptionResourceEnhancer(),
        new GeneralExceptionResourceEnhancer());
  }

  class RestApiEnhancer implements JerseyResourceEnhancer {
    @Override
    public void enhanceBinder(@NotNull AbstractBinder abstractBinder) {
      abstractBinder.bind(config)
          .to(ApplicationConfig.class);

      abstractBinder.bind(new TimeScaleParser())
          .to(TimeScaleParser.class);

      abstractBinder.bindFactory(HttpClientFactory.class)
          .to(OkHttpClient.class)
          .in(Singleton.class);

      abstractBinder.bind(ManagementPortalClient.class)
          .to(ManagementPortalClient.class)
          .in(Singleton.class);

      abstractBinder.bindFactory(MongoFactory.class)
          .to(MongoWrapper.class)
          .in(Singleton.class);

      abstractBinder.bind(SourceCatalog.class)
          .to(SourceCatalog.class)
          .in(Singleton.class);

      abstractBinder.bind(SourceMonitorService.class)
          .to(SourceMonitorService.class)
          .in(Singleton.class);

      abstractBinder.bind(SubjectService.class)
          .to(SubjectService.class)
          .in(Singleton.class);

      abstractBinder.bind(SourceService.class)
          .to(SourceService.class)
          .in(Singleton.class);

      abstractBinder.bind(DataSetService.class)
          .to(DataSetService.class)
          .in(Singleton.class);

      abstractBinder.bind(SourceStatusMonitorService.class)
          .to(SourceStatusMonitorService.class)
          .in(Singleton.class);
    }

    @Override
    public void enhanceResources(@NotNull ResourceConfig resourceConfig) {
      resourceConfig.packages(
          "io.swagger.v3.jaxrs2.integration.resources",
          "org.radarcns.webapp.resource",
          "org.radarcns.webapp.media");
    }
  }
}
