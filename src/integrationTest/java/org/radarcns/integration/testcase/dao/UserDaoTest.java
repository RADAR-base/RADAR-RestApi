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
import org.radarcns.avro.restapi.header.TimeFrame;
import org.radarcns.avro.restapi.sensor.SensorType;
import org.radarcns.avro.restapi.source.Source;
import org.radarcns.avro.restapi.source.SourceType;
import org.radarcns.avro.restapi.user.Cohort;
import org.radarcns.avro.restapi.user.Patient;
import org.radarcns.dao.AndroidAppDataAccessObject;
import org.radarcns.dao.SensorDataAccessObject;
import org.radarcns.dao.UserDataAccessObject;
import org.radarcns.dao.mongo.util.MongoHelper;
import org.radarcns.integration.util.RandomInput;
import org.radarcns.integration.util.Utility;

/**
 * UserDao Test.
 */
public class UserDaoTest {

    private static final String USER = "UserID_0";
    private static final String SOURCE = "SourceID_0";
    private static final SourceType SOURCE_TYPE = EMPATICA;
    private static final SensorType SENSOR_TYPE = HEART_RATE;
    private static final TimeFrame TIME_FRAME = TimeFrame.TEN_SECOND;
    private static final int SAMPLES = 10;

    @Test
    public void findAllUserTest() throws Exception {
        MongoClient client = Utility.getMongoClient();

        MongoCollection<Document> collection = MongoHelper.getCollection(client,
                SensorDataAccessObject.getInstance(SENSOR_TYPE).getCollectionName(
                    SOURCE_TYPE, TIME_FRAME));

        collection.insertMany(RandomInput.getDocumentsRandom(USER, SOURCE, SOURCE_TYPE, SENSOR_TYPE,
                COUNT, SAMPLES, false));

        Cohort cohort = UserDataAccessObject.findAllUsers(client);

        assertEquals(1, cohort.getPatients().size());
        assertEquals(1, cohort.getPatients().get(0).getSources().size());

        dropAndClose(client);
    }

    @Test
    public void findAllUserTestDoubleSource() throws Exception {
        MongoClient client = Utility.getMongoClient();

        MongoCollection<Document> collection = MongoHelper.getCollection(client,
                SensorDataAccessObject.getInstance(SENSOR_TYPE).getCollectionName(
                    SOURCE_TYPE, TIME_FRAME));

        List<Document> docs = RandomInput.getDocumentsRandom(USER, SOURCE,
                SOURCE_TYPE, SENSOR_TYPE, COUNT, SAMPLES, false);
        docs.addAll(RandomInput.getDocumentsRandom(USER, SOURCE.concat("1"),
                SOURCE_TYPE, SENSOR_TYPE, COUNT, SAMPLES, false));
        collection.insertMany(docs);

        Cohort cohort = UserDataAccessObject.findAllUsers(client);

        assertEquals(1, cohort.getPatients().size());
        assertEquals(2, cohort.getPatients().get(0).getSources().size());

        dropAndClose(client);
    }

    @Test
    public void findAllUserTestDoubleUser() throws Exception {
        MongoClient client = Utility.getMongoClient();

        // USER
        // SOURCE -> ANDROID
        Utility.insertMixedDocs(client,
                RandomInput.getRandomApplicationStatus(USER, SOURCE));

        MongoCollection<Document> collection = MongoHelper.getCollection(client,
                SensorDataAccessObject.getInstance(SENSOR_TYPE).getCollectionName(
                    SOURCE_TYPE, TIME_FRAME));
        // USER1
        // SOURCE1 -> EMPATICA
        collection.insertMany(RandomInput.getDocumentsRandom(USER.concat("1"), SOURCE.concat("1"),
                SOURCE_TYPE, SENSOR_TYPE, COUNT, SAMPLES, false));
        // USER
        // SOURCE2 -> EMPATICA
        collection.insertMany(RandomInput.getDocumentsRandom(USER, SOURCE.concat("2"), SOURCE_TYPE,
                SENSOR_TYPE, COUNT, SAMPLES, false));

        Cohort cohort = UserDataAccessObject.findAllUsers(client);

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

    /** Drops all used collections to bring the database back to the initial state, and close the
     *      database connection.
     **/
    public void dropAndClose(MongoClient client) {
        Utility.dropCollection(client, MongoHelper.DEVICE_CATALOG);
        Utility.dropCollection(client, SensorDataAccessObject.getInstance(
                SENSOR_TYPE).getCollectionName(SOURCE_TYPE, TIME_FRAME));
        Utility.dropCollection(client, AndroidAppDataAccessObject.getInstance().getCollections());
        client.close();
    }
}
