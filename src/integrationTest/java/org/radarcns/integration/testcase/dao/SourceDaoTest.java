package org.radarcns.integration.testcase.dao;

import static com.mongodb.client.model.Filters.eq;
import static org.junit.Assert.assertEquals;
import static org.radarcns.avro.restapi.header.DescriptiveStatistic.COUNT;
import static org.radarcns.avro.restapi.sensor.SensorType.HEART_RATE;
import static org.radarcns.avro.restapi.source.SourceType.ANDROID;
import static org.radarcns.avro.restapi.source.SourceType.EMPATICA;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.junit.After;
import org.junit.Test;
import org.radarcns.avro.restapi.sensor.SensorType;
import org.radarcns.avro.restapi.source.SourceType;
import org.radarcns.config.Properties;
import org.radarcns.dao.SensorDataAccessObject;
import org.radarcns.dao.mongo.AndroidDAO;
import org.radarcns.dao.mongo.SourceDAO;
import org.radarcns.dao.mongo.sensor.HeartRateDAO;
import org.radarcns.dao.mongo.util.MongoHelper;
import org.radarcns.integration.util.RandomInput;
import org.radarcns.integration.util.Utility;

/**
 * UserDao Test.
 */
public class SourceDaoTest {

    //private static final Logger logger = LoggerFactory.getLogger(SourceDaoTest.class);

    private static final String USER = "UserID_0";
    private static final String SOURCE = "SourceID_0";
    private static final SourceType SOURCE_TYPE = EMPATICA;
    private static final SensorType SENSOR_TYPE = HEART_RATE;
    private static final int SAMPLES = 10;

    @Test
    public void test() throws Exception {
        Properties.getInstanceTest(this.getClass().getClassLoader().getResource(
            Properties.NAME_FILE).getPath());

        MongoClient client = Utility.getMongoClient();

        MongoCollection<Document> collection = MongoHelper.getCollection(client,
            HeartRateDAO.getInstance().getCollectionName(SOURCE_TYPE));

        collection.insertMany(RandomInput.getDocumentsRandom(USER, SOURCE, SOURCE_TYPE, SENSOR_TYPE,
            COUNT, SAMPLES, false));

        assertEquals(SOURCE_TYPE, SourceDAO.getSourceType(SOURCE, client));

        Utility.insertMixedDocs(client,
            RandomInput.getRandomApplicationStatus(USER, SOURCE.concat("1")));

        assertEquals(ANDROID, SourceDAO.getSourceType(SOURCE.concat("1"), client));

        assertEquals(2, SourceDAO.findAllSoucesByUser(USER, client).getSources().size());

        String extractedSourceId = null;
        String extractedSourceType = null;
        collection = MongoHelper.getCollection(client, MongoHelper.DEVICE_CATALOG);
        MongoCursor<Document> cursor = collection.find(Filters.and(eq(MongoHelper.ID, SOURCE)))
                .iterator();
        if (cursor.hasNext()) {
            Document doc = cursor.next();
            extractedSourceId = doc.getString(MongoHelper.ID);
            extractedSourceType = doc.getString(MongoHelper.SOURCE_TYPE);
        }
        assertEquals(SOURCE, extractedSourceId);
        assertEquals(EMPATICA.name(), extractedSourceType);

        extractedSourceId = null;
        extractedSourceType = null;
        collection = MongoHelper.getCollection(client, MongoHelper.DEVICE_CATALOG);
        cursor = collection.find(Filters.and(eq(MongoHelper.ID, SOURCE.concat("1")))).iterator();
        if (cursor.hasNext()) {
            Document doc = cursor.next();
            extractedSourceId = doc.getString(MongoHelper.ID);
            extractedSourceType = doc.getString(MongoHelper.SOURCE_TYPE);
        }
        assertEquals(SOURCE.concat("1"), extractedSourceId);
        assertEquals(ANDROID.name(), extractedSourceType);

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
