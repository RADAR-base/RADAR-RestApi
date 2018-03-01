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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.radarcns.domain.restapi.SourceStatus.CONNECTED;
import static org.radarcns.domain.restapi.SourceStatus.DISCONNECTED;
import static org.radarcns.integration.util.ExpectedDocumentFactory.getDocumentsForStatistics;
import static org.radarcns.webapp.SampleDataHandler.MONITOR_STATISTICS_TOPIC;
import static org.radarcns.webapp.SampleDataHandler.PROJECT;
import static org.radarcns.webapp.SampleDataHandler.SOURCE;
import static org.radarcns.webapp.SampleDataHandler.SUBJECT;
import static org.radarcns.webapp.resource.BasePath.PROJECTS;
import static org.radarcns.webapp.resource.BasePath.SOURCES;
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
import org.radarcns.domain.restapi.Source;
import org.radarcns.integration.MongoRule;
import org.radarcns.integration.util.ApiClient;
import org.radarcns.integration.util.RestApiDetails;

public class SourceEndPointTest {

    @Rule
    public final MongoRule mongoRule = new MongoRule();

    @Rule
    public final ApiClient apiClient = new ApiClient(
            RestApiDetails.getRestApiClientDetails().getApplicationConfig().getUrlString());

    @Test
    public void getAllSourcesForSubjectInProject() throws IOException {
        String requestPath = PROJECTS + '/' + PROJECT + '/' + SUBJECTS + '/' + SUBJECT + '/'
                + SOURCES;

        List<Source> sources = apiClient.getJsonList(requestPath, Source.class, Status.OK);
        assertTrue(sources.size() > 0);
    }

    @Test
    public void getAllSourcesForSubjectInProjectDisconnected() throws IOException {
        Instant now = Instant.now();
        insertMonitorStatistics(now.minus(Duration.ofDays(2)), now.minus(Duration.ofHours(20)));

        String requestPath = PROJECTS + '/' + PROJECT + '/' + SUBJECTS + '/' + SUBJECT + '/'
                + SOURCES;

        List<Source> sources = apiClient.getJsonList(requestPath, Source.class, Status.OK);
        assertEquals(DISCONNECTED, sources.get(0).getStatus());
    }

    @Test
    public void getAllSourcesForSubjectInProjectConnected() throws IOException {
        Instant now = Instant.now();
        insertMonitorStatistics(now.minus(Duration.ofMinutes(30)), now);
        String requestPath = PROJECTS + '/' + PROJECT + '/' + SUBJECTS + '/' + SUBJECT + '/'
                + SOURCES;

        List<Source> sources = apiClient.getJsonList(requestPath, Source.class, Status.OK);
        assertEquals(CONNECTED, sources.get(0).getStatus());
    }

    @Test
    public void getSourceByIdConnected() throws IOException {
        Instant now = Instant.now();
        insertMonitorStatistics(now.minus(Duration.ofMinutes(30)), now);

        String requestPath = PROJECTS + '/' + PROJECT + '/' + SUBJECTS + '/' + SUBJECT + '/'
                + SOURCES + '/' + SOURCE;
        Source source = apiClient.getJson(requestPath, Source.class, Status.OK);
        assertNotNull(source);
        assertEquals(CONNECTED, source.getStatus());
    }

    @Test
    public void getSourceByIdInvalidConnected() throws IOException {
        Instant now = Instant.now();
        insertMonitorStatistics(now.minus(Duration.ofMinutes(30)), now);

        String requestPath = PROJECTS + '/' + PROJECT + '/' + SUBJECTS + '/' + SUBJECT + '/'
                + SOURCES + '/' + "something";
        apiClient.getJson(requestPath, Source.class, Status.NOT_FOUND);
    }


    private void insertMonitorStatistics(Instant startTime, Instant end) {
        Document doc = getDocumentsForStatistics(PROJECT, SUBJECT, SOURCE, startTime, end);
        Document second = getDocumentsForStatistics(PROJECT, SUBJECT, SOURCE, startTime,
                end.plus(Duration.ofMinutes(5)));
        MongoCollection<Document> collection = mongoRule.getCollection(MONITOR_STATISTICS_TOPIC);
        collection.insertMany(Arrays.asList(doc, second));
    }

}
