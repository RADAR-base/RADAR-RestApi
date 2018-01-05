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

package org.radarcns.dao;

import static org.junit.Assert.assertEquals;
import static org.radarcns.restapi.header.DescriptiveStatistic.COUNT;
import static org.radarcns.unit.config.TestCatalog.ANDROID;
import static org.radarcns.unit.config.TestCatalog.EMPATICA;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import java.util.List;
import org.bson.Document;
import org.junit.After;
import org.junit.Test;
import org.radarcns.catalogue.TimeWindow;
import org.radarcns.dao.mongo.util.MongoHelper;
import org.radarcns.integration.util.RandomInput;
import org.radarcns.integration.util.Utility;
import org.radarcns.restapi.source.Source;
import org.radarcns.restapi.subject.Cohort;
import org.radarcns.restapi.subject.Subject;

/**
 * UserDao Test.
 */
public class UserDaoTest {

    private static final String SUBJECT = "UserID_0";
    private static final String SOURCE = "SourceID_0";
    private static final String SOURCE_TYPE = EMPATICA;
    private static final String SENSOR_TYPE = "HEART_RATE";
    private static final TimeWindow TIME_FRAME = TimeWindow.TEN_SECOND;
    private static final int SAMPLES = 10;

    @Test
    public void findAllUserTest() throws Exception {
        MongoClient client = Utility.getMongoClient();

        MongoCollection<Document> collection = MongoHelper.getCollection(client,
                SensorDataAccessObject.getInstance(SENSOR_TYPE).getCollectionName(
                    SOURCE_TYPE, TIME_FRAME));

        collection.insertMany(RandomInput.getDocumentsRandom(SUBJECT, SOURCE, SOURCE_TYPE,
                SENSOR_TYPE, COUNT, TIME_FRAME, SAMPLES, false));

        Cohort cohort = SubjectDataAccessObject.getAllSubjects(client);

        assertEquals(1, cohort.getSubjects().size());
        assertEquals(1, cohort.getSubjects().get(0).getSources().size());

        dropAndClose(client);
    }

    @Test
    public void findAllUserTestDoubleSource() throws Exception {
        MongoClient client = Utility.getMongoClient();

        MongoCollection<Document> collection = MongoHelper.getCollection(client,
                SensorDataAccessObject.getInstance(SENSOR_TYPE).getCollectionName(
                    SOURCE_TYPE, TIME_FRAME));

        List<Document> docs = RandomInput.getDocumentsRandom(SUBJECT, SOURCE,
                SOURCE_TYPE, SENSOR_TYPE, COUNT, TIME_FRAME, SAMPLES, false);
        docs.addAll(RandomInput.getDocumentsRandom(SUBJECT, SOURCE.concat("XYZ1"),
                SOURCE_TYPE, SENSOR_TYPE, COUNT, TIME_FRAME, SAMPLES, false));
        collection.insertMany(docs);

        Cohort cohort = SubjectDataAccessObject.getAllSubjects(client);

        assertEquals(1, cohort.getSubjects().size());
        assertEquals(2, cohort.getSubjects().get(0).getSources().size());

        dropAndClose(client);
    }

    @Test
    public void findAllUserTestDoubleUser() throws Exception {
        MongoClient client = Utility.getMongoClient();

        // SUBJECT
        // SOURCE -> ANDROID
        Utility.insertMixedDocs(client,
                RandomInput.getRandomApplicationStatus(SUBJECT, SOURCE));

        MongoCollection<Document> collection = MongoHelper.getCollection(client,
                SensorDataAccessObject.getInstance(SENSOR_TYPE).getCollectionName(
                    SOURCE_TYPE, TIME_FRAME));
        // USER1
        // SOURCE1 -> EMPATICA
        collection.insertMany(RandomInput.getDocumentsRandom(SUBJECT.concat("1"),
                SOURCE.concat("1"), SOURCE_TYPE, SENSOR_TYPE, COUNT, TIME_FRAME,
                SAMPLES, false));
        // SUBJECT
        // SOURCE2 -> EMPATICA
        collection.insertMany(RandomInput.getDocumentsRandom(SUBJECT, SOURCE.concat("2"),
                SOURCE_TYPE, SENSOR_TYPE, COUNT, TIME_FRAME, SAMPLES, false));

        Cohort cohort = SubjectDataAccessObject.getAllSubjects(client);

        assertEquals(2, cohort.getSubjects().size());

        for (Subject patient : cohort.getSubjects()) {
            if (patient.getSubjectId().equals(SUBJECT)) {
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
