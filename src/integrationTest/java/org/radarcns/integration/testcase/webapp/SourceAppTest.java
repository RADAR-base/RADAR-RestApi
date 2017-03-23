package org.radarcns.integration.testcase.webapp;

import static org.junit.Assert.assertEquals;
import static org.radarcns.avro.restapi.header.DescriptiveStatistic.COUNT;
import static org.radarcns.avro.restapi.sensor.SensorType.HEART_RATE;
import static org.radarcns.avro.restapi.source.SourceType.ANDROID;
import static org.radarcns.avro.restapi.source.SourceType.BIOVOTION;
import static org.radarcns.avro.restapi.source.SourceType.EMPATICA;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import okhttp3.Response;
import org.bson.Document;
import org.junit.After;
import org.junit.Test;
import org.radarcns.avro.restapi.sensor.Sensor;
import org.radarcns.avro.restapi.sensor.SensorSpecification;
import org.radarcns.avro.restapi.sensor.SensorType;
import org.radarcns.avro.restapi.source.Source;
import org.radarcns.avro.restapi.source.SourceSpecification;
import org.radarcns.avro.restapi.source.SourceSummary;
import org.radarcns.avro.restapi.source.SourceType;
import org.radarcns.avro.restapi.source.State;
import org.radarcns.avro.restapi.user.Patient;
import org.radarcns.config.Properties;
import org.radarcns.dao.SensorDataAccessObject;
import org.radarcns.dao.mongo.AndroidDAO;
import org.radarcns.dao.mongo.sensor.HeartRateDAO;
import org.radarcns.dao.mongo.util.MongoDAO;
import org.radarcns.dao.mongo.util.MongoHelper;
import org.radarcns.integration.util.RandomInput;
import org.radarcns.integration.util.Utility;
import org.radarcns.monitor.Monitors;
import org.radarcns.util.AvroConverter;

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

public class SourceAppTest {

    private final String SERVER = "http://localhost:8080/";
    private final String PATH = "radar/api/";

    private static final String USER = "UserID_0";
    private static final String SOURCE = "SourceID_0";
    private static final SourceType SOURCE_TYPE = EMPATICA;
    private static final SensorType SENSOR_TYPE = HEART_RATE;
    private static final int SAMPLES = 10;

    @Test
    public void getStatusTest204() throws IOException {
        String path = "source/avro/state/{userID}/{sourceID}";
        path = path.replace("{userID}", USER);
        path = path.replace("{sourceID}", SOURCE);

        assertEquals(204, Utility.makeRequest(SERVER + PATH + path).code());
    }

    @Test
    public void getStatusTest200()
        throws IOException, IllegalAccessException, InstantiationException, URISyntaxException {
        Properties.getInstanceTest(Paths.get(this.getClass().getClassLoader().getResource(
                Properties.NAME_FILE).toURI()).toString());

        MongoClient client = Utility.getMongoClient();

        MongoDAO.writeSourceType(SOURCE, SOURCE_TYPE, client);

        String path = "source/avro/state/{userID}/{sourceID}";
        path = path.replace("{userID}", USER);
        path = path.replace("{sourceID}", SOURCE);

        Response response = Utility.makeRequest(SERVER + PATH + path);
        assertEquals(200, response.code());

        byte[] array = response.body().bytes();

        if (response.code() == 200) {
            Source actual = AvroConverter.avroByteToAvro(array, Source.getClassSchema());
            assertEquals(SOURCE, actual.getId());
            assertEquals(SOURCE_TYPE, actual.getType());

            SourceSummary summary = actual.getSummary();
            assertEquals(State.DISCONNECTED, summary.getState());
            assertEquals(1.0, summary.getMessageLoss(), 0.0);
            assertEquals(0, summary.getReceivedMessage(), 0.0);

            //TODO
            List<SensorSpecification> spec = new LinkedList<>(Monitors.getInstance().getSpecification(
                    SourceType.EMPATICA).getSensors().values());

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
        String path = "source/avro/specification/{sourceType}";
        path = path.replace("{sourceType}", BIOVOTION.toString());

        assertEquals(500, Utility.makeRequest(SERVER + PATH + path).code());
    }

    @Test
    public void getSpecificationTest200() throws IOException {
        String path = "source/avro/specification/{sourceType}";
        path = path.replace("{sourceType}", EMPATICA.toString());

        Response response = Utility.makeRequest(SERVER + PATH + path);
        assertEquals(200, response.code());

        //TODO
        SourceSpecification expected = Monitors.getInstance().getSpecification(SourceType.EMPATICA);
        List<SensorSpecification> listSensors = new LinkedList<>(expected.getSensors().values());

        SourceSpecification actual = null;
        if (response.code() == 200) {
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
    public void getAllSourcesTest()
        throws IOException, IllegalAccessException, InstantiationException, URISyntaxException {
        String path = "source/avro/getAllSources/{userID}";
        path = path.replace("{userID}", USER);

        Properties.getInstanceTest(Paths.get(this.getClass().getClassLoader().getResource(
                Properties.NAME_FILE).toURI()).toString());

        MongoClient client = Utility.getMongoClient();

        MongoCollection<Document> collection = MongoHelper.getCollection(client,
            HeartRateDAO.getInstance().getCollectionName(SOURCE_TYPE));

        collection.insertMany(RandomInput.getDocumentsRandom(USER, SOURCE, SOURCE_TYPE, SENSOR_TYPE,
            COUNT, SAMPLES, false));
        Utility.insertMixedDocs(client,
            RandomInput.getRandomApplicationStatus(USER, SOURCE.concat("1")));

        Response response = Utility.makeRequest(SERVER + PATH + path);
        assertEquals(200, response.code());

        Patient actual = null;
        if (response.code() == 200) {
            actual = AvroConverter.avroByteToAvro(
                    response.body().bytes(), Patient.getClassSchema());
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

    @After
    public void dropAndClose() throws URISyntaxException {
        Properties.getInstanceTest(Paths.get(this.getClass().getClassLoader().getResource(
                Properties.NAME_FILE).toURI()).toString());
        dropAndClose(Utility.getMongoClient());
    }

    public void dropAndClose(MongoClient client) {
        Utility.dropCollection(client, MongoHelper.DEVICE_CATALOG);
        Utility.dropCollection(client,
            SensorDataAccessObject.getInstance().getCollectionName(SOURCE_TYPE, SENSOR_TYPE));
        Utility.dropCollection(client, AndroidDAO.getInstance().getCollections());
        client.close();
    }

}
