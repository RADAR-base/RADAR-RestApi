package org.radarcns.dao.mongo.util;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Collection;
import javax.servlet.ServletContext;
import org.bson.Document;
import org.radarcns.avro.restapi.source.Source;
import org.radarcns.avro.restapi.source.Sources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic function for user management.
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public abstract class MongoDAO {

    private final Logger logger = LoggerFactory.getLogger(MongoDAO.class);

    /**
     * Finds all users.
     *
     * @return all distinct userIDs for the given collection, otherwise an empty Collection
     */
    public Collection<String> findAllUser(ServletContext context) throws ConnectException {
        MongoCursor<String> cursor = MongoHelper.findAllUser(getCollection(context));

        ArrayList<String> list = new ArrayList<>();

        if (!cursor.hasNext()) {
            logger.debug("Empty cursor");
        }

        while (cursor.hasNext()) {
            list.add(cursor.next());
        }

        cursor.close();
        return list;
    }

    /**
     * Finds all sources for the given user.
     *
     * @param user is the userID
     * @return all distinct sourceIDs for the given collection, otherwise empty Collection
     */
    public Collection<Source> findAllSoucesByUser(String user, ServletContext context)
            throws ConnectException {
        MongoCursor<String> cursor = MongoHelper.findAllSourceByUser(user, getCollection(context));

        ArrayList<Source> list = new ArrayList<>();

        if (!cursor.hasNext()) {
            logger.debug("Empty cursor");
        }

        while (cursor.hasNext()) {
            list.add(new Source(cursor.next(), getSourceType(), null));
        }

        cursor.close();
        return list;
    }

    /**
     * Extract the sensor type from the MongoDB collection.
     *
     * @return the source type
     */
    private Sources getSourceType() {
        String name = getCollectionName();

        if (name.toLowerCase().contains("empatica")) {
            return Sources.Empatica;
        } else if (name.toLowerCase().contains("application")) {
            return Sources.Android;
        } else if (name.toLowerCase().contains("pebble")) {
            return Sources.Pebble;
        } else if (name.toLowerCase().contains("biovotion")) {
            return Sources.Biovotion;
        }

        return Sources.Unknown;
    }

    /**
     * Returns the current collection.
     *
     * @param context is the servelet context needed to retrieve the mongodb client instance
     * @return the MongoDb collection
     */
    protected MongoCollection<Document> getCollection(ServletContext context)
            throws ConnectException {
        return MongoHelper.getCollection(context,getCollectionName());
    }

    /**
     * @implSpec this function must be override by the subclass.
     * @return the MongoDB Collection name
     */
    protected abstract String getCollectionName();
}
