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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.radarcns.restapi.header.DescriptiveStatistic.COUNT;
import static org.radarcns.unit.config.TestCatalog.BIOVOTION;
import static org.radarcns.unit.config.TestCatalog.EMPATICA;
import static org.radarcns.webapp.util.BasePath.AVRO_BINARY;
import static org.radarcns.webapp.util.BasePath.GET_ALL_SOURCES;
import static org.radarcns.webapp.util.BasePath.SPECIFICATION;
import static org.radarcns.webapp.util.BasePath.STATE;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.ws.rs.core.Response.Status;
import okhttp3.Response;
import org.bson.Document;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.radarcns.catalogue.TimeWindow;
import org.radarcns.config.Properties;
import org.radarcns.dao.AndroidAppDataAccessObject;
import org.radarcns.dao.SensorDataAccessObject;
import org.radarcns.dao.mongo.util.MongoDataAccess;
import org.radarcns.dao.mongo.util.MongoHelper;
import org.radarcns.integration.util.ApiClient;
import org.radarcns.integration.util.RandomInput;
import org.radarcns.integration.util.Utility;
import org.radarcns.monitor.Monitors;
import org.radarcns.restapi.source.Sensor;
import org.radarcns.restapi.source.Source;
import org.radarcns.restapi.source.SourceSummary;
import org.radarcns.restapi.source.States;
import org.radarcns.restapi.spec.SensorSpecification;
import org.radarcns.restapi.spec.SourceSpecification;
import org.radarcns.restapi.subject.Subject;
import org.radarcns.webapp.util.BasePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SourceEndPointTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SourceEndPointTest.class);

    private static final String SUBJECT = "sub-1";
    private static final String SOURCE = "SourceID_0";
    private static final String SOURCE_TYPE = EMPATICA;
    private static final String SENSOR_TYPE = "HEART_RATE";
    private static final TimeWindow TIME_FRAME = TimeWindow.TEN_SECOND;
    private static final int SAMPLES = 10;

    @Rule
    public final ApiClient apiClient = new ApiClient(
            Properties.getApiConfig().getApiUrl() + BasePath.SOURCE + '/');

    @Test
    public void getStatusTest204() throws IOException {
        try (Response response = apiClient.request(STATE + '/' + SUBJECT + '/' + SOURCE,
                AVRO_BINARY, Status.NO_CONTENT)) {
            assertNotNull(response);
        }
    }

    @Test
    public void getStatusTest200()
            throws IOException, ReflectiveOperationException, URISyntaxException {

        MongoClient client = Utility.getMongoClient();

        MongoDataAccess.writeSourceType(SOURCE, SOURCE_TYPE, client);

        Source actual = apiClient.requestAvro(STATE + '/' + SUBJECT + '/' + SOURCE,
                Source.class, Status.OK);

        assertEquals(SOURCE, actual.getId());
        assertEquals(SOURCE_TYPE, actual.getType());

        SourceSummary summary = actual.getSummary();
        assertEquals(States.DISCONNECTED, summary.getState());
        assertEquals(1.0, summary.getMessageLoss(), 0.0);
        assertEquals(0, summary.getReceivedMessage(), 0.0);

        List<SensorSpecification> spec = new LinkedList<>(
                Monitors.getInstance().getSpecification(
                    EMPATICA).getSensors().values());

        for (Sensor sensor : summary.getSensors().values()) {
            assertEquals(States.DISCONNECTED, sensor.getState());
            assertEquals(1.0, sensor.getMessageLoss(), 0.0);
            assertEquals(0, sensor.getReceivedMessage(), 0.0);

            for (int i = 0; i < spec.size(); i++) {
                if (spec.get(i).getName().equalsIgnoreCase(
                        sensor.getName())) {
                    spec.remove(i);
                    break;
                }
            }
        }

        assertEquals(0, spec.size());

        dropAndClose(client);
    }

    @Test
    public void getSpecificationTest500() throws IOException {
        try (Response response = apiClient.request(SPECIFICATION + '/' + BIOVOTION, AVRO_BINARY,
                Status.INTERNAL_SERVER_ERROR)) {
            assertNotNull(response);
        }
    }

    @Test
    public void getSpecificationTest200() throws IOException, ReflectiveOperationException {
        SourceSpecification expected = Monitors.getInstance().getSpecification(SOURCE_TYPE);
        List<SensorSpecification> listSensors = new LinkedList<>(expected.getSensors().values());

        SourceSpecification actual = apiClient.requestAvro(SPECIFICATION + '/' + SOURCE_TYPE,
                SourceSpecification.class, Status.OK);

        for (SensorSpecification sensorSpec : new ArrayList<>(actual.getSensors().values())) {
            for (int i = 0; i < listSensors.size(); i++) {
                if (listSensors.get(i).getName().equalsIgnoreCase(
                        sensorSpec.getName())) {
                    listSensors.remove(i);
                    break;
                }
            }
        }

        assertEquals(true, listSensors.isEmpty());
    }

    @Test
    public void getAllSourcesTest200()
            throws IOException, ReflectiveOperationException, URISyntaxException {
        String path = BasePath.SOURCE + "/" + GET_ALL_SOURCES + "/" + SUBJECT;

        LOGGER.info(path);

        MongoClient client = Utility.getMongoClient();

        MongoCollection<Document> collection = MongoHelper.getCollection(client,
                SensorDataAccessObject.getInstance(SENSOR_TYPE).getCollectionName(
                    SOURCE_TYPE, TIME_FRAME));

        collection.insertMany(RandomInput.getDocumentsRandom(SUBJECT, SOURCE, SOURCE_TYPE,
                SENSOR_TYPE, COUNT, TIME_FRAME, SAMPLES, false));
        Utility.insertMixedDocs(client,
                RandomInput.getRandomApplicationStatus(SUBJECT, SOURCE.concat("1")));

        Subject actual = apiClient.requestAvro(GET_ALL_SOURCES + '/' + SUBJECT,
                Subject.class, Status.OK);

        List<Source> listSource = new ArrayList<>(actual.getSources());
        assertEquals(2, listSource.size());

        Iterator<Source> iterator = listSource.iterator();

        while (iterator.hasNext()) {
            switch (iterator.next().getType()) {
                case org.radarcns.unit.config.TestCatalog.ANDROID: case EMPATICA:
                    iterator.remove();
                    break;
                default:
                    break;
            }
        }
        assertEquals(0, listSource.size());

        dropAndClose(client);
    }

    @Test
    public void getAllSourcesTest204() throws IOException {
        try (Response response = apiClient.request(
                GET_ALL_SOURCES + "/" + SUBJECT, AVRO_BINARY, Status.NO_CONTENT)) {
            assertNotNull(response);
        }
    }

    @After
    public void dropAndClose() throws URISyntaxException {
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
