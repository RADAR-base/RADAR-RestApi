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

import static org.junit.Assert.assertTrue;
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


    private static final String PROJECT = "radar";
    private static final String SUBJECT = "sub-1";

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
}
