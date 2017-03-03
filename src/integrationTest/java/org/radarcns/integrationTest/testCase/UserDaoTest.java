package org.radarcns.integrationTest.testCase;

import static org.junit.Assert.assertEquals;
import static org.radarcns.avro.restapi.header.DescriptiveStatistic.COUNT;
import static org.radarcns.avro.restapi.sensor.SensorType.HR;
import static org.radarcns.avro.restapi.source.SourceType.EMPATICA;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoCollection;
import java.util.List;
import org.bson.Document;
import org.junit.Before;
import org.junit.Test;
import org.radarcns.avro.restapi.source.SourceType;
import org.radarcns.avro.restapi.user.Cohort;
import org.radarcns.config.Properties;
import org.radarcns.dao.mongo.UserDAO;
import org.radarcns.dao.mongo.sensor.HeartRateDAO;
import org.radarcns.dao.mongo.util.MongoHelper;
import org.radarcns.integrationTest.util.RandomInput;
import org.radarcns.integrationTest.util.Utility;
import org.radarcns.listner.MongoDBContextListener;
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
    private static final int SAMPLES = 10;

    @Before
    public void initCollections() throws Exception {
        Properties.getInstanceTest(this.getClass().getClassLoader().getResource(
            Properties.NAME_FILE).getPath());

        MongoClient client = Utility.getMongoClient();
        MongoCollection<Document> collection = MongoHelper.getCollection(client,
                HeartRateDAO.getInstance().getCollectionName(SOURCE_TYPE));

        List<Document> docs = RandomInput.getDocumentsRandom(USER, SOURCE, SOURCE_TYPE, HR,
            COUNT, SAMPLES);

        collection.insertMany(docs);

        client.close();
    }

    @Test
    public void findAllUserTest() throws Exception {
        Properties.getInstanceTest(this.getClass().getClassLoader().getResource(
            Properties.NAME_FILE).getPath());

        List<MongoCredential> credentials = Properties.getInstance().getMongoDbCredential();
        MongoClient client = new MongoClient(Properties.getInstance().getMongoHosts(),credentials);

        MongoDBContextListener.checkMongoConnection(client, credentials);
        Cohort cohort = UserDAO.findAllUsers(client);

        assertEquals(1, cohort.getPatients().size());
        assertEquals(1, cohort.getPatients().get(0).getSources().size());

        client.close();
    }








}
