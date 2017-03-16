package org.radarcns.integration.testcase.dao;

import static org.junit.Assert.assertEquals;
import static org.radarcns.avro.restapi.header.DescriptiveStatistic.COUNT;
import static org.radarcns.avro.restapi.sensor.SensorType.HEART_RATE;
import static org.radarcns.avro.restapi.source.SourceType.ANDROID;
import static org.radarcns.avro.restapi.source.SourceType.EMPATICA;
import static org.radarcns.integration.util.RandomInput.getRandomIp;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import java.util.concurrent.ThreadLocalRandom;
import org.bson.Document;
import org.junit.After;
import org.junit.Test;
import org.radarcns.avro.restapi.app.Application;
import org.radarcns.avro.restapi.app.ServerStatus;
import org.radarcns.avro.restapi.sensor.SensorType;
import org.radarcns.avro.restapi.source.SourceType;
import org.radarcns.config.Properties;
import org.radarcns.dao.mongo.AndroidDAO;
import org.radarcns.dao.mongo.sensor.HeartRateDAO;
import org.radarcns.dao.mongo.util.MongoHelper;
import org.radarcns.integration.util.RandomInput;
import org.radarcns.integration.util.Utility;

/**
 * UserDao Test.
 */
public class AndroidDaoTest {

    //private static final Logger logger = LoggerFactory.getLogger(SourceDaoTest.class);

    private static final String USER = "UserID_0";
    private static final String SOURCE = "SourceID_0";
    private static final SourceType SOURCE_TYPE = EMPATICA;
    private static final SensorType SENSOR_TYPE = HEART_RATE;
    private static final int SAMPLES = 10;

    @Test
    public void testStatus() throws Exception {
        Properties.getInstanceTest(this.getClass().getClassLoader().getResource(
            Properties.NAME_FILE).getPath());

        MongoClient client = Utility.getMongoClient();

        String ipAdress = getRandomIp();
        ServerStatus serverStatus = ServerStatus.values()[
            ThreadLocalRandom.current().nextInt(0, ServerStatus.values().length)];
        Double uptime = ThreadLocalRandom.current().nextDouble();
        int recordsCached = ThreadLocalRandom.current().nextInt();
        int recordsSent = ThreadLocalRandom.current().nextInt();
        int recordsUnsent = ThreadLocalRandom.current().nextInt();

        Utility.insertMixedDocs(client,
            RandomInput.getRandomApplicationStatus(USER, SOURCE, ipAdress, serverStatus, uptime,
                    recordsCached, recordsSent, recordsUnsent));

        Application application = new Application(ipAdress, uptime, serverStatus, recordsCached,
            recordsSent, recordsUnsent);

        assertEquals(application, AndroidDAO.getInstance().getStatus(USER, SOURCE, client));

//        findAllSoucesByUser
//            findSourceType

        dropAndClose(client);
    }

    @Test
    public void testFindAllUser() throws Exception {
        Properties.getInstanceTest(this.getClass().getClassLoader().getResource(
            Properties.NAME_FILE).getPath());

        MongoClient client = Utility.getMongoClient();

        Utility.insertMixedDocs(client,
            RandomInput.getRandomApplicationStatus(USER, SOURCE));

        Utility.insertMixedDocs(client, RandomInput.getRandomApplicationStatus(
                USER.concat("1"), SOURCE.concat("1")));

        assertEquals(2, AndroidDAO.getInstance().findAllUser(client).size());

        dropAndClose(client);
    }

    @Test
    public void testFindAllSoucesByUser() throws Exception {
        Properties.getInstanceTest(this.getClass().getClassLoader().getResource(
            Properties.NAME_FILE).getPath());

        MongoClient client = Utility.getMongoClient();

        Utility.insertMixedDocs(client,
            RandomInput.getRandomApplicationStatus(USER, SOURCE));

        Utility.insertMixedDocs(client, RandomInput.getRandomApplicationStatus(
            USER, SOURCE.concat("1")));

        assertEquals(2, AndroidDAO.getInstance().findAllSoucesByUser(USER, client).size());

        dropAndClose(client);
    }

    @Test
    public void testFindSourceType() throws Exception {
        Properties.getInstanceTest(this.getClass().getClassLoader().getResource(
                Properties.NAME_FILE).getPath());

        MongoClient client = Utility.getMongoClient();

        Utility.insertMixedDocs(client,
                RandomInput.getRandomApplicationStatus(USER, SOURCE));

        MongoCollection<Document> collection = MongoHelper.getCollection(client,
                HeartRateDAO.getInstance().getCollectionName(SOURCE_TYPE));
        collection.insertMany(RandomInput.getDocumentsRandom(USER, SOURCE.concat("1"), SOURCE_TYPE,
                SENSOR_TYPE, COUNT, SAMPLES, false));

        assertEquals(ANDROID, AndroidDAO.getInstance().findSourceType(SOURCE, client));

        assertEquals(null,
                AndroidDAO.getInstance().findSourceType(SOURCE.concat("1"), client));

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
        Utility.dropCollection(client, AndroidDAO.getInstance().getCollections());

        client.close();
    }
}
