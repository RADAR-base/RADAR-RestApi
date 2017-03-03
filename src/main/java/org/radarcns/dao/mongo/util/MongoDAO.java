package org.radarcns.dao.mongo.util;

import static org.radarcns.dao.mongo.util.MongoHelper.SOURCE_TYPE;

import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import java.net.ConnectException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.bson.Document;
import org.radarcns.avro.restapi.source.Source;
import org.radarcns.avro.restapi.source.SourceType;
import org.radarcns.util.RadarConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic function for user management.
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public abstract class MongoDAO {

    private static final Logger logger = LoggerFactory.getLogger(MongoDAO.class);

    /**
     * Finds all users.
     *
     * @return all distinct userIDs for the current DAO instance, otherwise an empty Collection
     */
    public Collection<String> findAllUser(MongoClient client) throws ConnectException {
        Set<String> set = new HashSet<>();

        MongoCursor<String> cursor;
        for (String collection : getCollectionNames()) {
            cursor = MongoHelper.findAllUser(MongoHelper.getCollection(client, collection));

            if (!cursor.hasNext()) {
                logger.debug("Empty cursor for collection {}", collection);
            }

            while (cursor.hasNext()) {
                set.add(cursor.next());
            }

            cursor.close();
        }

        return set;
    }

    /**
     * Finds all sources for the given user.
     *
     * @param user is the userID
     * @return all distinct sourceIDs for the given collection, otherwise empty Collection
     */
    public Collection<Source> findAllSourcesByUser(String user, MongoClient client)
            throws ConnectException {
        Set<Source> list = new HashSet<>();

        MongoCursor<String> cursor;
        for (String collection : getCollectionNames()) {
            cursor = MongoHelper.findAllSourceByUser(user,
                    MongoHelper.getCollection(client, collection));

            if (!cursor.hasNext()) {
                logger.debug("Empty cursor");
            }

            while (cursor.hasNext()) {
                list.add(new Source(cursor.next(), getSourceType(collection), null));
            }

            cursor.close();
        }
        return list;
    }

    /**
     * Finds source type for the given user checking all available source collections.
     *
     * @param source is the sourceID
     * @return source type for the given sourceID, otherwise null
     */
    public SourceType findSourceType(String source, MongoClient client)
        throws ConnectException {
        SourceType type = null;

        MongoCursor<Document> cursor;
        for (String collection : getCollectionNames()) {
            cursor = MongoHelper.findDocumentBySource(source, null, 0, 1,
                MongoHelper.getCollection(client, collection));

            if (!cursor.hasNext()) {
                logger.debug("Empty cursor");
            }

            while (cursor.hasNext()) {
                type = getSourceType(collection);
            }

            cursor.close();
        }
        return type;
    }

    /**
     * Extract the sensor type from the MongoDB collection.
     *
     * @param collection collection name
     * @return the source type
     */
    private SourceType getSourceType(String collection) {
        if (collection.toLowerCase().contains("empatica")) {
            return SourceType.EMPATICA;
        } else if (collection.toLowerCase().contains("application")) {
            return SourceType.ANDROID;
        } else if (collection.toLowerCase().contains("pebble")) {
            return SourceType.PEBBLE;
        } else if (collection.toLowerCase().contains("biovotion")) {
            return SourceType.BIOVOTION;
        }

        throw new IllegalArgumentException("The collection name " + collection
            + " does not match with any SourceType");
    }

    /**
     * Finds source type for the given user using the source catalog.
     *
     * @param source is the sourceID
     * @return source type for the given sourceID, otherwise null
     */
    public static SourceType getSourceType(String source, MongoClient client)
            throws ConnectException {
        SourceType type = null;

        MongoCursor<Document> cursor = MongoHelper.findDocumentBySource(source, null,
                0, 1, MongoHelper.getCollection(client, MongoHelper.DEVICE_CATALOG));

        if (!cursor.hasNext()) {
            logger.debug("Empty cursor");
        }

        Document doc = cursor.tryNext();

        if (doc != null) {
            type = RadarConverter.getSourceType(doc.getString(SOURCE_TYPE));
        }

        cursor.close();

        return type;
    }

    /**
     * Writes the source type on the source catalog.
     *
     * @param source is the sourceID
     * @param type the source type that is assigned to the sourceID
     * @param client MongoDb client
     *
     * @throws ConnectException if MongoDB is not available
     * @throws MongoException if something goes wrong with the write
     */
    public static void witeSourceType(String source, SourceType type, MongoClient client)
            throws ConnectException, MongoException {

        MongoCursor<Document> cursor = MongoHelper.findDocumentBySource(source, null,
                0, 1, MongoHelper.getCollection(client, MongoHelper.DEVICE_CATALOG));

        Document doc = new Document().append("_id", source).append(SOURCE_TYPE, type);

        MongoCollection<Document> collection = MongoHelper.getCollection(client,
                MongoHelper.DEVICE_CATALOG);

        collection.insertOne(doc);
    }

    /**
     * Returns the current collection.
     *
     * @param client is the MongoDb client instance
     * @return the MongoDb collection
     */
    protected MongoCollection<Document> getCollection(MongoClient client, SourceType source)
            throws ConnectException {
        return MongoHelper.getCollection(client,getCollectionName(source));
    }

    /**
     * Returns the required mongoDB collection name for the given source type.
     *
     * @param source source type
     * @return the MongoDB Collection name
     */
    public String getCollectionName(SourceType source) {
        switch (source) {
            case ANDROID: return getAndroidCollection();
            case BIOVOTION: return getBiovotionCollection();
            case EMPATICA: return getEmpaticaCollection();
            case PEBBLE: return getPebbleCollection();
            default: throw new IllegalArgumentException("Unknown source type. " + source
                    + "is not yest supported.");

        }
    }

    /**
     * @implSpec this function must be override by the subclass.
     * @return the MongoDB Collection name for extracting data of Android on-board sensors
     */
    public String getAndroidCollection() {
        throw new UnsupportedOperationException("This function must be override by the subclass");
    }

    /**
     * @implSpec this function must be override by the subclass.
     * @return the MongoDB Collection name for extracting data of Biovotion on-board sensors
     */
    public String getBiovotionCollection() {
        throw new UnsupportedOperationException("This function must be override by the subclass");
    }

    /**
     * @implSpec this function must be override by the subclass.
     * @return the MongoDB Collection name for extracting data of Empatica on-board sensors
     */
    public String getEmpaticaCollection() {
        throw new UnsupportedOperationException("This function must be override by the subclass");
    }

    /**
     * @implSpec this function must be override by the subclass.
     * @return the MongoDB Collection name for extracting data of Pebble on-board sensors
     */
    public String getPebbleCollection() {
        throw new UnsupportedOperationException("This function must be override by the subclass");
    }

    /**
     * Returns all available collection for the current instance.
     *
     * @return the MongoDB Collection name
     */
    public Set<String> getCollectionNames() {
        Set<String> collections = new HashSet<>();

        for (SourceType source : SourceType.values()) {
            try {
                collections.add(getCollectionName(source));
            } catch (UnsupportedOperationException exec) {
                //Nothing to do
            }
        }

        return collections;
    }
}
