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

package org.radarcns.monitor;

import static org.junit.Assert.assertEquals;
import static org.radarcns.config.TestCatalog.EMPATICA;
import static org.radarcns.dao.mongo.data.sensor.AccelerationFormat.X_LABEL;
import static org.radarcns.dao.mongo.data.sensor.AccelerationFormat.Y_LABEL;
import static org.radarcns.dao.mongo.data.sensor.AccelerationFormat.Z_LABEL;
import static org.radarcns.dao.mongo.util.MongoHelper.FIRST_QUARTILE;
import static org.radarcns.dao.mongo.util.MongoHelper.SECOND_QUARTILE;
import static org.radarcns.dao.mongo.util.MongoHelper.THIRD_QUARTILE;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.bson.Document;
import org.junit.After;
import org.junit.Test;
import org.radarcns.catalog.SourceDefinition;
import org.radarcns.catalogue.TimeWindow;
import org.radarcns.config.Properties;
import org.radarcns.dao.SensorDataAccessObject;
import org.radarcns.dao.mongo.util.MongoHelper;
import org.radarcns.dao.mongo.util.MongoHelper.Stat;
import org.radarcns.integration.util.Utility;
import org.radarcns.restapi.source.Source;
import org.radarcns.restapi.source.States;
import org.radarcns.source.SourceCatalog;

public class SourceMonitorDbTest {

    private static final String SUBJECT = "UserID_0";
    private static final String SOURCE = "SourceID_0";
    private static final String SOURCE_TYPE = EMPATICA;

    private static int WINDOWS = 2;

    @Test
    public void testGetStateFine() throws IOException, URISyntaxException {
        MongoClient client = getClient();

        Source source = getSource(WINDOWS,0, client);

        assertEquals(States.FINE, source.getSummary().getState());

        dropAndClose(client);
    }

    @Test
    public void testGetStateOk() throws ConnectException, URISyntaxException {
        MongoClient client = getClient();

        Source source = getSource(WINDOWS, 0.05, client);

        assertEquals(States.OK, source.getSummary().getState());

        dropAndClose(client);
    }

    @Test
    public void testGetStateWarining() throws ConnectException, URISyntaxException {
        MongoClient client = getClient();

        Source source = getSource(WINDOWS, 0.50, client);

        assertEquals(States.WARNING, source.getSummary().getState());

        dropAndClose(client);
    }

    @Test
    public void testGetStateDisconnected() throws ConnectException, URISyntaxException {
        MongoClient client = getClient();

        Source source = getSource(WINDOWS, 1, client);

        assertEquals(States.DISCONNECTED, source.getSummary().getState());

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
        SourceDefinition definition = SourceCatalog.getInstance().getDefinition(SOURCE_TYPE);
        for (String sensorType : definition.getSensorTypes()) {
            Utility.dropCollection(client,
                    SensorDataAccessObject.getInstance().getCollectionName(
                        SOURCE_TYPE, sensorType, TimeWindow.TEN_SECOND));
        }

        client.close();
    }

