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
import static org.junit.Assert.assertTrue;
import static org.radarcns.domain.restapi.ServerStatus.UNKNOWN;
import static org.radarcns.webapp.SampleDataHandler.PROJECT;
import static org.radarcns.webapp.SampleDataHandler.SOURCE;
import static org.radarcns.webapp.SampleDataHandler.SUBJECT;
import static org.radarcns.webapp.resource.BasePath.APPLICATION_STATUS;

import java.io.IOException;
import java.util.Map;
import javax.ws.rs.core.Response.Status;

import org.bson.Document;
import org.junit.Rule;
import org.junit.Test;
import org.radarcns.domain.restapi.monitor.ApplicationStatus;
import org.radarcns.domain.restapi.monitor.MonitorData;
import org.radarcns.integration.MongoRule;
import org.radarcns.integration.util.ApiClient;
import org.radarcns.integration.util.RandomInput;
import org.radarcns.integration.util.RestApiDetails;
import org.radarcns.integration.util.Utility;


public class AppStatusEndPointTest {

    private static final String SOURCE_PATH =
            APPLICATION_STATUS + '/' + PROJECT + '/' + SUBJECT + '/' + SOURCE;

    @Rule
    public final ApiClient apiClient = new ApiClient(
            RestApiDetails.getRestApiClientDetails().getApplicationConfig().getUrlString());

    @Rule
    public final MongoRule mongoRule = new MongoRule();

    @Test
    public void getStatusTest200Unknown() throws IOException {
        MonitorData actual =
                apiClient.getJson(SOURCE_PATH, MonitorData.class, Status.OK);
        Map<String, String> status = (Map<String, String>) actual.getData();

        assertEquals(status.get("serverStatus"), UNKNOWN.toString());
    }

    @Test
    public void getStatusTest200() throws IOException {
        Map<String, Document> map = RandomInput.getRandomApplicationStatus(
                PROJECT, SUBJECT, SOURCE);

        map.forEach((k, v) -> mongoRule.getCollection(k).insertOne(v));

        ApplicationStatus expected = Utility.convertDocToApplication(map);
        MonitorData actual =
                apiClient.getJson(SOURCE_PATH, MonitorData.class, Status.OK);

        assertTrue(actual.getData() instanceof Map);
        Map<String, String> status = (Map<String, String>) actual.getData();

        assertEquals(expected.getServerStatus().toString(), status.get("serverStatus"));
    }
}
