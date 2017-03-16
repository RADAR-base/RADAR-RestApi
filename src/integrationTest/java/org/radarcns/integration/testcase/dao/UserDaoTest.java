package org.radarcns.integration.testcase.dao;

import static org.junit.Assert.assertEquals;
import static org.radarcns.avro.restapi.header.DescriptiveStatistic.COUNT;
import static org.radarcns.avro.restapi.sensor.SensorType.HEART_RATE;
import static org.radarcns.avro.restapi.source.SourceType.ANDROID;
import static org.radarcns.avro.restapi.source.SourceType.EMPATICA;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import java.util.List;
import org.bson.Document;
import org.junit.After;
import org.junit.Test;
import org.radarcns.avro.restapi.sensor.SensorType;
import org.radarcns.avro.restapi.source.Source;
import org.radarcns.avro.restapi.source.SourceType;
import org.radarcns.avro.restapi.user.Cohort;
import org.radarcns.avro.restapi.user.Patient;
import org.radarcns.config.Properties;
import org.radarcns.dao.mongo.AndroidDAO;
import org.radarcns.dao.mongo.UserDAO;
import org.radarcns.dao.mongo.sensor.HeartRateDAO;
import org.radarcns.dao.mongo.util.MongoHelper;
import org.radarcns.integration.util.RandomInput;
import org.radarcns.integration.util.Utility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * UserDao Test.
 */
public class UserDaoTest {

    private static final Logger logger = LoggerFactory.getLogger(UserDaoTest.class);

    private static final String USER = "UserID_0";
    private static final String SOURCE = "SourceID_0";
    private static final SourceType SOURCE_TYPE = EMPATICA;
    private static final SensorType SENSOR_TYPE = HEART_RATE;
    private static final int SAMPLES = 10;

    @Test
    public void findAllUserTest() throws Exception {
        Properties.getInstanceTest(this.getClass().getClassLoader().getResource(
            Properties.NAME_FILE).getPath());

        MongoClient client = Utility.getMongoClient();

        MongoCollection<Document> collection = MongoHelper.getCollection(client,
            HeartRateDAO.getInstance().getCollectionName(SOURCE_TYPE));

        collection.insertMany(RandomInput.getDocumentsRandom(USER, SOURCE, SOURCE_TYPE, SENSOR_TYPE,
            COUNT, SAMPLES, false));

        Cohort cohort = UserDAO.findAllUsers(client);

        assertEquals(1, cohort.getPatients().size());
        assertEquals(1, cohort.getPatients().get(0).getSources().size());

        dropAndClose(client);
    }

    @Test
    public void findAllUserTestDoubleSource() throws Exception {
        Properties.getInstanceTest(this.getClass().getClassLoader().getResource(
            Properties.NAME_FILE).getPath());

        MongoClient client = Utility.getMongoClient();
        MongoCollection<Document> collection = MongoHelper.getCollection(client,
            HeartRateDAO.getInstance().getCollectionName(SOURCE_TYPE));

        List<Document> docs = RandomInput.getDocumentsRandom(USER, SOURCE,
            SOURCE_TYPE, SENSOR_TYPE, COUNT, SAMPLES, false);
        docs.addAll(RandomInput.getDocumentsRandom(USER, SOURCE.concat("1"),
            SOURCE_TYPE, SENSOR_TYPE, COUNT, SAMPLES, false));
        collection.insertMany(docs);

        Cohort cohort = UserDAO.findAllUsers(client);

        assertEquals(1, cohort.getPatients().size());
        assertEquals(2, cohort.getPatients().get(0).getSources().size());

        dropAndClose(client);
    }

    @Test
    public void findAllUserTestDoubleUser() throws Exception {
        Properties.getInstanceTest(this.getClass().getClassLoader().getResource(
                Properties.NAME_FILE).getPath());

        MongoClient client = Utility.getMongoClient();

        // USER
        // SOURCE -> ANDROID
        Utility.insertMixedDocs(client,
            RandomInput.getRandomApplicationStatus(USER, SOURCE));

        MongoCollection<Document> collection = MongoHelper.getCollection(client,
            HeartRateDAO.getInstance().getCollectionName(SOURCE_TYPE));
        // USER1
        // SOURCE1 -> EMPATICA
        collection.insertMany(RandomInput.getDocumentsRandom(USER.concat("1"), SOURCE.concat("1"),
                SOURCE_TYPE, SENSOR_TYPE, COUNT, SAMPLES, false));
        // USER
        // SOURCE2 -> EMPATICA
        collection.insertMany(RandomInput.getDocumentsRandom(USER, SOURCE.concat("2"), SOURCE_TYPE,
            SENSOR_TYPE, COUNT, SAMPLES, false));

        Cohort cohort = UserDAO.findAllUsers(client);

        assertEquals(2, cohort.getPatients().size());

        for (Patient patient : cohort.getPatients()) {
            if (patient.getUserId().equals(USER)) {
                assertEquals(2, patient.getSources().size());
                for (Source temp : patient.getSources()) {
                    if (temp.getId().equals(SOURCE)) {
                        assertEquals(ANDROID, temp.getType());
                    } else if (temp.getId().equals(SOURCE.concat("2"))) {
                        assertEquals(EMPATICA, temp.getType());
                    }
                }
                break;
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
        Utility.dropCollection(client, HeartRateDAO.getInstance().getCollectionName(SOURCE_TYPE));
        Utility.dropCollection(client, AndroidDAO.getInstance().getCollections());
        client.close();
    }
}
