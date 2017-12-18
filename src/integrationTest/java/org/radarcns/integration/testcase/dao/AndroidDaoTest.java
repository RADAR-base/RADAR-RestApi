package org.radarcns.integration.testcase.dao;

/*
 * Copyright 2017 King's College London and The Hyve
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

import static junit.framework.TestCase.assertEquals;
import static org.radarcns.restapi.header.DescriptiveStatistic.COUNT;
import static org.radarcns.integration.util.RandomInput.getRandomIpAddress;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import java.util.concurrent.ThreadLocalRandom;
import org.bson.Document;
import org.junit.After;
import org.junit.Test;
import org.radarcns.catalogue.TimeWindow;
import org.radarcns.monitor.application.ServerStatus;
import org.radarcns.restapi.app.Application;
import org.radarcns.dao.AndroidAppDataAccessObject;
import org.radarcns.dao.SensorDataAccessObject;
import org.radarcns.dao.mongo.util.MongoHelper;
import org.radarcns.integration.util.RandomInput;
import org.radarcns.integration.util.Utility;

/**
 * AndroidDao Test.
 */
public class AndroidDaoTest {

    //private static final Logger logger = LoggerFactory.getLogger(SourceDaoTest.class);

    private static final String SUBJECT = "UserID_0";
    private static final String SOURCE = "SourceID_0";
    private static final String SOURCE_TYPE = "EMPATICA";
    private static final String SENSOR_TYPE = "HEART_RATE";
    private static final TimeWindow TIME_FRAME = TimeWindow.TEN_SECOND;
    private static final int SAMPLES = 10;

    @Test
    public void testStatus() throws Exception {
        MongoClient client = Utility.getMongoClient();

        String ipAdress = getRandomIpAddress();
        ServerStatus serverStatus = ServerStatus.values()[
                ThreadLocalRandom.current().nextInt(0, ServerStatus.values().length)];
        Double uptime = ThreadLocalRandom.current().nextDouble();
        int recordsCached = ThreadLocalRandom.current().nextInt();
        int recordsSent = ThreadLocalRandom.current().nextInt();
        int recordsUnsent = ThreadLocalRandom.current().nextInt();

        Utility.insertMixedDocs(client,
                RandomInput.getRandomApplicationStatus(SUBJECT, SOURCE, ipAdress, serverStatus,
                    uptime, recordsCached, recordsSent, recordsUnsent));

        Application application = new Application(ipAdress, uptime, serverStatus, recordsCached,
                recordsSent, recordsUnsent);

        assertEquals(application,
                AndroidAppDataAccessObject.getInstance().getStatus(SUBJECT, SOURCE, client));

        dropAndClose(client);
    }

    @Test
    public void testFindAllUser() throws Exception {
        MongoClient client = Utility.getMongoClient();

        Utility.insertMixedDocs(client,
                RandomInput.getRandomApplicationStatus(SUBJECT, SOURCE));

        Utility.insertMixedDocs(client, RandomInput.getRandomApplicationStatus(
                SUBJECT.concat("1"), SOURCE.concat("1")));

        assertEquals(2,
                AndroidAppDataAccessObject.getInstance().findAllUser(client).size());

        dropAndClose(client);
    }

    @Test
    public void testFindAllSoucesByUser() throws Exception {
        MongoClient client = Utility.getMongoClient();

        Utility.insertMixedDocs(client,
                RandomInput.getRandomApplicationStatus(SUBJECT, SOURCE));

        Utility.insertMixedDocs(client, RandomInput.getRandomApplicationStatus(
                SUBJECT, SOURCE.concat("1")));

        assertEquals(2,
                AndroidAppDataAccessObject.getInstance().findAllSourcesBySubject(SUBJECT,
                        client).size());

        dropAndClose(client);
    }

    @Test
    public void testFindSourceType() throws Exception {
        MongoClient client = Utility.getMongoClient();

        Utility.insertMixedDocs(client,
                RandomInput.getRandomApplicationStatus(SUBJECT, SOURCE));

        MongoCollection<Document> collection = MongoHelper.getCollection(client,
                SensorDataAccessObject.getInstance(SENSOR_TYPE).getCollectionName(
                    SOURCE_TYPE, TIME_FRAME));
        collection.insertMany(RandomInput.getDocumentsRandom(SUBJECT, SOURCE.concat("1"),
                SOURCE_TYPE, SENSOR_TYPE, COUNT, TIME_FRAME, SAMPLES, false));

        assertEquals("ANDROID",
                AndroidAppDataAccessObject.getInstance().findSourceType(SOURCE, client));

        assertEquals(null,
                AndroidAppDataAccessObject.getInstance().findSourceType(SOURCE.concat("1"),
                    client));

        dropAndClose(client);
    }

    @After
    public void dropAndClose() throws Exception {
        dropAndClose(Utility.getMongoClient());
    }

    /** Drops all used collections to bring the database back to the initial state, and close the
     *      database connection.
     **/
    public void dropAndClose(MongoClient client) {
        Utility.dropCollection(client, MongoHelper.DEVICE_CATALOG);
        Utility.dropCollection(client, AndroidAppDataAccessObject.getInstance().getCollections());

        client.close();
    }
}
