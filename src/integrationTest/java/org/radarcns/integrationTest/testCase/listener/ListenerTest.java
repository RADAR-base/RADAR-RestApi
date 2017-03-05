package org.radarcns.integrationTest.testCase.listener;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.radarcns.avro.restapi.header.DescriptiveStatistic.COUNT;
import static org.radarcns.avro.restapi.sensor.SensorType.HR;
import static org.radarcns.avro.restapi.source.SourceType.ANDROID;
import static org.radarcns.avro.restapi.source.SourceType.EMPATICA;
import static org.radarcns.integrationTest.util.RandomInput.getRandomIp;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import java.net.ConnectException;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.bson.Document;
import org.junit.After;
import org.junit.Test;
import org.radarcns.avro.restapi.app.Application;
import org.radarcns.avro.restapi.app.ServerStatus;
import org.radarcns.avro.restapi.source.SourceType;
import org.radarcns.config.Properties;
import org.radarcns.dao.mongo.AndroidDAO;
import org.radarcns.dao.mongo.sensor.HeartRateDAO;
import org.radarcns.dao.mongo.util.MongoHelper;
import org.radarcns.integrationTest.util.RandomInput;
import org.radarcns.integrationTest.util.Utility;
import org.radarcns.listner.MongoDBContextListener;

/**
 * MongoDBContextListener Test.
 */
public class ListenerTest {

    //private static final Logger logger = LoggerFactory.getLogger(ListenerTest.class);

    @Test
    public void testConnection() throws Exception {
        Properties.getInstanceTest(this.getClass().getClassLoader().getResource(
            Properties.NAME_FILE).getPath());

        MongoClient client = Utility.getMongoClient();

        assertEquals(true, MongoDBContextListener.checkMongoConnection(client));

        client = new MongoClient(new ServerAddress("localhosts", 27017),
                client.getCredentialsList());
        assertEquals(false, MongoDBContextListener.checkMongoConnection(client));
    }

}
