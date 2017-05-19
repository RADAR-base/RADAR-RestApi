package org.radarcns.integration.testcase.dao;

/*
 * Copyright 2017 King's College London and The Hyve
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
import static org.radarcns.avro.restapi.sensor.SensorType.ACCELEROMETER;
import static org.radarcns.avro.restapi.sensor.SensorType.HEART_RATE;
import static org.radarcns.avro.restapi.source.SourceType.EMPATICA;
import static org.radarcns.dao.mongo.util.MongoHelper.END;
import static org.radarcns.dao.mongo.util.MongoHelper.START;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bson.Document;
import org.junit.After;
import org.junit.Test;
import org.radarcns.avro.restapi.data.DoubleSample;
import org.radarcns.avro.restapi.dataset.Dataset;
import org.radarcns.avro.restapi.header.EffectiveTimeFrame;
import org.radarcns.avro.restapi.header.Header;
import org.radarcns.avro.restapi.header.TimeFrame;
import org.radarcns.avro.restapi.sensor.SensorType;
import org.radarcns.avro.restapi.sensor.Unit;
import org.radarcns.avro.restapi.source.SourceType;
import org.radarcns.dao.SensorDataAccessObject;
import org.radarcns.dao.mongo.util.MongoHelper;
import org.radarcns.dao.mongo.util.MongoHelper.Stat;
import org.radarcns.integration.util.RandomInput;
import org.radarcns.integration.util.Utility;
import org.radarcns.util.RadarConverter;

/**
 * UserDao Test.
 */
public class SensorDaoTest {

    //private static final Logger LOGGER = LoggerFactory.getLogger(SourceDaoTest.class);

    private static final String SUBJECT = "UserID_0";
    private static final String SOURCE = "SourceID_0";
    private static final SourceType SOURCE_TYPE = EMPATICA;
    private static final SensorType SENSOR_TYPE = HEART_RATE;
    private static final Unit UNIT = Unit.BEATS_PER_MIN;
    private static final Class ITEM = DoubleSample.class;
    private static final TimeFrame TIME_FRAME = TimeFrame.TEN_SECOND;
    private static final int SAMPLES = 10;

    private Set<String> dirtyCollections = new HashSet<>();

    @Test
    public void valueRealTimeByUserSourceTest() throws Exception {
        MongoClient client = Utility.getMongoClient();

        MongoCollection<Document> collection = MongoHelper.getCollection(client,
                SensorDataAccessObject.getInstance(SENSOR_TYPE).getCollectionName(
                    SOURCE_TYPE, TIME_FRAME));

        dirtyCollections.add(SensorDataAccessObject.getInstance(SENSOR_TYPE).getCollectionName(
                SOURCE_TYPE, TIME_FRAME));

        List<Document> docs = RandomInput.getDocumentsRandom(SUBJECT, SOURCE, SOURCE_TYPE,
                SENSOR_TYPE, COUNT, TIME_FRAME, SAMPLES, false);

        collection.insertMany(docs);

        Header header = new Header(SUBJECT, SOURCE, SOURCE_TYPE, SENSOR_TYPE, COUNT,
                    Unit.BEATS_PER_MIN, TIME_FRAME, null);

        Dataset actual = SensorDataAccessObject.getInstance(SENSOR_TYPE).valueRTByUserSource(
                SUBJECT, SOURCE, header, RadarConverter.getMongoStat(COUNT), collection);

        Dataset expected = Utility.convertDocToDataset(singletonList(docs.get(docs.size() - 1)),
                SUBJECT, SOURCE, SOURCE_TYPE, SENSOR_TYPE, RadarConverter.getMongoStat(COUNT), UNIT,
                TIME_FRAME, ITEM);

        assertEquals(expected, actual);

        dropAndClose(client);
    }

    @Test
    public void valueByUserSourceTest() throws Exception {
        MongoClient client = Utility.getMongoClient();

        MongoCollection<Document> collection = MongoHelper.getCollection(client,
                SensorDataAccessObject.getInstance(SENSOR_TYPE).getCollectionName(
                    SOURCE_TYPE, TIME_FRAME));

        dirtyCollections.add(SensorDataAccessObject.getInstance(SENSOR_TYPE).getCollectionName(
                SOURCE_TYPE, TIME_FRAME));

        List<Document> docs = RandomInput.getDocumentsRandom(SUBJECT, SOURCE, SOURCE_TYPE,
                SENSOR_TYPE, COUNT, TIME_FRAME, SAMPLES, false);

        collection.insertMany(docs);

        Header header = new Header(SUBJECT, SOURCE, SOURCE_TYPE, SENSOR_TYPE, COUNT,
                Unit.BEATS_PER_MIN, TIME_FRAME, null);

        Dataset actual = SensorDataAccessObject.getInstance(SENSOR_TYPE).valueByUserSource(SUBJECT,
                SOURCE, header, RadarConverter.getMongoStat(COUNT), collection);

        Dataset expected = Utility.convertDocToDataset(docs,
                SUBJECT, SOURCE, SOURCE_TYPE, SENSOR_TYPE, RadarConverter.getMongoStat(COUNT), UNIT,
                TIME_FRAME, ITEM);

        assertEquals(expected, actual);

        dropAndClose(client);
    }

