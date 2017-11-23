package org.radarcns.integration.testcase.webapp;

import static org.junit.Assert.assertEquals;
import static org.radarcns.avro.restapi.header.DescriptiveStatistic.COUNT;
import static org.radarcns.avro.restapi.sensor.SensorType.HEART_RATE;
import static org.radarcns.avro.restapi.source.SourceType.ANDROID;
import static org.radarcns.avro.restapi.source.SourceType.BIOVOTION;
import static org.radarcns.avro.restapi.source.SourceType.EMPATICA;
import static org.radarcns.webapp.util.BasePath.STATE;
import static org.radarcns.webapp.util.BasePath.SPECIFICATION;
import static org.radarcns.webapp.util.BasePath.AVRO_BINARY;
import static org.radarcns.webapp.util.BasePath.GET_ALL_SOURCES;
import static org.radarcns.webapp.util.Parameter.SOURCE_ID;
import static org.radarcns.webapp.util.Parameter.SUBJECT_ID;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import javax.ws.rs.core.Response.Status;
import okhttp3.Response;
import org.bson.Document;
import org.junit.*;
import org.radarcns.avro.restapi.header.TimeFrame;
import org.radarcns.avro.restapi.sensor.Sensor;
import org.radarcns.avro.restapi.sensor.SensorSpecification;
import org.radarcns.avro.restapi.sensor.SensorType;
import org.radarcns.avro.restapi.source.Source;
import org.radarcns.avro.restapi.source.SourceSpecification;
import org.radarcns.avro.restapi.source.SourceSummary;
import org.radarcns.avro.restapi.source.SourceType;
import org.radarcns.avro.restapi.source.State;
import org.radarcns.avro.restapi.subject.Subject;
import org.radarcns.config.Properties;
import org.radarcns.dao.AndroidAppDataAccessObject;
import org.radarcns.dao.SensorDataAccessObject;
import org.radarcns.dao.mongo.util.MongoDataAccess;
import org.radarcns.dao.mongo.util.MongoHelper;
import org.radarcns.integration.util.RandomInput;
import org.radarcns.integration.util.Utility;
import org.radarcns.monitor.Monitors;
import org.radarcns.util.AvroConverter;
import org.radarcns.webapp.util.BasePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public class SourceEndPointTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SourceEndPointTest.class);

    private static final String SUBJECT = "UserID_0";
    private static final String SOURCE = "SourceID_0";
    private static final SourceType SOURCE_TYPE = EMPATICA;
    private static final SensorType SENSOR_TYPE = HEART_RATE;
    private static final TimeFrame TIME_FRAME = TimeFrame.TEN_SECOND;
    private static final int SAMPLES = 10;

    @Test
    public void getStatusTest204() throws IOException {
        String path = BasePath.SOURCE + "/" + STATE + "/{" + SUBJECT_ID
                + "}/{" + SOURCE_ID + "}";
        path = path.replace("{" + SUBJECT_ID + "}", SUBJECT);
        path = path.replace("{" + SOURCE_ID + "}", SOURCE);

        LOGGER.info(path);

        assertEquals(Status.NO_CONTENT.getStatusCode(), Utility.makeRequest(
                Properties.getApiConfig().getApiUrl() + path, AVRO_BINARY).code());
    }

    @Test
    public void getStatusTest200()
            throws IOException, IllegalAccessException, InstantiationException, URISyntaxException {

        MongoClient client = Utility.getMongoClient();

        MongoDataAccess.writeSourceType(SOURCE, SOURCE_TYPE, client);

        String path = BasePath.SOURCE + "/" + STATE + "/{" + SUBJECT_ID
                + "}/{" + SOURCE_ID + "}";
        path = path.replace("{" + SUBJECT_ID + "}", SUBJECT);
        path = path.replace("{" + SOURCE_ID + "}", SOURCE);

        LOGGER.info(path);

        Response response = Utility.makeRequest(Properties.getApiConfig().getApiUrl() + path,
                AVRO_BINARY);
        assertEquals(Status.OK.getStatusCode(), response.code());

        byte[] array = response.body().bytes();

        if (response.code() == Status.OK.getStatusCode()) {
            Source actual = AvroConverter.avroByteToAvro(array, Source.getClassSchema());
            assertEquals(SOURCE, actual.getId());
            assertEquals(SOURCE_TYPE, actual.getType());

            SourceSummary summary = actual.getSummary();
            assertEquals(State.DISCONNECTED, summary.getState());
            assertEquals(1.0, summary.getMessageLoss(), 0.0);
            assertEquals(0, summary.getReceivedMessage(), 0.0);

            List<SensorSpecification> spec = new LinkedList<>(
                    Monitors.getInstance().getSpecification(
                        EMPATICA).getSensors().values());

            for (Sensor sensor : summary.getSensors().values()) {
                assertEquals(State.DISCONNECTED, sensor.getState());
                assertEquals(1.0, sensor.getMessageLoss(), 0.0);
                assertEquals(0, sensor.getReceivedMessage(), 0.0);

                for (int i = 0; i < spec.size(); i++) {
                    if (spec.get(i).getName().name().equalsIgnoreCase(
                            sensor.getName().toString())) {
                        spec.remove(i);
                        break;
                    }
                }
            }

            assertEquals(0, spec.size());
        }

        dropAndClose(client);
    }

    @Test
    public void getSpecificationTest500() throws IOException {
        String path = BasePath.SOURCE + "/" + SPECIFICATION + "/{" + SOURCE_TYPE + "}";
        path = path.replace("{" + SOURCE_TYPE + "}", BIOVOTION.toString());

        LOGGER.info(path);

        assertEquals(Status.INTERNAL_SERVER_ERROR.getStatusCode(), Utility.makeRequest(
                Properties.getApiConfig().getApiUrl() + path, AVRO_BINARY).code());
    }

    @Test
    public void getSpecificationTest200() throws IOException {
        String path = BasePath.SOURCE + "/" + SPECIFICATION + "/{" + SOURCE_TYPE + "}";
        path = path.replace("{" + SOURCE_TYPE + "}", EMPATICA.toString());

        LOGGER.info(path);

        Response response = Utility.makeRequest(Properties.getApiConfig().getApiUrl() + path,
                AVRO_BINARY);
        assertEquals(Status.OK.getStatusCode(), response.code());

        SourceSpecification expected = Monitors.getInstance().getSpecification(EMPATICA);
        List<SensorSpecification> listSensors = new LinkedList<>(expected.getSensors().values());

        SourceSpecification actual = null;
        if (response.code() == Status.OK.getStatusCode()) {
            actual = AvroConverter.avroByteToAvro(
                    response.body().bytes(), SourceSpecification.getClassSchema());
        }

        for (SensorSpecification sensorSpec : actual.getSensors().values()) {
            for (int i = 0; i < listSensors.size(); i++) {
                if (listSensors.get(i).getName().name().equalsIgnoreCase(
                        sensorSpec.getName().toString())) {
                    listSensors.remove(i);
                    break;
                }
            }
        }

        assertEquals(true, listSensors.isEmpty());
    }

    @Test
    public void getAllSourcesTest200()
        throws IOException, IllegalAccessException, InstantiationException, URISyntaxException {
        String path = BasePath.SOURCE + "/" + GET_ALL_SOURCES
                + "/{" + SUBJECT_ID + "}";
        path = path.replace("{" + SUBJECT_ID + "}", SUBJECT);

        LOGGER.info(path);

        MongoClient client = Utility.getMongoClient();

        MongoCollection<Document> collection = MongoHelper.getCollection(client,
                SensorDataAccessObject.getInstance(SENSOR_TYPE).getCollectionName(
                    SOURCE_TYPE, TIME_FRAME));

        collection.insertMany(RandomInput.getDocumentsRandom(SUBJECT, SOURCE, SOURCE_TYPE,
                SENSOR_TYPE, COUNT, TIME_FRAME, SAMPLES, false));
        Utility.insertMixedDocs(client,
                RandomInput.getRandomApplicationStatus(SUBJECT, SOURCE.concat("1")));

        Response response = Utility.makeRequest(Properties.getApiConfig().getApiUrl() + path,
                AVRO_BINARY);
        assertEquals(Status.OK.getStatusCode(), response.code());

        Subject actual = null;
        if (response.code() == Status.OK.getStatusCode()) {
            actual = AvroConverter.avroByteToAvro(
                    response.body().bytes(), Subject.getClassSchema());
        }
        List<Source> listSource = actual.getSources();
        assertEquals(2, listSource.size());

        for (int i = 0; i < listSource.size(); i++) {
            if (listSource.get(i).getType().name().equalsIgnoreCase(ANDROID.name())) {
                listSource.remove(i);
                break;
            }
        }

        for (int i = 0; i < listSource.size(); i++) {
            if (listSource.get(i).getType().name().equalsIgnoreCase(EMPATICA.name())) {
                listSource.remove(i);
                break;
            }
        }
        assertEquals(0, listSource.size());

        dropAndClose(client);
    }

    @Test
    public void getAllSourcesTest204()
        throws IOException, IllegalAccessException, InstantiationException, URISyntaxException {
        String path = BasePath.SOURCE + "/" + GET_ALL_SOURCES
                + "/{" + SUBJECT_ID + "}";
        path = path.replace("{" + SUBJECT_ID + "}", SUBJECT);

        LOGGER.info(path);

        Response response = Utility.makeRequest(Properties.getApiConfig().getApiUrl() + path,
                AVRO_BINARY);
        assertEquals(Status.NO_CONTENT.getStatusCode(), response.code());
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
