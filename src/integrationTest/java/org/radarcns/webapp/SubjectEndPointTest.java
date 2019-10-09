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

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.radarcns.domain.restapi.SourceStatus.CONNECTED;
import static org.radarcns.integration.util.ExpectedDocumentFactory.getDocumentsForStatistics;
import static org.radarcns.webapp.SampleDataHandler.MONITOR_STATISTICS_TOPIC;
import static org.radarcns.webapp.SampleDataHandler.PROJECT;
import static org.radarcns.webapp.SampleDataHandler.SOURCE;
import static org.radarcns.webapp.SampleDataHandler.SUBJECT;
import static org.radarcns.webapp.resource.BasePath.SUBJECTS;

import com.mongodb.client.MongoCollection;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import javax.ws.rs.core.Response.Status;
import org.bson.Document;
import org.junit.Rule;
import org.junit.Test;
import org.radarcns.domain.restapi.Subject;
import org.radarcns.integration.MongoRule;
import org.radarcns.integration.util.ApiClient;
import org.radarcns.integration.util.RestApiDetails;
import org.radarcns.integration.util.Utility;
import org.radarcns.webapp.resource.BasePath;

public class SubjectEndPointTest {

    @Rule
    public final ApiClient apiClient = new ApiClient(
            RestApiDetails.getRestApiClientDetails().getApplicationConfig().getUrlString());

    @Rule
    public final MongoRule mongoRule = new MongoRule(Utility.getConfig());

    @Test
    public void getSubjectsByProjectName200() throws IOException {
        Instant now = Instant.now();
        insertMonitorStatistics(now.minus(Duration.ofMinutes(30)), now);

        List<Subject> subjects = apiClient.getJsonList(
                BasePath.PROJECTS + '/' + PROJECT + '/' + SUBJECTS,
                Subject.class, Status.OK);
        assertNotNull(subjects);
        assertTrue(subjects.size() > 0);
        assertEquals(PROJECT, subjects.get(0).getProject());
        assertTrue(subjects.get(2).getSources().get(0).getSourceTypeId() > 0);

    }

    @Test
    public void getSubjectsBySubjectIdAndProjectName200() throws IOException {
        Instant now = Instant.now();
        insertMonitorStatistics(now.minus(Duration.ofMinutes(30)), now);

        Subject subject = apiClient.getJson(
                BasePath.PROJECTS + '/' + PROJECT + '/' + SUBJECTS + '/' + SUBJECT,
                Subject.class, Status.OK);

        assertNotNull(subject);
        assertEquals(SUBJECT, subject.getSubjectId());
        assertEquals(PROJECT, subject.getProject());
        assertTrue(subject.getSources().size() > 0);
        assertNotNull(subject.getSources().get(0)
                .getEffectiveTimeFrame()
                .getStartDateTime());
        assertNotNull(subject.getSources().get(0)
                .getEffectiveTimeFrame()
                .getEndDateTime());
        assertEquals(CONNECTED, subject.getSources().get(0).getStatus());
        assertNotNull(subject.getLastSeen());
        assertTrue(subject.getSources().get(0).getSourceTypeId() > 0);

    }

    @Test
    public void getSubjectTest404() throws IOException {
        assertNotNull(apiClient.get(
                BasePath.PROJECTS + '/' + PROJECT + '/' + SUBJECTS + "/OTHER",
                APPLICATION_JSON, Status.NOT_FOUND));
    }

    @Test
    public void getSubjectsByProjectName200WhenNoSourceStatisticsAvailable() throws IOException {
        List<Subject> subjects = apiClient.getJsonList(
                BasePath.PROJECTS + '/' + PROJECT + '/' + SUBJECTS,
                Subject.class, Status.OK);
        assertNotNull(subjects);
        assertTrue(subjects.size() > 0);
        assertEquals(PROJECT, subjects.get(0).getProject());
        assertNull(subjects.get(0).getLastSeen());
    }

    private void insertMonitorStatistics(Instant startTime, Instant end) {
        Document doc = getDocumentsForStatistics(PROJECT, SUBJECT, SOURCE, startTime, end);
        Document second = getDocumentsForStatistics(PROJECT, SUBJECT, SOURCE, startTime,
                end.plus(Duration.ofMinutes(5)));
        MongoCollection<Document> collection = mongoRule.getCollection(MONITOR_STATISTICS_TOPIC);
        collection.insertMany(Arrays.asList(doc, second));
    }

}
