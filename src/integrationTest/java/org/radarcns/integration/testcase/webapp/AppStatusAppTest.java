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
import static org.radarcns.avro.restapi.source.SourceType.EMPATICA;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Map;
import okhttp3.Response;
import org.bson.Document;
import org.junit.After;
import org.junit.Test;
import org.radarcns.avro.restapi.app.Application;
import org.radarcns.avro.restapi.sensor.SensorType;
import org.radarcns.avro.restapi.source.SourceType;
import org.radarcns.config.Properties;
import org.radarcns.dao.SensorDataAccessObject;
import org.radarcns.dao.mongo.AndroidDAO;
import org.radarcns.dao.mongo.sensor.HeartRateDAO;
import org.radarcns.dao.mongo.util.MongoHelper;
import org.radarcns.integration.util.RandomInput;
import org.radarcns.integration.util.Utility;
import org.radarcns.util.AvroConverter;

public class AppStatusAppTest {

    private final String SERVER = "http://localhost:8080/";
    private final String PATH = "radar/api/";

    private static final String USER = "UserID_0";
    private static final String SOURCE = "SourceID_0";
    private static final SourceType SOURCE_TYPE = EMPATICA;
    private static final SensorType SENSOR_TYPE = HEART_RATE;
    private static final int SAMPLES = 10;

    @Test
    public void getStatusTest204() throws IOException {
        String path = "android/avro/status/{userID}/{sourceID}";
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

        MongoCollection<Document> collection = MongoHelper.getCollection(client,
            HeartRateDAO.getInstance().getCollectionName(SOURCE_TYPE));
        collection.insertMany(RandomInput.getDocumentsRandom(USER, SOURCE, SOURCE_TYPE, SENSOR_TYPE,
            COUNT, SAMPLES, false));

        Map<String, Document> map = RandomInput.getRandomApplicationStatus(
                    USER.concat("1"), SOURCE.concat("1"));
        Utility.insertMixedDocs(client, map);

        Application expected = Utility.convertDocToApplication(map);

        String path = "android/avro/status/{userID}/{sourceID}";
        path = path.replace("{userID}", USER.concat("1"));
        path = path.replace("{sourceID}", SOURCE.concat("1"));

        Response response = Utility.makeRequest(SERVER + PATH + path);
        assertEquals(200, response.code());

        if (response.code() == 200) {
            Application actual = AvroConverter.avroByteToAvro(response.body().bytes(),
                Application.getClassSchema());
            assertEquals(expected, actual);
        }

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
