package org.radarcns.integration.testcase.listener;

/*
 *  Copyright 2016 Kings College London and The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import static org.junit.Assert.assertEquals;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import org.junit.Test;
import org.radarcns.config.Properties;
import org.radarcns.integration.util.Utility;
import org.radarcns.listener.MongoDBContextListener;

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
