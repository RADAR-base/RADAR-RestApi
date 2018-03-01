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
import static org.junit.Assert.assertSame;
import static org.radarcns.webapp.resource.BasePath.APPLICATION_STATUS;

import java.io.IOException;
import java.util.Map;
import javax.ws.rs.core.Response.Status;
import org.bson.Document;
import org.junit.Rule;
import org.junit.Test;
import org.radarcns.domain.restapi.Application;
import org.radarcns.domain.restapi.ServerStatus;
import org.radarcns.integration.MongoRule;
import org.radarcns.integration.util.ApiClient;
import org.radarcns.integration.util.RandomInput;
import org.radarcns.integration.util.RestApiDetails;
import org.radarcns.integration.util.Utility;


public class AppStatusEndPointTest {

    private static final String PROJECT = "radar";
    private static final String SUBJECT = "sub-1";
    private static final String SOURCE = "03d28e5c-e005-46d4-a9b3-279c27fbbc83";
    private static final String SOURCE_PATH =
            APPLICATION_STATUS + '/' + PROJECT + '/' + SUBJECT + '/' + SOURCE;

    @Rule
    public final ApiClient apiClient = new ApiClient(
            RestApiDetails.getRestApiClientDetails().getApplicationConfig().getUrlString());

    @Rule
    public final MongoRule mongoRule = new MongoRule();

    @Test
    public void getStatusTest200Unknown() throws IOException {
        Application actual = apiClient.getJson(SOURCE_PATH, Application.class, Status.OK);
        assertSame(ServerStatus.UNKNOWN, actual.getServerStatus());
    }

    @Test
    public void getStatusTest200() throws IOException {
        Map<String, Document> map = RandomInput.getRandomApplicationStatus(
                PROJECT, SUBJECT, SOURCE);

        map.forEach((k, v) -> mongoRule.getCollection(k).insertOne(v));

        Application expected = Utility.convertDocToApplication(map);
        Application actual = apiClient.getJson(SOURCE_PATH, Application.class, Status.OK);

        assertEquals(expected.getServerStatus(), actual.getServerStatus());
        assertEquals(expected.getIpAddress(), actual.getIpAddress());
    }
}
