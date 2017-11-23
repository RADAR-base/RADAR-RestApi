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
import static org.radarcns.avro.restapi.sensor.SensorType.ACCELEROMETER;
import static org.radarcns.avro.restapi.sensor.SensorType.BATTERY;
import static org.radarcns.avro.restapi.sensor.SensorType.BLOOD_VOLUME_PULSE;
import static org.radarcns.avro.restapi.sensor.SensorType.ELECTRODERMAL_ACTIVITY;
import static org.radarcns.avro.restapi.sensor.SensorType.HEART_RATE;
import static org.radarcns.avro.restapi.sensor.SensorType.INTER_BEAT_INTERVAL;
import static org.radarcns.avro.restapi.sensor.SensorType.THERMOMETER;
import static org.radarcns.avro.restapi.source.SourceType.ANDROID;
import static org.radarcns.avro.restapi.source.SourceType.EMPATICA;
import static org.radarcns.webapp.util.BasePath.*;
import static org.radarcns.webapp.util.Parameter.STUDY_ID;
import static org.radarcns.webapp.util.Parameter.SUBJECT_ID;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response.Status;
import okhttp3.Response;
import org.bson.Document;
import org.junit.*;
import org.radarcns.avro.restapi.header.TimeFrame;
import org.radarcns.avro.restapi.sensor.Sensor;
import org.radarcns.avro.restapi.sensor.SensorType;
import org.radarcns.avro.restapi.source.Source;
import org.radarcns.avro.restapi.source.SourceSummary;
import org.radarcns.avro.restapi.source.SourceType;
import org.radarcns.avro.restapi.source.State;
import org.radarcns.avro.restapi.subject.Cohort;
import org.radarcns.avro.restapi.subject.Subject;
import org.radarcns.config.Properties;
import org.radarcns.dao.AndroidAppDataAccessObject;
import org.radarcns.dao.SensorDataAccessObject;
import org.radarcns.dao.mongo.util.MongoHelper;
import org.radarcns.integration.util.RandomInput;
import org.radarcns.integration.util.Utility;
import org.radarcns.util.AvroConverter;
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
        String path = SUBJECT + "/" + GET_ALL_SUBJECTS + "/{"
                + STUDY_ID + "}";
        path = path.replace("{" + STUDY_ID + "}", "0");

        LOGGER.info(path);

        assertEquals(Status.NO_CONTENT.getStatusCode(), Utility.makeRequest(
                Properties.getApiConfig().getApiUrl() + path, AVRO_BINARY).code());
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

        String path = SUBJECT + "/" + GET_ALL_SUBJECTS + "/{"
                + STUDY_ID + "}";
        path = path.replace("{" + STUDY_ID + "}", "0");

        LOGGER.info(path);

        Response response = Utility.makeRequest(Properties.getApiConfig().getApiUrl() + path,
                AVRO_BINARY);
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

    @Test
    public void getSubjectTest204() throws IOException {
        String path = SUBJECT + "/" + GET_SUBJECT + "/{" + SUBJECT_ID + "}";
        path = path.replace("{" + SUBJECT_ID + "}", "0");

        LOGGER.info(path);

        assertEquals(Status.NO_CONTENT.getStatusCode(), Utility.makeRequest(
                Properties.getApiConfig().getApiUrl() + path, AVRO_BINARY).code());
    }

    @Test
    public void getSubjectTest200()
        throws IOException, IllegalAccessException, InstantiationException, URISyntaxException {

        MongoClient client = Utility.getMongoClient();

        MongoCollection<Document> collection = MongoHelper.getCollection(client,
                SensorDataAccessObject.getInstance(SENSOR_TYPE).getCollectionName(
                    SOURCE_TYPE, TIME_FRAME));

        List<Document> randomInput = RandomInput.getDocumentsRandom(SUBJECT, SOURCE, SOURCE_TYPE,
                SENSOR_TYPE, COUNT, TIME_FRAME, SAMPLES, false);

        collection.insertMany(randomInput);

        String path = SUBJECT + "/" + GET_SUBJECT + "/{" + SUBJECT_ID + "}";
        path = path.replace("{" + SUBJECT_ID + "}", SUBJECT);

        LOGGER.info(path);

        Response response = Utility.makeRequest(Properties.getApiConfig().getApiUrl() + path,
                AVRO_BINARY);
        assertEquals(Status.OK.getStatusCode(), response.code());

        if (response.code() == Status.OK.getStatusCode()) {
            Subject actual = AvroConverter.avroByteToAvro(
                        response.body().bytes(), Subject.getClassSchema());

            Map<String, Sensor> sensorMap = new HashMap<>();
            sensorMap.put(INTER_BEAT_INTERVAL.name(),
                    new Sensor(INTER_BEAT_INTERVAL, State.DISCONNECTED, 0, 1.0));
            sensorMap.put(BATTERY.name(),
                    new Sensor(BATTERY, State.DISCONNECTED, 0, 1.0));
            sensorMap.put(HEART_RATE.name(),
                    new Sensor(HEART_RATE, State.DISCONNECTED, 0, 1.0));
            sensorMap.put(THERMOMETER.name(),
                    new Sensor(THERMOMETER, State.DISCONNECTED, 0, 1.0));
            sensorMap.put(ACCELEROMETER.name(),
                    new Sensor(ACCELEROMETER, State.DISCONNECTED, 0, 1.0));
            sensorMap.put(ELECTRODERMAL_ACTIVITY.name(),
                    new Sensor(ELECTRODERMAL_ACTIVITY, State.DISCONNECTED, 0, 1.0));
            sensorMap.put(BLOOD_VOLUME_PULSE.name(),
                    new Sensor(BLOOD_VOLUME_PULSE, State.DISCONNECTED, 0, 1.0));

            Subject expected = new Subject(SUBJECT, true,
                    Utility.getExpectedTimeFrame(Long.MAX_VALUE, Long.MIN_VALUE, randomInput),
                    Collections.singletonList(new Source(SOURCE, SOURCE_TYPE, new SourceSummary(
                            State.DISCONNECTED, 0, 1.0, sensorMap))));

            assertEquals(expected, actual);
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
