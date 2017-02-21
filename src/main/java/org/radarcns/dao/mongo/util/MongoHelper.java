package org.radarcns.dao.mongo.util;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.gte;
import static com.mongodb.client.model.Filters.lte;
import static org.radarcns.listner.MongoDBContextListener.MONGO_CLIENT;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import java.net.ConnectException;
import java.util.Date;
import javax.servlet.ServletContext;
import org.bson.Document;
import org.radarcns.config.Properties;
import org.radarcns.listner.MongoDBContextListener;

/**
 * Generic MongoDB helper.
 */
public class MongoHelper {

    //private static final Logger logger = LoggerFactory.getLogger(MongoHelper.class);

    private static final String USER = "user";
    private static final String SOURCE = "source";
    private static final String START = "start";
    private static final String END = "end";

    /**
     * Enumerate all available statistical values.
     */
    public enum Stat {
        avg("avg"), count("count"), iqr("iqr"), max("max"), median("quartile"), min("min"),
        quartile("quartile"), sum("sum");

        private final String param;

        Stat(String param) {
            this.param = param;
        }

        public String getParam() {
            return param;
        }
    }

    /**
     * Finds all Documents within [start-end] belonging to the given user for the give source.
     *
     * @param user is the userID
     * @param source is the sourceID
     * @param start is the start time of the queried timewindow
     * @param end is the end time of the queried timewindow
     * @param collection is the MongoDB that will be queried
     * @return a MongoDB cursor containing all documents between start and end for the given User,
     *      Source and MongoDB collection
     */
    protected static MongoCursor<Document> findDocumentByUserSourceWindow(String user,
            String source, Long start, Long end, MongoCollection<Document> collection) {
        FindIterable<Document> result = collection.find(
                Filters.and(
                        eq(USER,user),
                        eq(SOURCE,source),
                        gte(START,new Date(start)),
                        lte(END,new Date(end)))).sort(new BasicDBObject(START,1));

        return result.iterator();
    }

    /**
     * Finds all Documents belonging to the given user for the give source.
     *
     * @param user is the userID
     * @param source is the sourceID
     * @param sortBy states the way in which documents have to be sorted. It is optional
     * @param limit is the number of document that will be retrieved
     * @param collection is the MongoDB that will be queried
     * @return a MongoDB cursor containing all documents for the given User,Source and MongoDB
     *      collection
     */
    protected static MongoCursor<Document> findDocumentByUserSource(String user, String source,
            String sortBy, int order, Integer limit, MongoCollection<Document> collection) {
        FindIterable<Document> result;

        if (sortBy == null) {
            result = collection.find(
                Filters.and(
                    eq(USER, user),
                    eq(SOURCE, source)));
        } else {
            result = collection.find(
                Filters.and(
                    eq(USER, user),
                    eq(SOURCE, source))
            ).sort(new BasicDBObject(sortBy, order));
        }

        if (limit != null) {
            result = result.limit(limit);
        }

        return result.iterator();
    }

    /**
     * Finds all users.
     *
     * @param collection is the MongoDB that will be queried
     * @return a MongoDB cursor containing all distinct users for the given MongoDB collection
     */
    public static MongoCursor<String> findAllUser(MongoCollection<Document> collection) {
        return collection.distinct("user", String.class).iterator();
    }

    /**
     * Finds all sources for the given user.
     *
     * @param user is the userID
     * @param collection is the MongoDB that will be queried
     * @return a MongoDB cursor containing all distinct sources for the given User and MongoDB
     *      collection
     */
    public static MongoCursor<String> findAllSourceByUser(String user,
                MongoCollection<Document> collection) {
        return collection.distinct("source", String.class)
                .filter(eq(USER,user)).iterator();
    }

    /**
     * Returns the needed MongoDB collection.
     *
     * @param context the application context maintaining the MongoDB client
     * @param collection is the name of the returned connection
     * @return the MongoDB collection named collection. If the MongoDB client is null, it first
     *      tries to establish a new connection and then return.
     * @throws ConnectException if MongoDB cannot be reached
     */
    public static MongoCollection<Document> getCollection(ServletContext context, String collection)
            throws ConnectException {
        MongoDBContextListener.testConnection(context);

        if (context.getAttribute(MONGO_CLIENT) == null) {
            MongoDBContextListener.recoverOrThrow(context);
        }

        MongoClient mongoClient = (MongoClient) context.getAttribute(MONGO_CLIENT);
        MongoDatabase database = mongoClient.getDatabase(Properties.getInstance().getMongoDbName());

        return database.getCollection(collection);
    }
}