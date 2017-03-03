package org.radarcns.dao.mongo;

import com.mongodb.MongoClient;
import java.net.ConnectException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.bson.Document;
import org.radarcns.avro.restapi.app.Application;
import org.radarcns.avro.restapi.source.Source;
import org.radarcns.avro.restapi.source.SourceType;
import org.radarcns.dao.mongo.util.MongoAppDAO;
import org.radarcns.util.RadarConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data Access Object for Android App Status values.
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public class AndroidDAO {

    private final Logger logger = LoggerFactory.getLogger(AndroidDAO.class);

    private static final AndroidDAO instance = new AndroidDAO();

    public static AndroidDAO getInstance() {
        return instance;
    }

    private MongoAppDAO server = new MongoAppDAO() {
        @Override
        protected Application getApplication(Document doc, Application app) {
            app.setIpAddress(doc.getString("clientIP"));
            app.setServerStatus(RadarConverter.getServerStatus(doc.getString("serverStatus")));

            return app;
        }

        @Override
        public String getAndroidCollection() {
            return "application_server_status";
        }
    };

    private MongoAppDAO uptime = new MongoAppDAO() {
        @Override
        protected Application getApplication(Document doc, Application app) {
            app.setUptime(doc.getDouble("applicationUptime"));

            return app;
        }

        @Override
        public String getAndroidCollection() {
            return "application_uptime";
        }
    };

    private MongoAppDAO recordCounter = new MongoAppDAO() {
        @Override
        protected Application getApplication(Document doc, Application app) {
            app.setRecordsCached(doc.getInteger("recordsCached"));
            app.setRecordsSent(doc.getInteger("recordsSent"));
            app.setRecordsUnsent(doc.getInteger("recordsUnsent"));

            return app;
        }

        @Override
        public String getAndroidCollection() {
            return "application_record_counts";
        }
    };

    /**
     * Computes the Android App Status realign on different collection.
     *
     * @param user identifier
     * @param source identifier
     * @param client is the MongoDb client
     * @return {@code Application} representing the status of the related Android App
     * @throws ConnectException if MongoDb is not available
     */
    public Application getStatus(String user, String source, MongoClient client)
            throws ConnectException {

        Application app = server.valueByUserSource(user, source, null, client);
        uptime.valueByUserSource(user, source, app, client);
        recordCounter.valueByUserSource(user, source, app, client);

        return app;
    }

    /**
     * Finds all users.
     *
     * @return all distinct userIDs for the given collection, otherwise an empty Collection
     *
     * @throws ConnectException if MongoDb is not available
     */
    public Collection<String> findAllUser(MongoClient client) throws ConnectException {
        Set<String> users = new HashSet<>();

        users.addAll(server.findAllUser(client));
        users.addAll(uptime.findAllUser(client));
        users.addAll(recordCounter.findAllUser(client));

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
    public Collection<Source> findAllSoucesByUser(String user, MongoClient client)
            throws ConnectException {
        Set<Source> users = new HashSet<>();

        users.addAll(server.findAllSourcesByUser(user, client));
        users.addAll(uptime.findAllSourcesByUser(user, client));
        users.addAll(recordCounter.findAllSourcesByUser(user, client));

        return users;
    }

    /**
     * Finds the source type for the given sourceID
     *
     * @param source SourceID
     * @param client MongoDB client
     * @return a study {@code SourceType}
     *
     * @throws ConnectException if MongoDB is not available
     *
     * @see {@link org.radarcns.avro.restapi.source.SourceType}
     */
    public SourceType findSourceType(String source, MongoClient client) throws ConnectException {
        SourceType type = server.findSourceType(source, client);

        if (type == null) {
            type = uptime.findSourceType(source, client);
        }

        if (type == null) {
            type = recordCounter.findSourceType(source, client);
        }

        return type;
    }
}