package org.radarcns.dao.mongo;

import java.net.ConnectException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.ServletContext;
import org.bson.Document;
import org.radarcns.avro.restapi.app.Application;
import org.radarcns.avro.restapi.app.ServerStatus;
import org.radarcns.dao.mongo.util.MongoAppDAO;
import org.radarcns.util.RadarConverter;

/**
 * Data Access Object for Android App Status values.
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public class AndroidDAO {

    //private final Logger logger = LoggerFactory.getLogger(AndroidDAO.class);

    private static final AndroidDAO instance = new AndroidDAO();

    public static AndroidDAO getInstance() {
        return instance;
    }

    private MongoAppDAO server = new MongoAppDAO() {
        @Override
        protected Application getApplication(Document doc, Application app) {

            app.setIpAddress(doc.getString("ipAddress"));
            app.setServerStatus(RadarConverter.getServerStatus(doc.getString("serverStatus")));

            return app;
        }

        @Override
        protected String getCollectionName() {
            return "application_server_status";
        }
    };

    private MongoAppDAO uptime = new MongoAppDAO() {
        @Override
        protected Application getApplication(Document doc, Application app) {
            app.setUptime(doc.getDouble("uptime"));

            return app;
        }

        @Override
        protected String getCollectionName() {
            return "application_uptime";
        }
    };

    private MongoAppDAO recordCounter = new MongoAppDAO() {
        @Override
        protected Application getApplication(Document doc, Application app) {
            app.setRecordsCached(doc.getInteger("recordsCached"));
            app.setRecordsCached(doc.getInteger("recordsSent"));
            app.setRecordsCached(doc.getInteger("recordsUnsent"));

            return app;
        }

        @Override
        protected String getCollectionName() {
            return "application_record_counts";
        }
    };

    /**
     * Computes the Android App Status realign on different collection.
     *
     * @param user identifier
     * @param source identifier
     * @param context useful to retrieves the MongoDB cliet
     * @return {@code Application} representing the status of the related Android App
     * @throws ConnectException if MongoDb is not available
     */
    public Application getStatus(String user, String source, ServletContext context)
            throws ConnectException {

        Application app = server.valueByUserSource(user, source, null, context);
        uptime.valueByUserSource(user, source, app, context);
        recordCounter.valueByUserSource(user, source, app, context);

        return app;
    }

    /**
     * Finds all users.
     *
     * @return all distinct userIDs for the given collection, otherwise an empty Collection
     *
     * @throws ConnectException if MongoDb is not available
     */
    public Collection<String> findAllUser(ServletContext context) throws ConnectException {
        Set<String> users = new HashSet<>();

        users.addAll(server.findAllUser(context));
        users.addAll(uptime.findAllUser(context));

        return users;
    }

    /**
     * Finds all sources for the given user.
     *
     * @param user is the userID
     * @return all distinct sourceIDs for the given collection, otherwise empty Collection
     *
     * @throws ConnectException if MongoDb is not available
     */
    public Collection<String> findAllSoucesByUser(String user, ServletContext context)
            throws ConnectException {
        Set<String> users = new HashSet<>();

        users.addAll(server.findAllSoucesByUser(user, context));
        users.addAll(uptime.findAllSoucesByUser(user, context));

        return users;
    }
}