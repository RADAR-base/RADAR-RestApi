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
import static org.radarcns.integration.util.ExpectedDocumentFactory.buildDocument;
import static org.radarcns.mongo.util.MongoHelper.END;
import static org.radarcns.mongo.util.MongoHelper.START;

import com.mongodb.client.MongoCollection;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.bson.Document;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.radarcns.domain.managementportal.SourceTypeDTO;
import org.radarcns.domain.restapi.header.TimeFrame;
import org.radarcns.integration.MongoRule;
import org.radarcns.service.SourceMonitorService;

public class SourceMonitorServiceDbTest {

    private static final String PROJECT_NAME = "radar";
    private static final String SUBJECT_ID = "sub-1";
    private static final String SOURCE_ID = "03d28e5c-e005-46d4-a9b3-279c27fbbc83";
    private static final String SOURCETYPE_PRODUCER = "EMPATICA";
    private static final String SOURCETYPE_MODEL = "E4";
    private static final String SOURCETYPE_CATALOGUE_VERSION = "v1";
    private static final String MONITOR_STATISTICS_TOPIC = "Empatica_E4_statistics";

    private static final int WINDOWS = 2;


    @Rule
    public final MongoRule mongoRule = new MongoRule();

    private static SourceTypeDTO sourceType;

    private static SourceMonitorService monitor;

    /**
     * Initializes common objects required for the test.
     */
    @Before
    public void setUp() {
        sourceType = new SourceTypeDTO();
        sourceType.setProducer(SOURCETYPE_PRODUCER);
        sourceType.setCanRegisterDynamically(false);
        sourceType.setModel(SOURCETYPE_MODEL);
        sourceType.setCatalogVersion(SOURCETYPE_CATALOGUE_VERSION);
        sourceType.setSourceStatisticsMonitorTopic(MONITOR_STATISTICS_TOPIC);
        sourceType.setSourceTypeScope("PASSIVE");
        monitor = new SourceMonitorService(mongoRule.getClient());
    }

    @Test
    public void testEffectiveTime() {
        Date start = Date.from(Instant.now());
        Date end = Date.from(Instant
                .ofEpochSecond(start.getTime()
                        + TimeUnit.SECONDS.toMillis(60 / (WINDOWS + 1))));
        Document doc = getDocumentsForStatistics(start, end);
        MongoCollection<Document> collection = mongoRule.getCollection(
                sourceType.getSourceStatisticsMonitorTopic());
        collection.insertOne(doc);

        TimeFrame result = monitor.getEffectiveTimeFrame(PROJECT_NAME, SUBJECT_ID,
                SOURCE_ID, sourceType);

        assertEquals(start.toInstant(), result.getStartDateTime());
        assertEquals(end.toInstant(), result.getEndDateTime());
    }


    @Test
    public void testEffectiveTimeWithMultipleDocuments() {
        Date start = Date.from(Instant.now());
        Date end = Date.from(start.toInstant().plusSeconds(60));
        Date earlier = Date.from(start.toInstant().minusSeconds(10));
        Date later = Date.from(end.toInstant().plusSeconds(65));
        Document doc = getDocumentsForStatistics(start, end);
        Document second = getDocumentsForStatistics(earlier, later);
        MongoCollection<Document> collection = mongoRule.getCollection(
                sourceType.getSourceStatisticsMonitorTopic());
        collection.insertMany(Arrays.asList(doc, second));

        TimeFrame result = monitor.getEffectiveTimeFrame(PROJECT_NAME, SUBJECT_ID,
                SOURCE_ID, sourceType);

        assertEquals(earlier.toInstant(), result.getStartDateTime());
        assertEquals(later.toInstant(), result.getEndDateTime());
    }

    private static Document getDocumentsForStatistics(Object start, Object end) {
        Document value = new Document()
                .append(START, start)
                .append(END, end);
        return buildDocument(PROJECT_NAME, SUBJECT_ID, SOURCE_ID, start, end, value);
    }
}