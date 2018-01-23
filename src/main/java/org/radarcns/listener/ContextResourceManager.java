package org.radarcns.listener;

import java.io.IOException;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.radarcns.catalog.SourceCatalog;
import org.radarcns.exception.TokenException;
import org.radarcns.listener.managementportal.ManagementPortalClient;
import org.radarcns.listener.managementportal.ManagementPortalClientManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A common place to manage context specific resources that can be reused.
 */
public class ContextResourceManager implements ServletContextListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContextResourceManager.class);
    private static final String SOURCE_CATALOGUE = "SOURCE_CATALOGUE";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            getSourceCatalogue(sce.getServletContext());
        } catch (TokenException | IOException e) {
            LOGGER.warn("Cannot initialize ManagementPortal Client due to Token exception ", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        sce.getServletContext().setAttribute(SOURCE_CATALOGUE, null);
    }

    /**
     * Returns the singleton.
     *
     * @return the singleton {@code SourceCatalog} instance
     */
    public static SourceCatalog getSourceCatalogue(ServletContext context)
            throws TokenException, IOException {

        SourceCatalog sourceCatalog = (SourceCatalog) context
                .getAttribute(SOURCE_CATALOGUE);
        if (sourceCatalog == null) {
            ManagementPortalClient client = ManagementPortalClientManager
                    .getManagementPortalClient(context);

            sourceCatalog = new SourceCatalog(client);
            context.setAttribute(SOURCE_CATALOGUE, sourceCatalog);
        }
        return sourceCatalog;
    }
}
