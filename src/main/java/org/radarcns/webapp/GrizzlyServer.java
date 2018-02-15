package org.radarcns.webapp;

import java.net.URI;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Server for the REST-API.
 */
public class GrizzlyServer {

    private static final Logger logger = LoggerFactory.getLogger(GrizzlyServer.class);
    private static final String BASE_URI = "http://0.0.0.0:8080/api/";

    /**
     * Main command line server.
     */
    public static void main(String[] args) {
        ServiceLocator locator = ServiceLocatorUtilities.createAndPopulateServiceLocator();

        HttpServer httpServer = GrizzlyHttpServerFactory
                .createHttpServer(URI.create(BASE_URI), new RadarApplication(), locator);

        try {
            httpServer.start();

            System.out.println(String.format("Jersey app started on %s.\nHit any key to stop it...",
                    BASE_URI));
            //noinspection ResultOfMethodCallIgnored
            System.in.read();
        } catch (Exception e) {
            logger.error("Error starting server", e);
        }
    }
}