    @Test
    public void valueByUserSourceWindowTest() throws Exception {
        MongoClient client = Utility.getMongoClient();

        MongoCollection<Document> collection = MongoHelper.getCollection(client,
                SensorDataAccessObject.getInstance(SENSOR_TYPE).getCollectionName(
                    SOURCE_TYPE, TIME_FRAME));

        dirtyCollections.add(SensorDataAccessObject.getInstance(SENSOR_TYPE).getCollectionName(
                SOURCE_TYPE, TIME_FRAME));

        List<Document> docs = RandomInput.getDocumentsRandom(SUBJECT, SOURCE, SOURCE_TYPE,
                SENSOR_TYPE, COUNT, TIME_FRAME, SAMPLES, false);
        while (docs.size() < 6) {
            docs = RandomInput.getDocumentsRandom(SUBJECT, SOURCE, SOURCE_TYPE, SENSOR_TYPE,
                COUNT, TIME_FRAME, SAMPLES, false);
        }
        collection.insertMany(docs);

        int index = Math.max(3, docs.size() / 2);

        long start = docs.get(index - 1).getDate(START).getTime();
        long end = docs.get(index + 1).getDate(END).getTime();

        Header header = new Header(SUBJECT, SOURCE, SOURCE_TYPE, SENSOR_TYPE, COUNT,
                Unit.BEATS_PER_MIN, TIME_FRAME, null);

        Dataset actual = SensorDataAccessObject.getInstance(SENSOR_TYPE).valueByUserSourceWindow(
                SUBJECT, SOURCE, header, RadarConverter.getMongoStat(COUNT), start, end,
                collection);

        Dataset expected = Utility.convertDocToDataset(docs.subList(index - 1, index + 2),
                SUBJECT, SOURCE, SOURCE_TYPE, SENSOR_TYPE, RadarConverter.getMongoStat(COUNT), UNIT,
                TIME_FRAME, ITEM);

        assertEquals(expected, actual);

        dropAndClose(client);
    }

    @Test
    public void countSamplesByUserSourceWindowTest() throws Exception {
        MongoClient client = Utility.getMongoClient();

        MongoCollection<Document> collection = MongoHelper.getCollection(client,
                SensorDataAccessObject.getInstance(SENSOR_TYPE).getCollectionName(
                    SOURCE_TYPE, TIME_FRAME));

        dirtyCollections.add(SensorDataAccessObject.getInstance(SENSOR_TYPE).getCollectionName(
                SOURCE_TYPE, TIME_FRAME));

        List<Document> docs = RandomInput.getDocumentsRandom(SUBJECT, SOURCE, SOURCE_TYPE,
                SENSOR_TYPE, COUNT, TIME_FRAME, SAMPLES, false);
        while (docs.size() < 6) {
            docs = RandomInput.getDocumentsRandom(SUBJECT, SOURCE, SOURCE_TYPE, SENSOR_TYPE,
                COUNT, TIME_FRAME, SAMPLES, false);
        }
        collection.insertMany(docs);

        int index = Math.max(3, docs.size() / 2);

        long start = docs.get(index - 1).getDate(START).getTime();
        long end = docs.get(index + 1).getDate(END).getTime();

        double actual = SensorDataAccessObject.getInstance(
                SENSOR_TYPE).countSamplesByUserSourceWindow(SUBJECT, SOURCE, start, end,
                collection);

        double expected = 0;
        for (Document doc : docs.subList(index - 1, index + 2)) {
            expected += doc.getDouble(Stat.count.getParam());
        }

        assertEquals(expected, actual, 0);

        dropAndClose(client);
    }

    @Test
    public void getUserEffectiveTimeFrame() throws Exception {
        MongoClient client = Utility.getMongoClient();

        MongoCollection<Document> collection = MongoHelper.getCollection(client,
                SensorDataAccessObject.getInstance(HEART_RATE).getCollectionName(
                SOURCE_TYPE, TIME_FRAME));

        dirtyCollections.add(SensorDataAccessObject.getInstance(HEART_RATE).getCollectionName(
                SOURCE_TYPE, TIME_FRAME));

        List<Document> randomInput = RandomInput.getDocumentsRandom(SUBJECT, SOURCE, SOURCE_TYPE,
                HEART_RATE, COUNT, TIME_FRAME, SAMPLES, false);

        collection.insertMany(randomInput);

        collection = MongoHelper.getCollection(client,
            SensorDataAccessObject.getInstance(ACCELEROMETER).getCollectionName(SOURCE_TYPE,
                TIME_FRAME));

        dirtyCollections.add(SensorDataAccessObject.getInstance(ACCELEROMETER).getCollectionName(
                SOURCE_TYPE, TIME_FRAME));

        randomInput = RandomInput.getDocumentsRandom(SUBJECT, SOURCE, SOURCE_TYPE, ACCELEROMETER,
                COUNT, TIME_FRAME, SAMPLES, false);

        collection.insertMany(randomInput);

        EffectiveTimeFrame expected = getExpectedTimeFrame(Long.MAX_VALUE, Long.MIN_VALUE,
                randomInput);

        expected = getExpectedTimeFrame(
                RadarConverter.getISO8601(expected.getStartDateTime()).getTime(),
                RadarConverter.getISO8601(expected.getEndDateTime()).getTime(),
                randomInput);

        EffectiveTimeFrame actual = SensorDataAccessObject.getInstance()
                .getUserEffectiveTimeFrame(SUBJECT, client);

        assertEquals(expected, actual);

        dropAndClose(client);
    }

    private EffectiveTimeFrame getExpectedTimeFrame(long start, long end, List<Document> docs) {
        long expectedStart = start;
        long expectedEnd = end;

        for (Document doc : docs) {
            expectedStart = Math.min(expectedStart, doc.getDate(START).getTime());
            expectedEnd = Math.max(expectedEnd, doc.getDate(END).getTime());
        }

        return new EffectiveTimeFrame(
            RadarConverter.getISO8601(expectedStart),
            RadarConverter.getISO8601(expectedEnd));
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

        for (String collectionName: dirtyCollections) {
            Utility.dropCollection(client, collectionName);
        }
        client.close();
    }
}
