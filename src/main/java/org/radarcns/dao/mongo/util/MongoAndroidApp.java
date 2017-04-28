package org.radarcns.dao.mongo.util;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCursor;
import java.net.ConnectException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.bson.Document;
import org.radarcns.avro.restapi.app.Application;
import org.radarcns.avro.restapi.header.TimeFrame;
import org.radarcns.avro.restapi.source.SourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic DAO to return Android App status information.
 */
public abstract class MongoAndroidApp extends MongoDataAccess {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoAndroidApp.class);

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
                    MongoHelper.getCollection(client, getCollectionName()));

        if (!cursor.hasNext()) {
            LOGGER.debug("Empty cursor");
            cursor.close();
            return null;
        }

        Document doc = cursor.next();
        cursor.close();

        if (app == null) {
            return getApplication(doc, new Application());
        }

        return getApplication(doc, app);

    }

    protected abstract Application getApplication(Document doc, Application app);

    protected abstract String getCollectionName();

    @Override
    public String getCollectionName(SourceType source, TimeFrame interval) {
        return null;
    }

    @Override
    public Set<String> getCollectionNames() {
        return new HashSet<>(Collections.singleton(getCollectionName()));
    }

    @Override
    public SourceType getSourceType(String collection) {
        return SourceType.ANDROID;
    }

}