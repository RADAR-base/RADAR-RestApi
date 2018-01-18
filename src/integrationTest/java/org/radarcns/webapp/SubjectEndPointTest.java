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

package org.radarcns.webapp;

import static java.util.function.Function.identity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.radarcns.restapi.header.DescriptiveStatistic.COUNT;
import static org.radarcns.webapp.util.BasePath.GET_ALL_SUBJECTS;
import static org.radarcns.webapp.util.BasePath.GET_SUBJECT;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.ws.rs.core.Response.Status;
import okhttp3.Response;
import org.bson.Document;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.radarcns.catalogue.TimeWindow;
import org.radarcns.dao.AndroidAppDataAccessObject;
import org.radarcns.dao.SensorDataAccessObject;
import org.radarcns.dao.mongo.util.MongoHelper;
import org.radarcns.integration.util.ApiClient;
import org.radarcns.integration.util.RandomInput;
import org.radarcns.integration.util.RestApiDetails;
import org.radarcns.integration.util.Utility;
import org.radarcns.restapi.source.Sensor;
import org.radarcns.restapi.source.Source;
import org.radarcns.restapi.source.SourceSummary;
import org.radarcns.restapi.source.States;
import org.radarcns.restapi.subject.Cohort;
import org.radarcns.restapi.subject.Subject;
import org.radarcns.util.RadarConverter;
import org.radarcns.webapp.util.BasePath;

public class SubjectEndPointTest {
    private static final String SUBJECT = "sub-1";
    private static final String SOURCE = "SourceID_0";
    private static final String STUDY = "0";
    private static final String SOURCE_TYPE = org.radarcns.config.TestCatalog.EMPATICA;
    private static final String SENSOR_TYPE = "HEART_RATE";
    private static final TimeWindow TIME_WINDOW = TimeWindow.TEN_SECOND;
    private static final int SAMPLES = 10;

    @Rule
    public final ApiClient apiClient = new ApiClient(
            RestApiDetails.getRestApiClientDetails().getApplicationConfig().getUrlString()
                    + BasePath.SUBJECT + '/');

    @Test
    public void getAllSubjectsTest204() throws IOException {
        try (Response response = apiClient.request(GET_ALL_SUBJECTS + "/" + STUDY, APPLICATION_JSON,
                Status.NO_CONTENT)) {
            assertNotNull(response);
        }
    }

    @Test
    public void getAllSubjectsTest200()
            throws IOException, ReflectiveOperationException {

        MongoClient client = Utility.getMongoClient();

        MongoCollection<Document> collection = MongoHelper.getCollection(client,
                SensorDataAccessObject.getInstance(SENSOR_TYPE).getCollectionName(
                    SOURCE_TYPE, TIME_WINDOW));
        collection.insertMany(RandomInput.getDocumentsRandom(SUBJECT, SOURCE, SOURCE_TYPE,
                SENSOR_TYPE, COUNT, TIME_WINDOW, SAMPLES, false));

        Utility.insertMixedDocs(client,
                RandomInput.getRandomApplicationStatus(SUBJECT.concat("1"), SOURCE.concat("1")));

        Cohort cohort = RadarConverter.readerFor(Cohort.class).readValue(apiClient.requestString(
                GET_ALL_SUBJECTS + "/" + STUDY, APPLICATION_JSON, Status.OK));

        for (Subject patient : cohort.getSubjects()) {
            if (patient.getSubjectId().equalsIgnoreCase(SUBJECT)) {
                Source source = patient.getSources().get(0);
                assertEquals(SOURCE_TYPE, source.getType());
                assertEquals(SOURCE, source.getId());
            } else if (patient.getSubjectId().equalsIgnoreCase(SUBJECT.concat("1"))) {
                Source source = patient.getSources().get(0);
                assertEquals(org.radarcns.config.TestCatalog.ANDROID, source.getType());
                assertEquals(SOURCE.concat("1"), source.getId());
            }
        }

        dropAndClose(client);
    }

    @Test
    public void getSubjectTest204() throws IOException {
        try (Response response = apiClient.request(
                GET_SUBJECT + '/' + SUBJECT, APPLICATION_JSON, Status.NO_CONTENT)) {
            assertNotNull(response);
        }
    }

    @Test
    public void getSubjectTest200()
            throws IOException, ReflectiveOperationException, URISyntaxException {

        MongoClient client = Utility.getMongoClient();

        MongoCollection<Document> collection = MongoHelper.getCollection(client,
                SensorDataAccessObject.getInstance(SENSOR_TYPE).getCollectionName(
                    SOURCE_TYPE, TIME_WINDOW));

        List<Document> randomInput = RandomInput.getDocumentsRandom(SUBJECT, SOURCE, SOURCE_TYPE,
                SENSOR_TYPE, COUNT, TIME_WINDOW, SAMPLES, false);

        collection.insertMany(randomInput);

        Subject actual = apiClient.requestAvro(
                GET_SUBJECT + '/' + SUBJECT, Subject.class, Status.OK);

        Map<String, Sensor> sensorMap = Arrays.stream(
                new String[] {
                        "INTER_BEAT_INTERVAL", "BATTERY", "HEART_RATE", "THERMOMETER",
                        "ACCELEROMETER", "ELECTRODERMAL_ACTIVITY", "BLOOD_VOLUME_PULSE"
                })
                .collect(Collectors.toMap(identity(), s -> new Sensor(
                        s, States.DISCONNECTED, 0, 1.0)));

        Subject expected = new Subject(SUBJECT, true,
                Utility.getExpectedTimeFrame(Long.MAX_VALUE, Long.MIN_VALUE, randomInput),
                Collections.singletonList(new Source(SOURCE, SOURCE_TYPE, new SourceSummary(
                        States.DISCONNECTED, 0, 1.0, sensorMap))));

        assertEquals(expected, actual);

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
                    SOURCE_TYPE, TIME_WINDOW));
        Utility.dropCollection(client, AndroidAppDataAccessObject.getInstance().getCollections());
        client.close();
    }

}
