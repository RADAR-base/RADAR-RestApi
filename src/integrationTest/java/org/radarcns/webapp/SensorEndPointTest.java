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

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.radarcns.restapi.header.DescriptiveStatistic.COUNT;
import static org.radarcns.webapp.util.BasePath.AVRO_BINARY;
import static org.radarcns.webapp.util.BasePath.DATA;
import static org.radarcns.webapp.util.BasePath.REALTIME;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import javax.ws.rs.core.Response.Status;
import okhttp3.Response;
import org.bson.Document;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.radarcns.catalogue.TimeWindow;
import org.radarcns.catalogue.Unit;
import org.radarcns.config.Properties;
import org.radarcns.dao.AndroidAppDataAccessObject;
import org.radarcns.dao.SensorDataAccessObject;
import org.radarcns.dao.mongo.util.MongoHelper;
import org.radarcns.integration.util.ApiClient;
import org.radarcns.integration.util.RandomInput;
import org.radarcns.integration.util.Utility;
import org.radarcns.restapi.data.DoubleSample;
import org.radarcns.restapi.dataset.Dataset;
import org.radarcns.util.RadarConverter;

public class SensorEndPointTest {
    private static final String SUBJECT = "sub-1";
    private static final String SOURCE = "SourceID_0";
    private static final String SOURCE_TYPE = org.radarcns.unit.config.TestCatalog.EMPATICA;
    private static final String SENSOR_TYPE = "HEART_RATE";
    private static final TimeWindow TIME_FRAME = TimeWindow.TEN_SECOND;
    private static final Class<DoubleSample> ITEM = DoubleSample.class;
    private static final int SAMPLES = 10;
    private static final String SOURCE_PATH = SENSOR_TYPE + '/' + COUNT + '/' + TIME_FRAME + '/'
            + SUBJECT + '/' + SOURCE;

    @Rule
    public final ApiClient apiClient = new ApiClient(
            Properties.getApiConfig().getApiUrl() + DATA + '/');

    @Test
    public void getRealtimeTest()
            throws IOException, ReflectiveOperationException, URISyntaxException {
        MongoClient client = Utility.getMongoClient();

        MongoCollection<Document> collection = MongoHelper.getCollection(client,
                SensorDataAccessObject.getInstance(SENSOR_TYPE).getCollectionName(
                    SOURCE_TYPE, TIME_FRAME));

        List<Document> docs = RandomInput.getDocumentsRandom(SUBJECT, SOURCE, SOURCE_TYPE,
                SENSOR_TYPE, COUNT, TIME_FRAME, SAMPLES, false);

        collection.insertMany(docs);

        Dataset expected = Utility.convertDocToDataset(singletonList(docs.get(docs.size() - 1)),
                SUBJECT, SOURCE, SOURCE_TYPE, SENSOR_TYPE, RadarConverter.getMongoStat(COUNT),
                Unit.BEATS_PER_MIN, TIME_FRAME, ITEM);


        Dataset actual = apiClient.requestAvro(REALTIME + "/" + SOURCE_PATH,
                Dataset.class, Status.OK);

        assertEquals(expected, actual);

        dropAndClose(client);
    }

    @Test
    public void getAllByUserTest()
            throws IOException, ReflectiveOperationException, URISyntaxException {
        MongoClient client = Utility.getMongoClient();

        MongoCollection<Document> collection = MongoHelper.getCollection(client,
                SensorDataAccessObject.getInstance(SENSOR_TYPE).getCollectionName(
                    SOURCE_TYPE, TIME_FRAME));

        List<Document> docs = RandomInput.getDocumentsRandom(SUBJECT, SOURCE, SOURCE_TYPE,
                SENSOR_TYPE, COUNT, TIME_FRAME, SAMPLES, false);

        collection.insertMany(docs);

        Dataset expected = Utility.convertDocToDataset(docs, SUBJECT, SOURCE, SOURCE_TYPE,
                SENSOR_TYPE, RadarConverter.getMongoStat(COUNT), Unit.BEATS_PER_MIN, TIME_FRAME,
                ITEM);

        Dataset actual = apiClient.requestAvro(SOURCE_PATH, Dataset.class, Status.OK);

        assertEquals(expected, actual);

        dropAndClose(client);
    }

    @Test
    public void getTimeWindowTest200() throws IOException, ReflectiveOperationException {
        MongoClient client = Utility.getMongoClient();

        MongoCollection<Document> collection = MongoHelper.getCollection(client,
                SensorDataAccessObject.getInstance(SENSOR_TYPE).getCollectionName(
                    SOURCE_TYPE, TIME_FRAME));

        List<Document> docs = RandomInput.getDocumentsRandom(SUBJECT, SOURCE, SOURCE_TYPE,
                SENSOR_TYPE, COUNT, TIME_FRAME, SAMPLES, false);
        while (docs.size() < 6) {
            docs = RandomInput.getDocumentsRandom(SUBJECT, SOURCE, SOURCE_TYPE, SENSOR_TYPE,
                COUNT, TIME_FRAME, SAMPLES, false);
        }
        collection.insertMany(docs);

        int index = Math.max(3, docs.size() / 2);

        long start = docs.get(index - 1).getDate(MongoHelper.START).getTime();
        long end = docs.get(index + 1).getDate(MongoHelper.END).getTime();

        String path = SOURCE_PATH + '/' + start + '/' + end;
        Dataset actual = apiClient.requestAvro(path, Dataset.class, Status.OK);

        Dataset expected = Utility.convertDocToDataset(docs.subList(index - 1, index + 2),
                SUBJECT, SOURCE, SOURCE_TYPE, SENSOR_TYPE, RadarConverter.getMongoStat(COUNT),
                Unit.BEATS_PER_MIN, TIME_FRAME, ITEM);

        assertEquals(expected, actual);

        dropAndClose(client);
    }

    @Test
    public void getAllDataTest204() throws IOException {
        try (Response response = apiClient.request(SOURCE_PATH, AVRO_BINARY, Status.NO_CONTENT)) {
            assertNotNull(response);
        }
    }

    @After
    public void dropAndClose() {
        dropAndClose(Utility.getMongoClient());
    }

    /** Drops all used collections to bring the database back to the initial state, and close the
     *          database connection.
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
