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
import static org.radarcns.domain.restapi.header.MonitorHeader.MonitorCategory.PASSIVE;
import static org.radarcns.domain.restapi.header.MonitorHeader.MonitorCategory.QUESTIONNAIRE;
import static org.radarcns.mongo.data.monitor.questionnaire.QuestionnaireCompletionLogWrapper.QUESTIONNAIRE_COMPLETION_LOG_COLLECTION;
import static org.radarcns.webapp.SampleDataHandler.PROJECT;
import static org.radarcns.webapp.resource.BasePath.APPLICATION_STATUS;

import java.io.IOException;
import java.util.Map;
import javax.ws.rs.core.Response.Status;

import org.bson.Document;
import org.junit.Rule;
import org.junit.Test;
import org.radarcns.domain.restapi.header.MonitorHeader;
import org.radarcns.domain.restapi.monitor.ApplicationStatus;
import org.radarcns.domain.restapi.monitor.MonitorData;
import org.radarcns.domain.restapi.monitor.QuestionnaireCompletionStatus;
import org.radarcns.integration.MongoRule;
import org.radarcns.integration.util.ApiClient;
import org.radarcns.integration.util.RandomInput;
import org.radarcns.integration.util.RestApiDetails;
import org.radarcns.integration.util.Utility;


public class AppStatusEndPointTest {


    @Rule
    public final ApiClient apiClient = new ApiClient(
            RestApiDetails.getRestApiClientDetails().getApplicationConfig().getUrlString());

    @Rule
    public final MongoRule mongoRule = new MongoRule();

    @Test
    public void getStatusTest200Unknown() throws IOException {
        String sourceId = "c4064d12-b963-47ae-b560-067131e321ea";
        String subjectId = "sub-2";

        MonitorHeader monitorHeader = new MonitorHeader(PROJECT, subjectId, sourceId, null);
        assertRequestsMatch(subjectId, sourceId, monitorHeader);
    }

    @Test
    public void getStatusTest200() throws IOException {
        String sourceId = "c4064d12-b963-47ae-b560-067131e321ea";
        String subjectId = "sub-2";

        Map<String, Document> map = RandomInput.getRandomApplicationStatus(
                PROJECT, subjectId, sourceId);

        map.forEach((k, v) -> mongoRule.getCollection(k).insertOne(v));

        ApplicationStatus expected = Utility.convertDocToApplicationStatus(map);
        MonitorHeader monitorHeader = new MonitorHeader(PROJECT, subjectId, sourceId, PASSIVE);
        MonitorData actual = assertRequestsMatch(subjectId, sourceId, monitorHeader);

        assertTrue(actual.getData() instanceof Map);
        Map<String, String> status = (Map<String, String>) actual.getData();

        assertEquals(expected.getServerStatus().toString(), status.get("serverStatus"));
    }

    @Test
    public void getQuestionnaireCompletionStatusTest200() throws IOException {
        String sourceId = "0d29b9eb-289a-4dc6-b969-534dca72a187";
        String subjectId = "sub-3";

        Document document = RandomInput.getRandomQuestionnaireCompletionLog(
                PROJECT, subjectId, sourceId);

        mongoRule.getCollection(QUESTIONNAIRE_COMPLETION_LOG_COLLECTION).insertOne(document);

        QuestionnaireCompletionStatus expected = Utility
                .convertDocToQuestionnaireCompletionStatus(document);
        MonitorHeader monitorHeader = new MonitorHeader(PROJECT, subjectId, sourceId,
                QUESTIONNAIRE);
        MonitorData actual = assertRequestsMatch(subjectId, sourceId, monitorHeader);

        assertTrue(actual.getData() instanceof Map);
        Map<String, String> status = (Map<String, String>) actual.getData();

        assertEquals(expected.getCompletionPercentage(), status.get("completionPercentage"));
    }

    private MonitorData assertRequestsMatch(String subjectId, String sourceId, MonitorHeader
            expectedHeader)
            throws IOException {
        String relativeUrl =
                APPLICATION_STATUS + '/' + PROJECT + '/' + subjectId + '/' + sourceId;

        MonitorData actual = apiClient.getJson(relativeUrl, MonitorData.class, Status.OK);
        assertNotNull(actual);
        MonitorHeader actualHeader = actual.getHeader();
        assertEquals(expectedHeader.getProjectId(), actualHeader.getProjectId());
        assertEquals(expectedHeader.getSubjectId(), actualHeader.getSubjectId());
        assertEquals(expectedHeader.getSourceId(), actualHeader.getSourceId());
        return actual;
    }
}