    private Source getSource(int window, double percentage, MongoClient client)
            throws ConnectException {
        long timestamp = System.currentTimeMillis();

        Map<String, Integer> count = new HashMap<>();
        long start = timestamp + TimeUnit.SECONDS.toMillis(10);
        long end = start + TimeUnit.SECONDS.toMillis(60 / (window + 1));
        int messages;

        String collectionName;

        SourceDefinition definition = SourceCatalog.getInstance().getDefinition(SOURCE_TYPE);
        for (int i = 0; i < window; i++) {
            for (String sensorType : definition.getSensorTypes()) {
                messages = reducedMessage(
                    definition.getFrequency(sensorType).intValue() * 60, percentage)
                        / window;

                collectionName = SensorDataAccessObject.getInstance().getCollectionName(
                    SOURCE_TYPE, sensorType, TimeWindow.TEN_SECOND);

                insertDoc(sensorType, messages, start, end,
                        MongoHelper.getCollection(client, collectionName));

                if (count.containsKey(sensorType)) {
                    count.put(sensorType, count.get(sensorType) + messages);
                } else {
                    count.put(sensorType, messages);
                }
            }

            start = end;
            end = start + TimeUnit.SECONDS.toMillis(60 / (window + 1));
        }

        end = start + TimeUnit.SECONDS.toMillis(1);
        for (String sensorType : count.keySet()) {
            int sendMessages = count.getOrDefault(sensorType, 0);
            messages = reducedMessage(
                definition.getFrequency(sensorType).intValue() * 60, percentage)
                    - sendMessages;

            if (messages > 0) {
                insertDoc(sensorType, messages, start, end,
                        MongoHelper.getCollection(client,
                            SensorDataAccessObject.getInstance().getCollectionName(SOURCE_TYPE,
                                sensorType, TimeWindow.TEN_SECOND)));
            }
        }

        return new SourceMonitor(new SourceDefinition(EMPATICA,
                Properties.getDeviceCatalog().getDevices().get(EMPATICA))).getState(
            SUBJECT, SOURCE, timestamp, end, client);
    }

    private static void insertDoc(String sensorType, int messages, long start, long end,
            MongoCollection collection) {
        Document doc;
        if (sensorType.equals("ACCELEROMETER")) {
            doc = getDocumentsByArray(messages, start, end);
        } else {
            doc = getDocumentsBySingle(messages, start, end);
        }
        collection.insertOne(doc);
    }


    private static Document getDocumentsBySingle(int samples, long start, long end) {
        return new Document(MongoHelper.ID, SUBJECT + "-" + SOURCE + "-" + start + "-" + end)
            .append(MongoHelper.USER, SUBJECT)
            .append(MongoHelper.SOURCE, SOURCE)
            .append(Stat.min.getParam(), 0d)
            .append(Stat.max.getParam(), 0d)
            .append(Stat.sum.getParam(), 0d)
            .append(Stat.count.getParam(), (double) samples)
            .append(Stat.avg.getParam(), 0d)
            .append(Stat.quartile.getParam(), getQuartile())
            .append(Stat.iqr.getParam(), 0d)
            .append(MongoHelper.START, new Date(start))
            .append(MongoHelper.END, new Date(end));
    }

    private static Document getDocumentsByArray(int samples, long start, long end) {
        return new Document(MongoHelper.ID, SUBJECT + "-" + SOURCE + "-" + start + "-" + end)
            .append(MongoHelper.USER, SUBJECT)
            .append(MongoHelper.SOURCE, SOURCE)
            .append(Stat.min.getParam(), getValue(0d))
            .append(Stat.max.getParam(), getValue(0d))
            .append(Stat.sum.getParam(), getValue(0d))
            .append(Stat.count.getParam(), getValue(samples))
            .append(Stat.avg.getParam(), getValue(0d))
            .append(Stat.quartile.getParam(), getValue(getQuartile()))
            .append(Stat.iqr.getParam(), getValue(0))
            .append(MongoHelper.START, new Date(start))
            .append(MongoHelper.END, new Date(end));
    }

    private static List<Document> getQuartile() {
        return Arrays.asList(
                new Document(FIRST_QUARTILE, 0d),
                new Document(SECOND_QUARTILE, 0d),
                new Document(THIRD_QUARTILE, 0d));
    }

    private static List<Document> getValue(Object value) {
        return Arrays.asList(
                new Document(X_LABEL, value),
                new Document(Y_LABEL, value),
                new Document(Z_LABEL, value));
    }

    /** Reduces the frequency rate to mock a data loss. **/
    public static int reducedMessage(double frequency, double reduction) {
        if (frequency == 1.0 && reduction == 1.0) {
            return 0;
        } else if (frequency == 1.0) {
            return Double.valueOf(frequency).intValue();
        }

        return Double.valueOf(frequency * (1 - reduction)).intValue();
    }

    private MongoClient getClient() throws URISyntaxException {
        return Utility.getMongoClient();
    }
}
