/*
 * Copyright 2016 King's College London and The Hyve
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

package org.radarcns.listener;

import static org.junit.Assert.assertEquals;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import org.junit.Test;
import org.radarcns.integration.util.Utility;

/**
 * MongoDbContextListener Test.
 */
public class ListenerTest {

    @Test
    public void testConnection() throws Exception {
        MongoClient client = Utility.getMongoClient();

        assertEquals(true, MongoDbContextListener.checkMongoConnection(client));

        client = new MongoClient(new ServerAddress("localhosts", 27017),
                client.getCredentialsList().get(0), client.getMongoClientOptions());
        assertEquals(false, MongoDbContextListener.checkMongoConnection(client));
    }

}
