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
import static org.radarcns.domain.restapi.header.DescriptiveStatistic.COUNT;
import static org.radarcns.mongo.data.applicationstatus.ApplicationStatusRecordCounter.RECORD_COLLECTION;
import static org.radarcns.mongo.data.applicationstatus.ApplicationStatusServerStatus.STATUS_COLLECTION;
import static org.radarcns.mongo.data.applicationstatus.ApplicationStatusUpTime.UPTIME_COLLECTION;
import static org.radarcns.webapp.resource.BasePath.APPLICATION_STATUS;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response.Status;
import org.bson.Document;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.radarcns.domain.restapi.Application;
import org.radarcns.domain.restapi.ServerStatus;
import org.radarcns.domain.restapi.TimeWindow;
import org.radarcns.integration.util.ApiClient;
import org.radarcns.integration.util.RandomInput;
import org.radarcns.integration.util.RestApiDetails;
import org.radarcns.integration.util.Utility;
import org.radarcns.mongo.util.MongoHelper;

public class AppStatusEndPointTest {

    private static final String PROJECT = "radar";
    private static final String SUBJECT = "sub-1";
    private static final String SOURCE = "03d28e5c-e005-46d4-a9b3-279c27fbbc83";
    private static final String SOURCE_TYPE = "empatica_e4_v1";
    private static final String SENSOR_TYPE = "HEART_RATE";
    private static final TimeWindow TIME_WINDOW = TimeWindow.TEN_SECOND;
    private static final int SAMPLES = 10;
    private static final String SOURCE_PATH =
            APPLICATION_STATUS + '/' + PROJECT + '/' + SUBJECT + '/' + SOURCE;
    private static final String COLLECTION_NAME = "android_empatica_e4_heartrate_10sec";

    @Rule
    public final ApiClient apiClient = new ApiClient(
            RestApiDetails.getRestApiClientDetails().getApplicationConfig().getUrlString());

    @Test
    public void getStatusTest200Unknown() throws IOException, ReflectiveOperationException {
        Application actual = apiClient.requestJson(SOURCE_PATH, Application.class, Status.OK);
        assertSame(ServerStatus.UNKNOWN, actual.getServerStatus());
    }

    @Test
    public void getStatusTest200()
            throws IOException, ReflectiveOperationException {
        MongoClient client = Utility.getMongoClient();

        MongoCollection<Document> collection = MongoHelper.getCollection(client, COLLECTION_NAME);

        List<Document> list = RandomInput.getDocumentsRandom(PROJECT, SUBJECT, SOURCE, SOURCE_TYPE,
                SENSOR_TYPE, COUNT, TIME_WINDOW, SAMPLES, false);

        collection.insertMany(list);

        Map<String, Document> map = RandomInput.getRandomApplicationStatus(PROJECT,
                SUBJECT, SOURCE);

        Utility.insertMixedDocs(client, map);

        Application expected = Utility.convertDocToApplication(map);
        Application actual = apiClient.requestJson(SOURCE_PATH, Application.class, Status.OK);

        assertEquals(expected.getServerStatus(), actual.getServerStatus());
        assertEquals(expected.getIpAddress(), actual.getIpAddress());

        dropAndClose(client);
    }

    @After
    public void dropAndClose() throws URISyntaxException {
        dropAndClose(Utility.getMongoClient());
    }

    /**
     * Drops all used collections to bring the database back to the initial state, and close the
     * database connection.
     **/
    public void dropAndClose(MongoClient client) {
        Utility.dropCollection(client, COLLECTION_NAME);
        Utility.dropCollection(client, STATUS_COLLECTION);
        Utility.dropCollection(client, UPTIME_COLLECTION);
        Utility.dropCollection(client, RECORD_COLLECTION);

        client.close();
    }

}
