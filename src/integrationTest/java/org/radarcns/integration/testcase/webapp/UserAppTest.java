package org.radarcns.integration.testcase.webapp;

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

import static org.junit.Assert.assertEquals;
import static org.radarcns.avro.restapi.header.DescriptiveStatistic.COUNT;
import static org.radarcns.avro.restapi.sensor.SensorType.HEART_RATE;
import static org.radarcns.avro.restapi.source.SourceType.ANDROID;
import static org.radarcns.avro.restapi.source.SourceType.EMPATICA;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import okhttp3.Response;
import org.bson.Document;
import org.junit.After;
import org.junit.Test;
import org.radarcns.avro.restapi.sensor.SensorType;
import org.radarcns.avro.restapi.source.Source;
import org.radarcns.avro.restapi.source.SourceType;
import org.radarcns.avro.restapi.user.Cohort;
import org.radarcns.avro.restapi.user.Patient;
import org.radarcns.config.Properties;
import org.radarcns.dao.SensorDataAccessObject;
import org.radarcns.dao.mongo.AndroidDAO;
import org.radarcns.dao.mongo.sensor.HeartRateDAO;
import org.radarcns.dao.mongo.util.MongoHelper;
import org.radarcns.integration.util.RandomInput;
import org.radarcns.integration.util.Utility;
import org.radarcns.util.AvroConverter;

public class UserAppTest {

    private final String SERVER = "http://localhost:8080/";
    private final String PATH = "radar/api/";

    private static final String USER = "UserID_0";
    private static final String SOURCE = "SourceID_0";
    private static final SourceType SOURCE_TYPE = EMPATICA;
    private static final SensorType SENSOR_TYPE = HEART_RATE;
    private static final int SAMPLES = 10;

    @Test
    public void getAllPatientsTest204() throws IOException {
        String path = "user/avro/getAllPatients/{studyID}";
        path = path.replace("{studyID}", "0");

        assertEquals(204, Utility.makeRequest(SERVER + PATH + path).code());
    }

    @Test
    public void getAllPatientsTest200()
        throws IOException, IllegalAccessException, InstantiationException, URISyntaxException {
        Properties.getInstanceTest(Paths.get(this.getClass().getClassLoader().getResource(
            Properties.NAME_FILE).toURI()).toString());

        MongoClient client = Utility.getMongoClient();

        MongoCollection<Document> collection = MongoHelper.getCollection(client,
            HeartRateDAO.getInstance().getCollectionName(SOURCE_TYPE));
        collection.insertMany(RandomInput.getDocumentsRandom(USER, SOURCE, SOURCE_TYPE, SENSOR_TYPE,
            COUNT, SAMPLES, false));

        Utility.insertMixedDocs(client,
            RandomInput.getRandomApplicationStatus(USER.concat("1"), SOURCE.concat("1")));

        String path = "user/avro/getAllPatients/{studyID}";
        path = path.replace("{studyID}", "0");

        Response response = Utility.makeRequest(SERVER + PATH + path);
        assertEquals(200, response.code());

        byte[] array = response.body().bytes();

        if (response.code() == 200) {
            Cohort cohort = AvroConverter.avroByteToAvro(array, Cohort.getClassSchema());

            for (Patient patient : cohort.getPatients()) {
                if (patient.getUserId().equalsIgnoreCase(USER)) {
                    Source source = patient.getSources().get(0);
                    assertEquals(SOURCE_TYPE, source.getType());
                    assertEquals(SOURCE, source.getId());
                } else if (patient.getUserId().equalsIgnoreCase(USER.concat("1"))) {
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

    public void dropAndClose(MongoClient client) {
        Utility.dropCollection(client, MongoHelper.DEVICE_CATALOG);
        Utility.dropCollection(client,
            SensorDataAccessObject.getInstance().getCollectionName(SOURCE_TYPE, SENSOR_TYPE));
        Utility.dropCollection(client, AndroidDAO.getInstance().getCollections());
        client.close();
    }

}
