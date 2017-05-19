package org.radarcns.integration.testcase.webapp;

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

import static org.junit.Assert.assertEquals;
import static org.radarcns.avro.restapi.header.DescriptiveStatistic.COUNT;
import static org.radarcns.avro.restapi.sensor.SensorType.HEART_RATE;
import static org.radarcns.avro.restapi.source.SourceType.ANDROID;
import static org.radarcns.avro.restapi.source.SourceType.EMPATICA;
import static org.radarcns.webapp.util.BasePath.AVRO;
import static org.radarcns.webapp.util.BasePath.GET_ALL_SUBJECTS;
import static org.radarcns.webapp.util.Parameter.STUDY_ID;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import java.io.IOException;
import java.net.URISyntaxException;
import javax.ws.rs.core.Response.Status;
import okhttp3.Response;
import org.bson.Document;
import org.junit.After;
import org.junit.Test;
import org.radarcns.avro.restapi.header.TimeFrame;
import org.radarcns.avro.restapi.sensor.SensorType;
import org.radarcns.avro.restapi.source.Source;
import org.radarcns.avro.restapi.source.SourceType;
import org.radarcns.avro.restapi.subject.Cohort;
import org.radarcns.avro.restapi.subject.Subject;
import org.radarcns.config.Properties;
import org.radarcns.dao.AndroidAppDataAccessObject;
import org.radarcns.dao.SensorDataAccessObject;
import org.radarcns.dao.mongo.util.MongoHelper;
import org.radarcns.integration.util.RandomInput;
import org.radarcns.integration.util.Utility;
import org.radarcns.util.AvroConverter;
import org.radarcns.webapp.util.BasePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubjectEndPointTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubjectEndPointTest.class);

    private static final String SUBJECT = "UserID_0";
    private static final String SOURCE = "SourceID_0";
    private static final SourceType SOURCE_TYPE = EMPATICA;
    private static final SensorType SENSOR_TYPE = HEART_RATE;
    private static final TimeFrame TIME_FRAME = TimeFrame.TEN_SECOND;
    private static final int SAMPLES = 10;

    @Test
    public void getAllSubjectsTest204() throws IOException {
        String path = BasePath.SUBJECT + "/" + AVRO + "/" + GET_ALL_SUBJECTS + "/{"
                + STUDY_ID + "}";
        path = path.replace("{" + STUDY_ID + "}", "0");

        LOGGER.info(path);

        assertEquals(Status.NO_CONTENT.getStatusCode(), Utility.makeRequest(
                Properties.getApiConfig().getApiUrl() + path).code());
    }

    @Test
    public void getAllSubjectsTest200()
            throws IOException, IllegalAccessException, InstantiationException, URISyntaxException {

        MongoClient client = Utility.getMongoClient();

        MongoCollection<Document> collection = MongoHelper.getCollection(client,
                SensorDataAccessObject.getInstance(SENSOR_TYPE).getCollectionName(
                    SOURCE_TYPE, TIME_FRAME));
        collection.insertMany(RandomInput.getDocumentsRandom(SUBJECT, SOURCE, SOURCE_TYPE,
                SENSOR_TYPE, COUNT, TIME_FRAME, SAMPLES, false));

        Utility.insertMixedDocs(client,
                RandomInput.getRandomApplicationStatus(SUBJECT.concat("1"), SOURCE.concat("1")));

        String path = BasePath.SUBJECT + "/" + AVRO + "/" + GET_ALL_SUBJECTS + "/{"
                + STUDY_ID + "}";
        path = path.replace("{" + STUDY_ID + "}", "0");

        LOGGER.info(path);

        Response response = Utility.makeRequest(Properties.getApiConfig().getApiUrl() + path);
        assertEquals(Status.OK.getStatusCode(), response.code());

        byte[] array = response.body().bytes();

        if (response.code() == Status.OK.getStatusCode()) {
            Cohort cohort = AvroConverter.avroByteToAvro(array, Cohort.getClassSchema());

            for (Subject patient : cohort.getSubjects()) {
                if (patient.getSubjectId().equalsIgnoreCase(SUBJECT)) {
                    Source source = patient.getSources().get(0);
                    assertEquals(SOURCE_TYPE, source.getType());
                    assertEquals(SOURCE, source.getId());
                } else if (patient.getSubjectId().equalsIgnoreCase(SUBJECT.concat("1"))) {
                    Source source = patient.getSources().get(0);
                    assertEquals(ANDROID, source.getType());
                    assertEquals(SOURCE.concat("1"), source.getId());
                }
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
        Utility.dropCollection(client,
                SensorDataAccessObject.getInstance(SENSOR_TYPE).getCollectionName(
                    SOURCE_TYPE, TIME_FRAME));
        Utility.dropCollection(client, AndroidAppDataAccessObject.getInstance().getCollections());
        client.close();
    }

}
