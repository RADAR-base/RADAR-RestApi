package org.radarcns.integrationTest.testCase.webapp;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.radarcns.avro.restapi.header.DescriptiveStatistic.COUNT;
import static org.radarcns.avro.restapi.sensor.SensorType.HEART_RATE;
import static org.radarcns.avro.restapi.source.SourceType.EMPATICA;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import java.io.IOException;
import java.util.List;
import okhttp3.Response;
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
import org.radarcns.dao.mongo.AndroidDAO;
import org.radarcns.dao.mongo.sensor.HeartRateDAO;
import org.radarcns.dao.mongo.util.MongoHelper;
import org.radarcns.integrationTest.util.RandomInput;
import org.radarcns.integrationTest.util.Utility;
import org.radarcns.util.AvroConverter;
import org.radarcns.util.RadarConverter;

/**
 * Created by francesco on 06/03/2017.
 */
public class SensorAppTest {

    private final String SERVER = "http://localhost:8080/";
    private final String PATH = "radar/api/";

    private static final String USER = "UserID_0";
    private static final String SOURCE = "SourceID_0";
    private static final SourceType SOURCE_TYPE = EMPATICA;
    private static final SensorType SENSOR_TYPE = HEART_RATE;
    private static final int SAMPLES = 10;

    @Test
    public void getRealtimeTest()
        throws IOException, IllegalAccessException, InstantiationException {
        String path = "sensor/avro/realTime/{sensor}/{stat}/{userID}/{sourceID}";
        path = path.replace("{sensor}", SENSOR_TYPE.name());
        path = path.replace("{stat}", COUNT.name());
        path = path.replace("{userID}", USER);
        path = path.replace("{sourceID}", SOURCE);

        Properties.getInstanceTest(this.getClass().getClassLoader().getResource(
            Properties.NAME_FILE).getPath());

        MongoClient client = Utility.getMongoClient();

        MongoCollection<Document> collection = MongoHelper.getCollection(client,
            HeartRateDAO.getInstance().getCollectionName(SOURCE_TYPE));

        List<Document> docs = RandomInput.getDocumentsRandom(USER, SOURCE, SOURCE_TYPE, SENSOR_TYPE,
            COUNT, SAMPLES, false);

        collection.insertMany(docs);

        Dataset expected = Utility.convertDocToDataset(singletonList(docs.get(docs.size() - 1)),
            RadarConverter.getMongoStat(COUNT), Unit.BEATS_PER_MIN, HeartRate.class);

        Dataset actual = null;

        Response response = Utility.makeRequest(SERVER + PATH + path);
        assertEquals(200, response.code());

        if (response.code() == 200) {
            actual = AvroConverter.avroByteToAvro(response.body().bytes(),
                    Dataset.getClassSchema());
        }

        assertEquals(expected, actual);

        dropAndClose(client);
    }

    @Test
    public void getAllByUserTest()
        throws IOException, IllegalAccessException, InstantiationException {
        String path = "sensor/avro/{sensor}/{stat}/{userID}/{sourceID}";
        path = path.replace("{sensor}", SENSOR_TYPE.name());
        path = path.replace("{stat}", COUNT.name());
        path = path.replace("{userID}", USER);
        path = path.replace("{sourceID}", SOURCE);

        Properties.getInstanceTest(this.getClass().getClassLoader().getResource(
            Properties.NAME_FILE).getPath());

        MongoClient client = Utility.getMongoClient();

        MongoCollection<Document> collection = MongoHelper.getCollection(client,
            HeartRateDAO.getInstance().getCollectionName(SOURCE_TYPE));

        List<Document> docs = RandomInput.getDocumentsRandom(USER, SOURCE, SOURCE_TYPE, SENSOR_TYPE,
            COUNT, SAMPLES, false);

        collection.insertMany(docs);

        Dataset expected = Utility.convertDocToDataset(docs, RadarConverter.getMongoStat(COUNT),
                Unit.BEATS_PER_MIN, HeartRate.class);

        Dataset actual = null;

        Response response = Utility.makeRequest(SERVER + PATH + path);
        assertEquals(200, response.code());

        if (response.code() == 200) {
            actual = AvroConverter.avroByteToAvro(response.body().bytes(),
                Dataset.getClassSchema());
        }

        assertEquals(expected, actual);

        dropAndClose(client);
    }

    @Test
    public void getRealtimeTest200()
        throws IOException, IllegalAccessException, InstantiationException {
        String path = "sensor/avro/{sensor}/{stat}/{userID}/{sourceID}/{start}/{end}";
        path = path.replace("{sensor}", SENSOR_TYPE.name());
        path = path.replace("{stat}", COUNT.name());
        path = path.replace("{userID}", USER);
        path = path.replace("{sourceID}", SOURCE);

        Properties.getInstanceTest(this.getClass().getClassLoader().getResource(
            Properties.NAME_FILE).getPath());

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

        Dataset expected = Utility.convertDocToDataset(docs.subList(index - 1, index + 2),
            RadarConverter.getMongoStat(COUNT), Unit.BEATS_PER_MIN, HeartRate.class);

        path = path.replace("{start}", String.valueOf(start));
        path = path.replace("{end}", String.valueOf(end));

        Dataset actual = null;

        Response response = Utility.makeRequest(SERVER + PATH + path);
        assertEquals(200, response.code());

        if (response.code() == 200) {
            actual = AvroConverter.avroByteToAvro(response.body().bytes(),
                Dataset.getClassSchema());
        }

        assertEquals(expected, actual);

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
        Utility.dropCollection(client, AndroidDAO.getInstance().getCollections());
        client.close();
    }

}
