package org.radarcns.listener;

import java.io.IOException;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.radarcns.catalog.SourceCatalog;
import org.radarcns.dao.SensorDataAccessObject;
import org.radarcns.dao.SourceDataAccessObject;
import org.radarcns.dao.SubjectDataAccessObject;
import org.radarcns.exception.TokenException;
import org.radarcns.listener.managementportal.ManagementPortalClient;
import org.radarcns.listener.managementportal.ManagementPortalClientManager;
import org.radarcns.mongo.util.MongoHelper;
import org.radarcns.service.SourceMonitorService;
import org.radarcns.service.SourceService;
import org.radarcns.service.SubjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A common place to manage context specific resources that can be reused.
 */
public class ContextResourceManager implements ServletContextListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContextResourceManager.class);
    private static final String SOURCE_CATALOGUE = "SOURCE_CATALOGUE";
    private static final String SENSOR_DATA_ACCESS_OBJECT = "SENSOR_DATA_ACCESS_OBJECT";
    private static final String SOURCE_DATA_ACCESS_OBJECT = "SOURCE_DATA_ACCESS_OBJECT";
    private static final String SUBJECT_DATA_ACCESS_OBJECT = "SUBJECT_DATA_ACCESS_OBJECT";
    private static final String SOURCE_SERVICE = "SOURCE_SERVICE";
    private static final String SUBJECT_SERVICE = "SUBJECT_SERVICE";
    private static final String SOURCE_MONITOR = "SOURCE_MONITOR";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            LOGGER.info("Initializing context resources...");
            getSourceCatalogue(sce.getServletContext());
            getSourceMonitor(sce.getServletContext());
            getSourceService(sce.getServletContext());
            getSubjectService(sce.getServletContext());

            getSourceDataAccessObject(sce.getServletContext());
            getSubjectDataAccessObject(sce.getServletContext());

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
            LOGGER.info("Initializing SourceCatalog");
            ManagementPortalClient client = ManagementPortalClientManager
                    .getManagementPortalClient(context);
            sourceCatalog = new SourceCatalog(client);
            context.setAttribute(SOURCE_CATALOGUE, sourceCatalog);
        }
        return sourceCatalog;
    }

    /**
     * Returns the singleton.
     *
     * @return the {@code SourceMonitorService} instance
     */
    public static SourceMonitorService getSourceMonitor(ServletContext context)
            throws TokenException, IOException {

        SourceMonitorService sourceMonitorService = (SourceMonitorService) context
                .getAttribute(SOURCE_MONITOR);
        if (sourceMonitorService == null) {
            LOGGER.info("Initializing SourceMonitorService");
            sourceMonitorService = new SourceMonitorService(MongoHelper.getClient(context));
            context.setAttribute(SOURCE_MONITOR, sourceMonitorService);
        }
        return sourceMonitorService;
    }



    /**
     * Returns the singleton.
     *
     * @return the {@code SourceService} instance
     */
    public static SourceService getSourceService(ServletContext context)
            throws TokenException, IOException {

        SourceService sourceService = (SourceService) context
                .getAttribute(SOURCE_SERVICE);
        if (sourceService == null) {
            LOGGER.info("Initializing SourceService");
            sourceService = new SourceService(getSourceMonitor(context) ,
                    getSourceCatalogue(context));
            context.setAttribute(SOURCE_SERVICE, sourceService);
        }
        return sourceService;
    }


    /**
     * Returns the singleton.
     *
     * @return the {@code SubjectService} instance
     */
    public static SubjectService getSubjectService(ServletContext context)
            throws TokenException, IOException {

        SubjectService subjectService = (SubjectService) context
                .getAttribute(SUBJECT_SERVICE);
        if (subjectService == null) {
            LOGGER.info("Initializing SubjectService");
            subjectService = new SubjectService(ManagementPortalClientManager
                    .getManagementPortalClient(context) , getSourceService(context));
            context.setAttribute(SUBJECT_SERVICE, subjectService);
        }
        return subjectService;
    }

    /**
     * Returns the singleton.
     *
     * @return the singleton {@code SensorDataAccessObject} instance
     */
    public static SensorDataAccessObject getSensorDataAccessObject(ServletContext context)
            throws TokenException, IOException {

        SensorDataAccessObject sensorDataAccessObject = (SensorDataAccessObject) context
                .getAttribute(SENSOR_DATA_ACCESS_OBJECT);
        if (sensorDataAccessObject == null) {


            sensorDataAccessObject = new SensorDataAccessObject(getSourceCatalogue(context));
            context.setAttribute(SENSOR_DATA_ACCESS_OBJECT, sensorDataAccessObject);
        }
        return sensorDataAccessObject;
    }

    /**
     * Returns the singleton.
     *
     * @return the singleton {@code SourceDataAccessObject} instance
     */
    public static SourceDataAccessObject getSourceDataAccessObject(ServletContext context)
            throws TokenException, IOException {

        SourceDataAccessObject sourceDataAccessObject = (SourceDataAccessObject) context
                .getAttribute(SOURCE_DATA_ACCESS_OBJECT);
        if (sourceDataAccessObject == null) {
            sourceDataAccessObject = new SourceDataAccessObject(getSensorDataAccessObject(context));
            context.setAttribute(SOURCE_DATA_ACCESS_OBJECT, sourceDataAccessObject);
        }
        return sourceDataAccessObject;
    }

    /**
     * Returns the singleton.
     *
     * @return the singleton {@code SourceDataAccessObject} instance
     */
    public static SubjectDataAccessObject getSubjectDataAccessObject(ServletContext context)
            throws TokenException, IOException {

        SubjectDataAccessObject subjectDataAccessObject = (SubjectDataAccessObject) context
                .getAttribute(SUBJECT_DATA_ACCESS_OBJECT);
        if (subjectDataAccessObject == null) {
            subjectDataAccessObject = new SubjectDataAccessObject(getSensorDataAccessObject
                    (context), getSourceDataAccessObject(context));
            context.setAttribute(SUBJECT_DATA_ACCESS_OBJECT, subjectDataAccessObject);
        }
        return subjectDataAccessObject;
    }
}
