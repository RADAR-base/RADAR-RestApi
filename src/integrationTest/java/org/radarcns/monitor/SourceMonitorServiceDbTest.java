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
import static org.radarcns.integration.util.ExpectedDocumentFactory.getDocumentsForStatistics;
import static org.radarcns.webapp.SampleDataHandler.PROJECT;
import static org.radarcns.webapp.SampleDataHandler.SOURCE;
import static org.radarcns.webapp.SampleDataHandler.SUBJECT;

import com.mongodb.client.MongoCollection;
import java.time.Instant;
import java.util.Arrays;
import org.bson.Document;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.radarcns.domain.managementportal.SourceTypeDTO;
import org.radarcns.domain.restapi.header.TimeFrame;
import org.radarcns.integration.MongoRule;
import org.radarcns.integration.util.Utility;
import org.radarcns.mongo.util.MongoWrapper;
import org.radarcns.service.SourceMonitorService;

public class SourceMonitorServiceDbTest {

    private static final String SOURCETYPE_PRODUCER = "EMPATICA";
    private static final String SOURCETYPE_MODEL = "E4";
    private static final String SOURCETYPE_CATALOGUE_VERSION = "v1";
    private static final String MONITOR_STATISTICS_TOPIC = "Empatica_E4_statistics";

    private static final int WINDOWS = 2;

    @Rule
    public final MongoRule mongoRule = new MongoRule(Utility.getConfig());

    private SourceTypeDTO sourceType;

    private SourceMonitorService monitor;
    private MongoWrapper client;

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
        client = mongoRule.getClient();
        monitor = new SourceMonitorService(client);
    }

    @Test
    public void testEffectiveTime() {
        Instant start = Instant.now();
        Instant end = start.plusSeconds(60 / (WINDOWS + 1));
        Document doc = getDocumentsForStatistics(PROJECT, SUBJECT, SOURCE, start, end);
        MongoCollection<Document> collection = client.getCollection(
                sourceType.getSourceStatisticsMonitorTopic());
        collection.insertOne(doc);

        TimeFrame result = monitor.getEffectiveTimeFrame(PROJECT, SUBJECT, SOURCE, sourceType);

        assertEquals(start, result.getStartDateTime());
        assertEquals(end, result.getEndDateTime());
    }

    @Test
    public void testEffectiveTimeWithMultipleDocuments() {
        Instant start = Instant.now();
        Instant end = start.plusSeconds(60);
        Instant earlier = start.minusSeconds(10);
        Instant later = end.plusSeconds(5);
        Document doc = getDocumentsForStatistics(PROJECT, SUBJECT, SOURCE, start, end);
        Document second = getDocumentsForStatistics(PROJECT, SUBJECT, SOURCE, earlier, later);
        MongoCollection<Document> collection = client.getCollection(
                sourceType.getSourceStatisticsMonitorTopic());
        collection.insertMany(Arrays.asList(doc, second));

        TimeFrame result = monitor.getEffectiveTimeFrame(PROJECT, SUBJECT, SOURCE, sourceType);

        assertEquals(earlier, result.getStartDateTime());
        assertEquals(later, result.getEndDateTime());
    }
}