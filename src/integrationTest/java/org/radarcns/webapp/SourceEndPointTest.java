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
import static org.radarcns.webapp.SampleDataHandler.PROJECT;
import static org.radarcns.webapp.SampleDataHandler.SUBJECT;
import static org.radarcns.webapp.resource.BasePath.PROJECTS;
import static org.radarcns.webapp.resource.BasePath.SOURCES;
import static org.radarcns.webapp.resource.BasePath.SUBJECTS;

import java.io.IOException;
import java.util.List;
import javax.ws.rs.core.Response.Status;
import org.junit.Rule;
import org.junit.Test;
import org.radarcns.domain.restapi.Source;
import org.radarcns.integration.util.ApiClient;
import org.radarcns.integration.util.RestApiDetails;

public class SourceEndPointTest {

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

        List<Source> sources = requestSourcesOfSubject();
        assertEquals(SourceStatus.DISCONNECTED, sources.get(0).getStatus());
    }

    @Test
    public void getAllSourcesForSubjectInProjectConnected() throws IOException {
        Instant now = Instant.now();
        insertMonitorStatistics(now.minus(Duration.ofMinutes(30)), now);

        // not effective time available
        List<Source> sources = requestSourcesOfSubject();
        assertEquals(SourceStatus.CONNECTED, sources.get(0).getStatus());
    }

    @Test
    public void getSourceByIdConnected() throws IOException {
        Instant now = Instant.now();
        insertMonitorStatistics(now.minus(Duration.ofMinutes(30)), now);

        String requestPath = PROJECTS + '/' + PROJECT + '/' + SUBJECTS + '/' + SUBJECT + '/'
                + SOURCES + '/' + SOURCE;
        try (Response response = apiClient.request(requestPath, APPLICATION_JSON, Status.OK)) {
            assertNotNull(response);
            assertTrue(response.isSuccessful());

            ObjectReader reader = RadarConverter.readerFor(Source.class);
            Source sources = reader.readValue(response.body().byteStream());
            assertNotNull(sources);
            assertEquals(SourceStatus.CONNECTED, sources.getStatus());
        }
    }

    @Test
    public void getSourceByIdInvalidConnected() throws IOException {
        Instant now = Instant.now();
        insertMonitorStatistics(now.minus(Duration.ofMinutes(30)), now);

        String requestPath = PROJECTS + '/' + PROJECT + '/' + SUBJECTS + '/' + SUBJECT + '/'
                + SOURCES + '/' + "something";
        try (Response response = apiClient
                .request(requestPath, APPLICATION_JSON, Status.NOT_FOUND)) {
            assertNotNull(response);
        }
    }

    private List<Source> requestSourcesOfSubject() throws IOException {
        String requestPath = PROJECTS + '/' + PROJECT + '/' + SUBJECTS + '/' + SUBJECT + '/'
                + SOURCES;

        List<Source> sources = apiClient.getJsonList(requestPath, Source.class, Status.OK);
        assertTrue(sources.size() > 0);
    }

}

    @After
    public void dropAndClose() {
        Utility.dropCollection(Utility.getMongoClient(), MONITOR_STATISTICS_TOPIC);
    }


}
