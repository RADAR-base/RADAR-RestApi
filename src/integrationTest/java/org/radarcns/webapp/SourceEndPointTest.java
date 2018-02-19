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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.radarcns.webapp.resource.BasePath.PROJECTS;
import static org.radarcns.webapp.resource.BasePath.SOURCES;
import static org.radarcns.webapp.resource.BasePath.SUBJECTS;

import com.fasterxml.jackson.databind.ObjectReader;
import com.mongodb.MongoClient;
import java.io.IOException;
import java.util.List;
import javax.ws.rs.core.Response.Status;
import okhttp3.Response;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.radarcns.domain.restapi.Source;
import org.radarcns.integration.util.ApiClient;
import org.radarcns.integration.util.RestApiDetails;
import org.radarcns.integration.util.Utility;
import org.radarcns.util.RadarConverter;

public class SourceEndPointTest {


    private static final String PROJECT = "radar";
    private static final String SUBJECT = "sub-1";

    @Rule
    public final ApiClient apiClient = new ApiClient(
            RestApiDetails.getRestApiClientDetails().getApplicationConfig().getUrlString());


    @Test
    public void getAllSourcesForSubjectInProject() throws IOException {
        String requestPath = PROJECTS + '/' + PROJECT + '/' + SUBJECTS + '/' + SUBJECT + '/'
                + SOURCES;
        try (Response response = apiClient.request(requestPath, APPLICATION_JSON, Status.OK)) {
            assertNotNull(response);
            assertTrue(response.isSuccessful());

            ObjectReader reader = RadarConverter.readerForCollection(List.class, Source.class);
            List<Source> sources = reader.readValue(response.body().byteStream());
            assertTrue(sources.size() > 0);
        }
    }

    @After
    public void dropAndClose() {
        dropAndClose(Utility.getMongoClient());
    }

    /**
     * Drops all used collections to bring the database back to the initial state, and close the
     * database connection.
     **/
    public void dropAndClose(MongoClient client) {
        client.close();
    }

}
