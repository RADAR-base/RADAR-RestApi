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
import static org.junit.Assert.assertTrue;
import static org.radarcns.integration.util.ExpectedDocumentFactory.buildDocument;
import static org.radarcns.mongo.util.MongoHelper.END;
import static org.radarcns.mongo.util.MongoHelper.START;
import static org.radarcns.webapp.resource.BasePath.SUBJECTS;

import com.mongodb.client.MongoCollection;
import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.ws.rs.core.Response.Status;
import org.bson.Document;
import org.junit.Rule;
import org.junit.Test;
import org.radarcns.domain.restapi.Subject;
import org.radarcns.integration.MongoRule;
import org.radarcns.integration.util.ApiClient;
import org.radarcns.integration.util.RestApiDetails;
import org.radarcns.webapp.resource.BasePath;

public class SubjectEndPointTest {

    private static final String PROJECT_NAME = "radar";
    private static final String SUBJECT_ID = "sub-1";
    private static final String SOURCE_ID = "03d28e5c-e005-46d4-a9b3-279c27fbbc83";
    private static final String MONITOR_STATISTICS_TOPIC = "source_statistics_empatica_e4";

    @Rule
    public final ApiClient apiClient = new ApiClient(
            RestApiDetails.getRestApiClientDetails().getApplicationConfig().getUrlString());

    @Rule
    public final MongoRule mongoRule = new MongoRule();

    @Test
    public void getSubjectsByProjectName200() throws IOException {
        insertMonitorStatistics();

        List<Subject> subjects = apiClient.getJsonList(
                BasePath.PROJECTS + '/' + PROJECT_NAME + '/' + SUBJECTS,
                Subject.class, Status.OK);

        assertNotNull(subjects);
        assertTrue(subjects.size() > 0);
        assertEquals(PROJECT_NAME, subjects.get(0).getProject());

    }

    private static Document getDocumentsForStatistics(Object start, Object end) {
        Document value = new Document()
                .append(START, start)
                .append(END, end);
        return buildDocument(PROJECT_NAME, SUBJECT_ID, SOURCE_ID, start, end, value);
    }

    private void insertMonitorStatistics() {
        Date start = Date.from(Instant.now());
        Date end = Date.from(start.toInstant().plusSeconds(60));
        Date later = Date.from(end.toInstant().plusSeconds(65));
        Document doc = getDocumentsForStatistics(start, end);
        Document second = getDocumentsForStatistics(start, later);
        MongoCollection<Document> collection = mongoRule.getCollection(MONITOR_STATISTICS_TOPIC);
        collection.insertMany(Arrays.asList(doc, second));
    }

    @Test
    public void getSubjectsBySubjectIdAndProjectName200() throws IOException {
        insertMonitorStatistics();

        Subject subject = apiClient.getJson(
                BasePath.PROJECTS + '/' + PROJECT_NAME + '/' + SUBJECTS + '/' + SUBJECT_ID,
                Subject.class, Status.OK);

        assertNotNull(subject);
        assertEquals(SUBJECT_ID, subject.getSubjectId());
        assertEquals(PROJECT_NAME, subject.getProject());
        assertTrue(subject.getSources().size() > 0);
        assertNotNull(subject.getSources().get(0)
                .getEffectiveTimeFrame()
                .getStartDateTime());
        assertNotNull(subject.getSources().get(0)
                .getEffectiveTimeFrame()
                .getEndDateTime());
        assertNotNull(subject.getLastSeen());

    }

    @Test
    public void getSubjectTest404() throws IOException {
        assertNotNull(apiClient.get(
                BasePath.PROJECTS + '/' + PROJECT_NAME + '/' + SUBJECTS + "/OTHER",
                APPLICATION_JSON, Status.NOT_FOUND));
    }
}
