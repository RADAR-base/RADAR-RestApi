package org.radarcns.integration.testcase.dao.sensor;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.radarcns.avro.restapi.header.DescriptiveStatistic.COUNT;
import static org.radarcns.avro.restapi.sensor.SensorType.HEART_RATE;
import static org.radarcns.avro.restapi.source.SourceType.EMPATICA;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import java.util.List;
import org.bson.Document;
import org.junit.After;
import org.junit.Test;
import org.radarcns.avro.restapi.data.DoubleValue;
import org.radarcns.avro.restapi.dataset.Dataset;
import org.radarcns.avro.restapi.header.TimeFrame;
import org.radarcns.avro.restapi.sensor.SensorType;
import org.radarcns.avro.restapi.sensor.Unit;
import org.radarcns.avro.restapi.source.SourceType;
import org.radarcns.dao.SensorDataAccessObject;
import org.radarcns.dao.mongo.util.MongoHelper;
import org.radarcns.integration.util.RandomInput;
import org.radarcns.integration.util.Utility;
import org.radarcns.util.RadarConverter;

/**
 * UserDao Test.
 */
public class HeartRateDaoTest {

    //private static final Logger logger = LoggerFactory.getLogger(SourceDaoTest.class);

    private static final String USER = "UserID_0";
    private static final String SOURCE = "SourceID_0";
    private static final SourceType SOURCE_TYPE = EMPATICA;
    private static final SensorType SENSOR_TYPE = HEART_RATE;
    private static final Class ITEM = DoubleValue.class;
    private static final TimeFrame TIME_FRAME = TimeFrame.TEN_SECOND;
    private static final int SAMPLES = 10;

    @Test
    public void valueRealTimeByUserSourceTest() throws Exception {
        MongoClient client = Utility.getMongoClient();

        MongoCollection<Document> collection = MongoHelper.getCollection(client,
                SensorDataAccessObject.getInstance(SENSOR_TYPE).getCollectionName(
                    SOURCE_TYPE, TIME_FRAME));

        List<Document> docs = RandomInput.getDocumentsRandom(USER, SOURCE, SOURCE_TYPE, SENSOR_TYPE,
                COUNT, SAMPLES, false);

        collection.insertMany(docs);

        Dataset actual = SensorDataAccessObject.getInstance(SENSOR_TYPE).valueRTByUserSource(
                USER, SOURCE, Unit.BEATS_PER_MIN, RadarConverter.getMongoStat(COUNT), TIME_FRAME,
                    collection);

        Dataset expected = Utility.convertDocToDataset(singletonList(docs.get(docs.size() - 1)),
                RadarConverter.getMongoStat(COUNT), Unit.BEATS_PER_MIN, TIME_FRAME, ITEM);

        assertEquals(expected, actual);

        dropAndClose(client);
    }

    @Test
    public void valueByUserSourceTest() throws Exception {
        MongoClient client = Utility.getMongoClient();

        MongoCollection<Document> collection = MongoHelper.getCollection(client,
                SensorDataAccessObject.getInstance(SENSOR_TYPE).getCollectionName(
                    SOURCE_TYPE, TIME_FRAME));

        List<Document> docs = RandomInput.getDocumentsRandom(USER, SOURCE, SOURCE_TYPE, SENSOR_TYPE,
                COUNT, SAMPLES, false);

        collection.insertMany(docs);

        Dataset actual = SensorDataAccessObject.getInstance(SENSOR_TYPE).valueByUserSource(USER,
                SOURCE, Unit.BEATS_PER_MIN, RadarConverter.getMongoStat(COUNT),
                    TimeFrame.TEN_SECOND, collection);

        Dataset expected = Utility.convertDocToDataset(docs,
                RadarConverter.getMongoStat(COUNT), Unit.BEATS_PER_MIN, TIME_FRAME, ITEM);

        assertEquals(expected, actual);

        dropAndClose(client);
    }

    @Test
    public void valueByUserSourceWindowTest() throws Exception {
        MongoClient client = Utility.getMongoClient();

        MongoCollection<Document> collection = MongoHelper.getCollection(client,
                SensorDataAccessObject.getInstance(SENSOR_TYPE).getCollectionName(
                    SOURCE_TYPE, TIME_FRAME));

        List<Document> docs = RandomInput.getDocumentsRandom(USER, SOURCE, SOURCE_TYPE, SENSOR_TYPE,
                COUNT, SAMPLES, false);
        while (docs.size() < 6) {
            docs = RandomInput.getDocumentsRandom(USER, SOURCE, SOURCE_TYPE, SENSOR_TYPE,
                COUNT, SAMPLES, false);
        }
        collection.insertMany(docs);

        int index = Math.max(3, docs.size() / 2);

        long start = docs.get(index - 1).getDate(MongoHelper.START).getTime();
        long end = docs.get(index + 1).getDate(MongoHelper.END).getTime();

        Dataset actual = SensorDataAccessObject.getInstance(SENSOR_TYPE).valueByUserSourceWindow(
                USER, SOURCE, Unit.BEATS_PER_MIN, RadarConverter.getMongoStat(COUNT), TIME_FRAME,
                    start, end, collection);

        Dataset expected = Utility.convertDocToDataset(docs.subList(index - 1, index + 2),
                RadarConverter.getMongoStat(COUNT), Unit.BEATS_PER_MIN, TIME_FRAME, ITEM);

        assertEquals(expected, actual);

        dropAndClose(client);
    }

    @Test
    public void countSamplesByUserSourceWindowTest() throws Exception {
        MongoClient client = Utility.getMongoClient();

        MongoCollection<Document> collection = MongoHelper.getCollection(client,
                SensorDataAccessObject.getInstance(SENSOR_TYPE).getCollectionName(
                    SOURCE_TYPE, TIME_FRAME));

        List<Document> docs = RandomInput.getDocumentsRandom(USER, SOURCE, SOURCE_TYPE, SENSOR_TYPE,
                COUNT, SAMPLES, false);
        while (docs.size() < 6) {
            docs = RandomInput.getDocumentsRandom(USER, SOURCE, SOURCE_TYPE, SENSOR_TYPE,
                COUNT, SAMPLES, false);
        }
        collection.insertMany(docs);

        int index = Math.max(3, docs.size() / 2);

        long start = docs.get(index - 1).getDate(MongoHelper.START).getTime();
        long end = docs.get(index + 1).getDate(MongoHelper.END).getTime();

        double actual = SensorDataAccessObject.getInstance(
                SENSOR_TYPE).countSamplesByUserSourceWindow(USER, SOURCE, start, end, collection);

        double expected = 0;
        for (Document doc : docs.subList(index - 1, index + 2)) {
            expected += doc.getDouble("count");
        }

        assertEquals(expected, actual, 0);

        dropAndClose(client);
    }

    @After
    public void dropAndClose() {
        dropAndClose(Utility.getMongoClient());
    }

    /** Drops all used collections to bring the database back to the initial state, and close the
     *      database connection.
     **/
    public void dropAndClose(MongoClient client) {
        Utility.dropCollection(client, MongoHelper.DEVICE_CATALOG);
        Utility.dropCollection(client,
                SensorDataAccessObject.getInstance(SENSOR_TYPE).getCollectionName(
                SOURCE_TYPE, TIME_FRAME));
        client.close();
    }
}
