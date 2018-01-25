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

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.bson.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.radarcns.mongo.util.MongoHelper;
import org.radarcns.domain.restapi.header.EffectiveTimeFrame;
import org.radarcns.integration.util.Utility;
import org.radarcns.domain.managementportal.SourceType;
import org.radarcns.util.RadarConverter;

public class SourceMonitorDbTest {

    private static final String SUBJECT_ID = "sub-1";
    private static final String SOURCE_ID = "03d28e5c-e005-46d4-a9b3-279c27fbbc83";
    private static final String SOURCETYPE_PRODUCER = "EMPATICA";
    private static final String SOURCETYPE_MODEL = "E4";
    private static final String SOURCETYPE_CATALOGUE_VERSION = "v1";
    private static final String MONITOR_STATISTICS_TOPIC = "empatica_e4_statistics";

    private static int WINDOWS = 2;

    private static MongoClient mongoClient = Utility.getMongoClient();

    private static SourceType sourceType;

    private static SourceMonitor monitor;

    @Before
    public void setUp() {
        sourceType = new SourceType();
        sourceType.setId(1);
        sourceType.setProducer(SOURCETYPE_PRODUCER);
        sourceType.setCanRegisterDynamically(false);
        sourceType.setModel(SOURCETYPE_MODEL);
        sourceType.setCatalogVersion(SOURCETYPE_CATALOGUE_VERSION);
        sourceType.setSourceStatisticsMonitorTopic(MONITOR_STATISTICS_TOPIC);
        sourceType.setSourceTypeScope("PASSIVE");
        monitor = new SourceMonitor(mongoClient);
    }

    @Test
    public void testEffectiveTime() {
        long start = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(10);
        long end = start + TimeUnit.SECONDS.toMillis(60 / (WINDOWS + 1));
        Document doc = getDocumentsForStatistics(start, end);
        MongoCollection collection = MongoHelper.getCollection(mongoClient, sourceType
                .getSourceStatisticsMonitorTopic());
        collection.insertOne(doc);

        EffectiveTimeFrame result = monitor.getEffectiveTimeFrame(SUBJECT_ID, SOURCE_ID, sourceType);

        assertEquals(result.getStartDateTime(), RadarConverter.getISO8601(start));
        assertEquals(result.getEndDateTime(), RadarConverter.getISO8601(end));
    }


    @Test
    public void testEffectiveTimeWithMultipleDocuments() {
        long start = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(10);
        long end = start + TimeUnit.SECONDS.toMillis(60 / (WINDOWS + 1));
        long later = end + TimeUnit.SECONDS.toMillis(60 / (WINDOWS + 1));
        Document doc = getDocumentsForStatistics(start, end);
        Document second = getDocumentsForStatistics(start, later);
        MongoCollection collection = MongoHelper.getCollection(mongoClient, sourceType
                .getSourceStatisticsMonitorTopic());
        collection.insertMany(Arrays.asList(doc, second));

        EffectiveTimeFrame result = monitor.getEffectiveTimeFrame(SUBJECT_ID, SOURCE_ID, sourceType);

        assertEquals(result.getStartDateTime(), RadarConverter.getISO8601(start));
        assertEquals(result.getEndDateTime(), RadarConverter.getISO8601(later));
    }

    @After
    public void cleanUp() {
        Utility.dropCollection(mongoClient, sourceType.getSourceStatisticsMonitorTopic());
    }


    private static Document getDocumentsForStatistics(long start, long end) {
        return new Document(MongoHelper.ID, SUBJECT_ID + "-" + SOURCE_ID + "-" + start + "-" + end)
                .append(MongoHelper.USER_ID, SUBJECT_ID)
                .append(MongoHelper.SOURCE_ID, SOURCE_ID)
                .append(MongoHelper.PROJECT_ID, "radar")
                .append(MongoHelper.START, new Date(start))
                .append(MongoHelper.END, new Date(end));
    }

}
