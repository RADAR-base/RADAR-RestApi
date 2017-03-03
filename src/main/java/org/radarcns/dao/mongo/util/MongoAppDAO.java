package org.radarcns.dao.mongo.util;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCursor;
import java.net.ConnectException;
import org.bson.Document;
import org.radarcns.avro.restapi.app.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic DAO to return Android App status information.
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public abstract class MongoAppDAO extends MongoDAO {

    private final Logger logger = LoggerFactory.getLogger(MongoAppDAO.class);

    /**
     * Returns an {@code Application} initialised with the extracted value.
     *
     * @param user is the userID
     * @param source is the sourceID
     * @param client is the mongoDb client instance
     * @return the last seen status update for the given user and source, otherwise null
     */
    public Application valueByUserSource(String user, String source, Application app,
            MongoClient client) throws ConnectException {

        MongoCursor<Document> cursor = MongoHelper
                .findDocumentByUserSource(user, source, null, -1, 1,
                    MongoHelper.getCollection(client, getAndroidCollection()));

        if (!cursor.hasNext()) {
            logger.debug("Empty cursor");
            cursor.close();
            return null;
        }

        Document doc = cursor.next();
        cursor.close();

        if (app == null) {
            app = new Application();
        }

        return getApplication(doc, app);
    }

    protected abstract Application getApplication(Document doc, Application app);

}