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

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.radarcns.avro.restapi.header.DescriptiveStatistic.COUNT;
import static org.radarcns.avro.restapi.sensor.SensorType.HEART_RATE;
import static org.radarcns.avro.restapi.source.SourceType.EMPATICA;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import okhttp3.Response;
import org.bson.Document;
import org.junit.After;
import org.junit.Test;
import org.radarcns.avro.restapi.data.DoubleSample;
import org.radarcns.avro.restapi.dataset.Dataset;
import org.radarcns.avro.restapi.header.TimeFrame;
import org.radarcns.avro.restapi.sensor.SensorType;
import org.radarcns.avro.restapi.sensor.Unit;
import org.radarcns.avro.restapi.source.SourceType;
import org.radarcns.config.Properties;
import org.radarcns.dao.AndroidAppDataAccessObject;
import org.radarcns.dao.SensorDataAccessObject;
import org.radarcns.dao.mongo.util.MongoHelper;
import org.radarcns.integration.util.RandomInput;
import org.radarcns.integration.util.Utility;
import org.radarcns.util.AvroConverter;
import org.radarcns.util.RadarConverter;

public class SensorEndPointTest {

    private static final String USER = "UserID_0";
    private static final String SOURCE = "SourceID_0";
    private static final SourceType SOURCE_TYPE = EMPATICA;
    private static final SensorType SENSOR_TYPE = HEART_RATE;
    private static final TimeFrame TIME_FRAME = TimeFrame.TEN_SECOND;
    private static final Class ITEM = DoubleSample.class;
    private static final int SAMPLES = 10;

    @Test
    public void getRealtimeTest()
            throws IOException, IllegalAccessException, InstantiationException, URISyntaxException {
        String path = "data/avro/realTime/{sensor}/{stat}/{interval}/{userID}/{sourceID}";
        path = path.replace("{sensor}", SENSOR_TYPE.name());
        path = path.replace("{stat}", COUNT.name());
        path = path.replace("{interval}", TIME_FRAME.name());
        path = path.replace("{userID}", USER);
        path = path.replace("{sourceID}", SOURCE);

        MongoClient client = Utility.getMongoClient();

        MongoCollection<Document> collection = MongoHelper.getCollection(client,
                SensorDataAccessObject.getInstance(SENSOR_TYPE).getCollectionName(
                    SOURCE_TYPE, TIME_FRAME));

        List<Document> docs = RandomInput.getDocumentsRandom(USER, SOURCE, SOURCE_TYPE, SENSOR_TYPE,
                COUNT, SAMPLES, false);

        collection.insertMany(docs);

        Dataset expected = Utility.convertDocToDataset(singletonList(docs.get(docs.size() - 1)),
                RadarConverter.getMongoStat(COUNT), Unit.BEATS_PER_MIN, TIME_FRAME, ITEM);

        Dataset actual = null;

        Response response = Utility.makeRequest(Properties.getApiConfig().getApiUrl() + path);
        assertEquals(200, response.code());

        if (response.code() == 200) {
            actual = AvroConverter.avroByteToAvro(response.body().bytes(),
                    Dataset.getClassSchema());
        }

        assertEquals(expected, actual);

        dropAndClose(client);
    }

    @Test
    public void getAllByUserTest()
            throws IOException, IllegalAccessException, InstantiationException, URISyntaxException {
        String path = "data/avro/{sensor}/{stat}/{interval}/{userID}/{sourceID}";
        path = path.replace("{sensor}", SENSOR_TYPE.name());
        path = path.replace("{stat}", COUNT.name());
        path = path.replace("{interval}", TIME_FRAME.name());
        path = path.replace("{userID}", USER);
        path = path.replace("{sourceID}", SOURCE);

        MongoClient client = Utility.getMongoClient();

        MongoCollection<Document> collection = MongoHelper.getCollection(client,
                SensorDataAccessObject.getInstance(SENSOR_TYPE).getCollectionName(
                    SOURCE_TYPE, TIME_FRAME));

        List<Document> docs = RandomInput.getDocumentsRandom(USER, SOURCE, SOURCE_TYPE, SENSOR_TYPE,
                COUNT, SAMPLES, false);

        collection.insertMany(docs);

        Dataset expected = Utility.convertDocToDataset(docs, RadarConverter.getMongoStat(COUNT),
                Unit.BEATS_PER_MIN, TIME_FRAME, ITEM);

        Dataset actual = null;

        Response response = Utility.makeRequest(Properties.getApiConfig().getApiUrl() + path);
        assertEquals(200, response.code());

        if (response.code() == 200) {
            actual = AvroConverter.avroByteToAvro(response.body().bytes(),
                Dataset.getClassSchema());
        }

        assertEquals(expected, actual);

        dropAndClose(client);
    }

    @Test
    public void getRealtimeTest200()
            throws IOException, IllegalAccessException, InstantiationException, URISyntaxException {
        String path = "data/avro/{sensor}/{stat}/{interval}/{userID}/{sourceID}/{start}/{end}";
        path = path.replace("{sensor}", SENSOR_TYPE.name());
        path = path.replace("{stat}", COUNT.name());
        path = path.replace("{interval}", TIME_FRAME.name());
        path = path.replace("{userID}", USER);
        path = path.replace("{sourceID}", SOURCE);

        MongoClient client = Utility.getMongoClient();

        MongoCollection<Document> collection = MongoHelper.getCollection(client,
                SensorDataAccessObject.getInstance(SENSOR_TYPE).getCollectionName(
                    SOURCE_TYPE, TIME_FRAME));

        List<Document> docs = RandomInput.getDocumentsRandom(USER, SOURCE, SOURCE_TYPE, SENSOR_TYPE,
                COUNT, SAMPLES, false);
        while (docs.size() < 6) {
            docs = RandomInput.getDocumentsRandom(USER, SOURCE, SOURCE_TYPE, SENSOR_TYPE,
                COUNT, SAMPLES, false);
        }
        collection.insertMany(docs);

        int index = Math.max(3, docs.size() / 2);

        long start = docs.get(index - 1).getDate(MongoHelper.START).getTime();
        long end = docs.get(index + 1).getDate(MongoHelper.END).getTime();

        path = path.replace("{start}", String.valueOf(start));
        path = path.replace("{end}", String.valueOf(end));

        Dataset expected = Utility.convertDocToDataset(docs.subList(index - 1, index + 2),
                RadarConverter.getMongoStat(COUNT), Unit.BEATS_PER_MIN, TIME_FRAME, ITEM);

        Dataset actual = null;

        Response response = Utility.makeRequest(Properties.getApiConfig().getApiUrl() + path);
        assertEquals(200, response.code());

        if (response.code() == 200) {
            actual = AvroConverter.avroByteToAvro(response.body().bytes(),
                Dataset.getClassSchema());
        }

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
                    SOURCE_TYPE, TIME_FRAME));
        Utility.dropCollection(client, AndroidAppDataAccessObject.getInstance().getCollections());
        client.close();
    }

}
