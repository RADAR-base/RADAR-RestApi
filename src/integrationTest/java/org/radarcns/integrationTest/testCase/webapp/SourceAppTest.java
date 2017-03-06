package org.radarcns.integrationTest.testCase.webapp;

import static org.junit.Assert.assertEquals;
import static org.radarcns.avro.restapi.header.DescriptiveStatistic.COUNT;
import static org.radarcns.avro.restapi.sensor.SensorType.HR;
import static org.radarcns.avro.restapi.source.SourceType.ANDROID;
import static org.radarcns.avro.restapi.source.SourceType.BIOVOTION;
import static org.radarcns.avro.restapi.source.SourceType.EMPATICA;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import java.io.IOException;
import java.util.List;
import okhttp3.Response;
import org.bson.Document;
import org.junit.After;
import org.junit.Test;
import org.radarcns.avro.restapi.sensor.Sensor;
import org.radarcns.avro.restapi.sensor.SensorSpecification;
import org.radarcns.avro.restapi.sensor.SensorType;
import org.radarcns.avro.restapi.source.Source;
import org.radarcns.avro.restapi.source.SourceSpecification;
import org.radarcns.avro.restapi.source.SourceSummary;
import org.radarcns.avro.restapi.source.SourceType;
import org.radarcns.avro.restapi.source.State;
import org.radarcns.avro.restapi.user.Patient;
import org.radarcns.config.Properties;
import org.radarcns.dao.SensorDataAccessObject;
import org.radarcns.dao.mongo.AndroidDAO;
import org.radarcns.dao.mongo.sensor.HeartRateDAO;
import org.radarcns.dao.mongo.util.MongoDAO;
import org.radarcns.dao.mongo.util.MongoHelper;
import org.radarcns.integrationTest.util.RandomInput;
import org.radarcns.integrationTest.util.Utility;
import org.radarcns.monitor.Monitors;
import org.radarcns.util.AvroConverter;

/**
 * Created by francesco on 06/03/2017.
 */
public class SourceAppTest {

    private final String SERVER = "http://localhost:8080/";
    private final String PATH = "radar/api/";

    private static final String USER = "UserID_0";
    private static final String SOURCE = "SourceID_0";
    private static final SourceType SOURCE_TYPE = EMPATICA;
    private static final SensorType SENSOR_TYPE = HR;
    private static final int SAMPLES = 10;

    @Test
    public void getStatusTest204() throws IOException {
        String path = "source/avro/state/{userID}/{sourceID}";
        path = path.replace("{userID}", USER);
        path = path.replace("{sourceID}", SOURCE);

        assertEquals(204, Utility.makeRequest(SERVER + PATH + path).code());
    }

    @Test
    public void getStatusTest200()
        throws IOException, IllegalAccessException, InstantiationException {
        Properties.getInstanceTest(this.getClass().getClassLoader().getResource(
            Properties.NAME_FILE).getPath());

        MongoClient client = Utility.getMongoClient();

        MongoDAO.writeSourceType(SOURCE, SOURCE_TYPE, client);

        String path = "source/avro/state/{userID}/{sourceID}";
        path = path.replace("{userID}", USER);
        path = path.replace("{sourceID}", SOURCE);

        Response response = Utility.makeRequest(SERVER + PATH + path);
        assertEquals(200, response.code());

        byte[] array = response.body().bytes();

        if (response.code() == 200) {
            Source actual = AvroConverter.avroByteToAvro(array, Source.getClassSchema());
            assertEquals(SOURCE, actual.getId());
            assertEquals(SOURCE_TYPE, actual.getType());

            SourceSummary summary = actual.getSummary();
            assertEquals(State.DISCONNECTED, summary.getState());
            assertEquals(1.0, summary.getMessageLoss(), 0.0);
            assertEquals(0, summary.getReceivedMessage(), 0.0);

            List<SensorSpecification> spec = Monitors.getInstance().getSpecification(
                    SourceType.EMPATICA).getSensors();

            for (Sensor sensor : summary.getSensors()) {
                assertEquals(State.DISCONNECTED, sensor.getState());
                assertEquals(1.0, sensor.getMessageLoss(), 0.0);
                assertEquals(0, sensor.getReceivedMessage(), 0.0);

                for (int i = 0; i < spec.size(); i++) {
                    if (spec.get(i).getName().name().equalsIgnoreCase(
                            sensor.getName().toString())) {
                        spec.remove(i);
                        break;
                    }
                }
            }

            assertEquals(0, spec.size());
        }

        dropAndClose(client);
    }

    @Test
    public void getSpecificationTest500() throws IOException {
        String path = "source/avro/specification/{sourceType}";
        path = path.replace("{sourceType}", BIOVOTION.toString());

        assertEquals(500, Utility.makeRequest(SERVER + PATH + path).code());
    }

    @Test
    public void getSpecificationTest200() throws IOException {
        String path = "source/avro/specification/{sourceType}";
        path = path.replace("{sourceType}", EMPATICA.toString());

        Response response = Utility.makeRequest(SERVER + PATH + path);
        assertEquals(200, response.code());

        SourceSpecification expected = Monitors.getInstance().getSpecification(SourceType.EMPATICA);
        List<SensorSpecification> listSensors = expected.getSensors();

        SourceSpecification actual = null;
        if (response.code() == 200) {
            actual = AvroConverter.avroByteToAvro(
                    response.body().bytes(), SourceSpecification.getClassSchema());
        }

        for (SensorSpecification sensorSpec : actual.getSensors()) {
            for (int i = 0; i < listSensors.size(); i++) {
                if (listSensors.get(i).getName().name().equalsIgnoreCase(
                    sensorSpec.getName().toString())) {
                    listSensors.remove(i);
                    break;
                }
            }
        }

        assertEquals(true, listSensors.isEmpty());
    }

    @Test
    public void getAllSourcesTest()
        throws IOException, IllegalAccessException, InstantiationException {
        String path = "source/avro/getAllSources/{userID}";
        path = path.replace("{userID}", USER);

        Properties.getInstanceTest(this.getClass().getClassLoader().getResource(
            Properties.NAME_FILE).getPath());

        MongoClient client = Utility.getMongoClient();

        MongoCollection<Document> collection = MongoHelper.getCollection(client,
            HeartRateDAO.getInstance().getCollectionName(SOURCE_TYPE));

        collection.insertMany(RandomInput.getDocumentsRandom(USER, SOURCE, SOURCE_TYPE, SENSOR_TYPE,
            COUNT, SAMPLES, false));
        Utility.insertMixedDocs(client,
            RandomInput.getRandomApplicationStatus(USER, SOURCE.concat("1")));

        Response response = Utility.makeRequest(SERVER + PATH + path);
        assertEquals(200, response.code());

        Patient actual = null;
        if (response.code() == 200) {
            actual = AvroConverter.avroByteToAvro(
                    response.body().bytes(), Patient.getClassSchema());
        }
        List<Source> listSource = actual.getSources();
        assertEquals(2, listSource.size());

        for (int i = 0; i < listSource.size(); i++) {
            if (listSource.get(i).getType().name().equalsIgnoreCase(ANDROID.name())) {
                listSource.remove(i);
                break;
            }
        }

        for (int i = 0; i < listSource.size(); i++) {
            if (listSource.get(i).getType().name().equalsIgnoreCase(EMPATICA.name())) {
                listSource.remove(i);
                break;
            }
        }
        assertEquals(0, listSource.size());

        dropAndClose(client);
    }

    @After
    public void dropAndClose() {
        Properties.getInstanceTest(this.getClass().getClassLoader().getResource(
                Properties.NAME_FILE).getPath());
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
