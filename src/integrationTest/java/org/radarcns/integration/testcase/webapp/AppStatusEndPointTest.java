package org.radarcns.integration.testcase.webapp;

/*
 *  Copyright 2016 King's College London and The Hyve
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
import java.util.List;
import java.util.Map;
import okhttp3.Response;
import org.bson.Document;
import org.junit.After;
import org.junit.Test;
import org.radarcns.avro.restapi.app.Application;
import org.radarcns.avro.restapi.header.TimeFrame;
import org.radarcns.avro.restapi.sensor.SensorType;
import org.radarcns.avro.restapi.source.SourceType;
import org.radarcns.config.api.Properties;
import org.radarcns.dao.AndroidAppDataAccessObject;
import org.radarcns.dao.SensorDataAccessObject;
import org.radarcns.dao.mongo.util.MongoHelper;
import org.radarcns.integration.util.RandomInput;
import org.radarcns.integration.util.Utility;
import org.radarcns.util.AvroConverter;

public class AppStatusEndPointTest {

    private static final String USER = "UserID_0";
    private static final String SOURCE = "SourceID_0";
    private static final SourceType SOURCE_TYPE = EMPATICA;
    private static final SensorType SENSOR_TYPE = HEART_RATE;
    private static final TimeFrame TIME_FRAME = TimeFrame.TEN_SECOND;
    private static final int SAMPLES = 10;

    @Test
    public void getStatusTest204() throws IOException {
        String path = "android/avro/status/{userID}/{sourceID}";
        path = path.replace("{userID}", USER);
        path = path.replace("{sourceID}", SOURCE);

        assertEquals(204, Utility.makeRequest(Properties.getApiConfig().getApiUrl()
                + path).code());
    }

    @Test
    public void getStatusTest200()
        throws IOException, IllegalAccessException, InstantiationException, URISyntaxException {
        MongoClient client = Utility.getMongoClient();

        MongoCollection<Document> collection = MongoHelper.getCollection(client,
                SensorDataAccessObject.getInstance(SENSOR_TYPE).getCollectionName(
                    SOURCE_TYPE, TIME_FRAME));

        List<Document> list = RandomInput.getDocumentsRandom(USER, SOURCE, SOURCE_TYPE, SENSOR_TYPE,
            COUNT, SAMPLES, false);

        collection.insertMany(list);

        Map<String, Document> map = RandomInput.getRandomApplicationStatus(
                USER.concat("1"), SOURCE.concat("1"));

        Utility.insertMixedDocs(client, map);

        Application expected = Utility.convertDocToApplication(map);

        String path = "android/avro/status/{userID}/{sourceID}";
        path = path.replace("{userID}", USER.concat("1"));
        path = path.replace("{sourceID}", SOURCE.concat("1"));

        Response response = Utility.makeRequest(Properties.getApiConfig().getApiUrl() + path);
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
