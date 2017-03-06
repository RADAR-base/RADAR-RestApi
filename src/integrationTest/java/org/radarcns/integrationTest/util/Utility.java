package org.radarcns.integrationTest.util;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.avro.specific.SpecificRecord;
import org.bson.Document;
import org.radarcns.avro.restapi.dataset.Dataset;
import org.radarcns.avro.restapi.dataset.Item;
import org.radarcns.avro.restapi.header.EffectiveTimeFrame;
import org.radarcns.avro.restapi.header.Header;
import org.radarcns.avro.restapi.sensor.Unit;
import org.radarcns.config.Properties;
import org.radarcns.dao.mongo.util.MongoHelper;
import org.radarcns.dao.mongo.util.MongoHelper.Stat;
import org.radarcns.listner.MongoDBContextListener;
import org.radarcns.util.RadarConverter;

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

    public static Dataset convertDocToDataset(List<Document> docs, Stat stat, Unit unit,
            Class<? extends SpecificRecord> recordClass)
        throws IllegalAccessException, InstantiationException {
        EffectiveTimeFrame eftHeader = new EffectiveTimeFrame(
            RadarConverter.getISO8601(docs.get(0).getDate("start")),
            RadarConverter.getISO8601(docs.get(docs.size() - 1).getDate("end")));

        List<Item> itemList = new LinkedList<>();
        for (Document doc : docs) {
            SpecificRecord record = recordClass.newInstance();
            switch (stat) {
                case quartile:
                    throw new UnsupportedOperationException("Not yet implemented");
                default:
                    record.put(record.getSchema().getField("value").pos(),
                        doc.getDouble(stat.getParam()));
            }
            itemList.add(new Item(record, new EffectiveTimeFrame(
                RadarConverter.getISO8601(doc.getDate("start")),
                RadarConverter.getISO8601(doc.getDate("end")))));
        }

        Header header = new Header(RadarConverter.getDescriptiveStatistic(stat), unit, eftHeader);
        return new Dataset(header, itemList);
    }
}
