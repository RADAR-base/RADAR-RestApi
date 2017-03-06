package org.radarcns.integrationTest.testCase.webapp;

import static org.junit.Assert.assertEquals;
import static org.radarcns.avro.restapi.header.DescriptiveStatistic.COUNT;
import static org.radarcns.avro.restapi.sensor.SensorType.HR;
import static org.radarcns.avro.restapi.source.SourceType.ANDROID;
import static org.radarcns.avro.restapi.source.SourceType.EMPATICA;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import java.io.IOException;
import okhttp3.Response;
import org.bson.Document;
import org.junit.After;
import org.junit.Test;
import org.radarcns.avro.restapi.sensor.SensorType;
import org.radarcns.avro.restapi.source.Source;
import org.radarcns.avro.restapi.source.SourceType;
import org.radarcns.avro.restapi.user.Cohort;
import org.radarcns.avro.restapi.user.Patient;
import org.radarcns.config.Properties;
import org.radarcns.dao.SensorDataAccessObject;
import org.radarcns.dao.mongo.AndroidDAO;
import org.radarcns.dao.mongo.sensor.HeartRateDAO;
import org.radarcns.dao.mongo.util.MongoHelper;
import org.radarcns.integrationTest.util.RandomInput;
import org.radarcns.integrationTest.util.Utility;
import org.radarcns.util.AvroConverter;

/**
 * Created by francesco on 06/03/2017.
 */
public class UserAppTest {

    private final String SERVER = "http://localhost:8080/";
    private final String PATH = "radar/api/";

    private static final String USER = "UserID_0";
    private static final String SOURCE = "SourceID_0";
    private static final SourceType SOURCE_TYPE = EMPATICA;
    private static final SensorType SENSOR_TYPE = HR;
    private static final int SAMPLES = 10;

    @Test
    public void getAllPatientsTest204() throws IOException {
        String path = "user/avro/getAllPatients/{studyID}";
        path = path.replace("{studyID}", "0");

        assertEquals(204, Utility.makeRequest(SERVER + PATH + path).code());
    }

    @Test
    public void getAllPatientsTest200()
        throws IOException, IllegalAccessException, InstantiationException {
        Properties.getInstanceTest(this.getClass().getClassLoader().getResource(
            Properties.NAME_FILE).getPath());

        MongoClient client = Utility.getMongoClient();

        MongoCollection<Document> collection = MongoHelper.getCollection(client,
            HeartRateDAO.getInstance().getCollectionName(SOURCE_TYPE));
        collection.insertMany(RandomInput.getDocumentsRandom(USER, SOURCE, SOURCE_TYPE, SENSOR_TYPE,
            COUNT, SAMPLES, false));

        Utility.insertMixedDocs(client,
            RandomInput.getRandomApplicationStatus(USER.concat("1"), SOURCE.concat("1")));

        String path = "user/avro/getAllPatients/{studyID}";
        path = path.replace("{studyID}", "0");

        Response response = Utility.makeRequest(SERVER + PATH + path);
        assertEquals(200, response.code());

        byte[] array = response.body().bytes();

        if (response.code() == 200) {
            Cohort cohort = AvroConverter.avroByteToAvro(array, Cohort.getClassSchema());

            for (Patient patient : cohort.getPatients()) {
                if (patient.getUserId().equalsIgnoreCase(USER)) {
                    Source source = patient.getSources().get(0);
                    assertEquals(SOURCE_TYPE, source.getType());
                    assertEquals(SOURCE, source.getId());
                } else if (patient.getUserId().equalsIgnoreCase(USER.concat("1"))) {
                    Source source = patient.getSources().get(0);
                    assertEquals(ANDROID, source.getType());
                    assertEquals(SOURCE.concat("1"), source.getId());
                }
            }
        }

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
