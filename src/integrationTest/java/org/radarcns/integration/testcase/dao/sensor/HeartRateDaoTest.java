package org.radarcns.integration.testcase.dao.sensor;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.junit.After;
import org.junit.Test;
import org.radarcns.avro.restapi.dataset.Dataset;
import org.radarcns.avro.restapi.sensor.HeartRate;
import org.radarcns.avro.restapi.sensor.SensorType;
import org.radarcns.avro.restapi.sensor.Unit;
import org.radarcns.avro.restapi.source.SourceType;
import org.radarcns.config.Properties;
import org.radarcns.dao.SensorDataAccessObject;
import org.radarcns.dao.mongo.sensor.HeartRateDAO;
import org.radarcns.dao.mongo.util.MongoHelper;
import org.radarcns.integration.util.RandomInput;
import org.radarcns.integration.util.Utility;
import org.radarcns.util.RadarConverter;

import java.nio.file.Paths;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.radarcns.avro.restapi.header.DescriptiveStatistic.COUNT;
import static org.radarcns.avro.restapi.sensor.SensorType.HEART_RATE;
import static org.radarcns.avro.restapi.source.SourceType.EMPATICA;

/**
 * UserDao Test.
 */
public class HeartRateDaoTest {

    //private static final Logger logger = LoggerFactory.getLogger(SourceDaoTest.class);

    private static final String USER = "UserID_0";
    private static final String SOURCE = "SourceID_0";
    private static final SourceType SOURCE_TYPE = EMPATICA;
    private static final SensorType SENSOR_TYPE = HEART_RATE;
    private static final int SAMPLES = 10;

    @Test
    public void valueRTByUserSourceTest() throws Exception {
        Properties.getInstanceTest(Paths.get(this.getClass().getClassLoader().getResource(
                Properties.NAME_FILE).toURI()).toString());

        MongoClient client = Utility.getMongoClient();

        MongoCollection<Document> collection = MongoHelper.getCollection(client,
            HeartRateDAO.getInstance().getCollectionName(SOURCE_TYPE));

        List<Document> docs = RandomInput.getDocumentsRandom(USER, SOURCE, SOURCE_TYPE, SENSOR_TYPE,
            COUNT, SAMPLES, false);

        collection.insertMany(docs);

        Dataset actual = HeartRateDAO.getInstance().valueRTByUserSource(USER, SOURCE,
                Unit.BEATS_PER_MIN, RadarConverter.getMongoStat(COUNT), collection);

        Dataset expected = Utility.convertDocToDataset(singletonList(docs.get(docs.size() - 1)),
                RadarConverter.getMongoStat(COUNT), Unit.BEATS_PER_MIN, HeartRate.class);

        assertEquals(expected, actual);

        dropAndClose(client);
    }

    @Test
    public void valueByUserSourceTest() throws Exception {
        Properties.getInstanceTest(Paths.get(this.getClass().getClassLoader().getResource(
                Properties.NAME_FILE).toURI()).toString());

        MongoClient client = Utility.getMongoClient();

        MongoCollection<Document> collection = MongoHelper.getCollection(client,
            HeartRateDAO.getInstance().getCollectionName(SOURCE_TYPE));

        List<Document> docs = RandomInput.getDocumentsRandom(USER, SOURCE, SOURCE_TYPE, SENSOR_TYPE,
            COUNT, SAMPLES, false);

        collection.insertMany(docs);

        Dataset actual = HeartRateDAO.getInstance().valueByUserSource(USER, SOURCE,
            Unit.BEATS_PER_MIN, RadarConverter.getMongoStat(COUNT), collection);

        Dataset expected = Utility.convertDocToDataset(docs,
            RadarConverter.getMongoStat(COUNT), Unit.BEATS_PER_MIN, HeartRate.class);

        assertEquals(expected, actual);

        dropAndClose(client);
    }

    @Test
    public void valueByUserSourceWindowTest() throws Exception {
        Properties.getInstanceTest(Paths.get(this.getClass().getClassLoader().getResource(
                Properties.NAME_FILE).toURI()).toString());

        MongoClient client = Utility.getMongoClient();

        MongoCollection<Document> collection = MongoHelper.getCollection(client,
            HeartRateDAO.getInstance().getCollectionName(SOURCE_TYPE));

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

        Dataset actual = HeartRateDAO.getInstance().valueByUserSourceWindow(USER, SOURCE,
                Unit.BEATS_PER_MIN, RadarConverter.getMongoStat(COUNT), start, end, collection);

        Dataset expected = Utility.convertDocToDataset(docs.subList(index - 1, index + 2),
                RadarConverter.getMongoStat(COUNT), Unit.BEATS_PER_MIN, HeartRate.class);

        assertEquals(expected, actual);

        dropAndClose(client);
    }

    @Test
    public void countSamplesByUserSourceWindowTest() throws Exception {
        Properties.getInstanceTest(Paths.get(this.getClass().getClassLoader().getResource(
                Properties.NAME_FILE).toURI()).toString());

        MongoClient client = Utility.getMongoClient();

        MongoCollection<Document> collection = MongoHelper.getCollection(client,
            HeartRateDAO.getInstance().getCollectionName(SOURCE_TYPE));

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

        double actual = HeartRateDAO.getInstance().countSamplesByUserSourceWindow(
            USER, SOURCE, start, end, collection);

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

    public void dropAndClose(MongoClient client) {
        Utility.dropCollection(client, MongoHelper.DEVICE_CATALOG);
        Utility.dropCollection(client,
                SensorDataAccessObject.getInstance().getCollectionName(SOURCE_TYPE, SENSOR_TYPE));
        client.close();
    }
}
