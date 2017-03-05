package org.radarcns.integrationTest.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.apache.avro.specific.SpecificRecord;
import org.bson.Document;
import org.radarcns.config.Properties;
import org.radarcns.dao.mongo.util.MongoHelper;
import org.radarcns.listner.MongoDBContextListener;

/**
 * Created by francesco on 03/03/2017.
 */
public class Utility {

    public static MongoClient getMongoClient() {
        List<MongoCredential> credentials = Properties.getInstance().getMongoDbCredential();
        MongoClient client = new MongoClient(Properties.getInstance().getMongoHosts(),credentials);
        if (!MongoDBContextListener.checkMongoConnection(client)) {
            client = null;
        }

        return client;
    }

    /**
     * @param value Long value that has to be converted.
     * @return the number of milliseconds since January 1, 1970, 00:00:00 GMT representing the
     *      initial time of a Kafka time window.
     **/
    public static Long getStartTimeWindow(Long value) {
        Double timeDouble = value.doubleValue() / 10000d;
        return timeDouble.longValue() * 10000;
    }

    /**
     * Converts AVRO objects in pretty JSON.
     * @param record Specific Record that has to be converted
     * @return String with the object serialised in pretty JSON
     */
    public static String getPrettyJSON(SpecificRecord record) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Object json = mapper.readValue(record.toString(), Object.class);
        String indented = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
        return  indented;
    }

    /**
     * Drop mongo collection called name.
     * @param client mongoDB client
     * @param name collection name that has to be dropped
     */
    public static void dropCollection(MongoClient client, String name) {
        MongoHelper.getCollection(client, name).drop();
    }

    /**
     * Drop mongo collection in names.
     * @param client mongoDB client
     * @param names collection names that have to be dropped
     */
    public static void dropCollection(MongoClient client, List<String> names) {
        for (String tmp : names) {
            MongoHelper.getCollection(client, tmp).drop();
        }
    }

    /**
     * Inserts mixed documents in mixed collections.
     * @param client mongoDb client to access the instance
     * @param map mapping between document and collections
     */
    public static void insertMixedDocs(MongoClient client, Map<String, Document> map) {
        for (String collectionName : map.keySet()) {
            MongoHelper.getCollection(client, collectionName).insertOne(map.get(collectionName));
        }
    }

}
