package org.radarcns.integrationTest.testCase.listener;

import static org.junit.Assert.assertEquals;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import org.junit.Test;
import org.radarcns.config.Properties;
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
