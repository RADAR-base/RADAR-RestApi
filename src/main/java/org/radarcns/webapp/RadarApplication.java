package org.radarcns.webapp;

import java.net.URI;
import org.glassfish.jersey.server.ResourceConfig;
import org.radarbase.jersey.GrizzlyServer;
import org.radarbase.jersey.config.ConfigLoader;
import org.radarcns.config.ApplicationConfig;

/**
 * Radar application configuration.
 *
 * <p>Replaces previous {@code web.xml}.
 */
public class RadarApplication {
    /**
     * Radar application configuration.
     */
    public static void main(String... args) {
        try {
            ApplicationConfig config = ConfigLoader.INSTANCE.loadConfig("radar.yml", args,
                ApplicationConfig.class);
            ResourceConfig resources = ConfigLoader.INSTANCE.loadResources(
                RestApiEnhancerFactory.class, config);
            GrizzlyServer server = new GrizzlyServer(
                new URI("http://0.0.0.0:8080/api/"), resources, false);
            server.listen();
        } catch (Exception ex) {
            System.err.println("Failed to run server: " + ex.toString());
        }
    }
}
