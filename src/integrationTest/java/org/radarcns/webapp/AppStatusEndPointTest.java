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
import static org.radarcns.webapp.resource.BasePath.STATUS;

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
import org.radarcns.dao.AndroidAppDataAccessObject;
import org.radarcns.domain.restapi.Application;
import org.radarcns.domain.restapi.TimeWindow;
import org.radarcns.integration.util.ApiClient;
import org.radarcns.integration.util.RandomInput;
import org.radarcns.integration.util.RestApiDetails;
import org.radarcns.integration.util.Utility;
import org.radarcns.mongo.util.MongoHelper;
import org.radarcns.monitor.application.ServerStatus;
import org.radarcns.webapp.resource.BasePath;

public class AppStatusEndPointTest {

    private static final String PROJECT = "radar";
    private static final String SUBJECT = "sub-1";
    private static final String SOURCE = "03d28e5c-e005-46d4-a9b3-279c27fbbc83";
    private static final String SOURCE_TYPE = "EMPATICA";
    private static final String SENSOR_TYPE = "HEART_RATE";
    private static final TimeWindow TIME_WINDOW = TimeWindow.TEN_SECOND;
    private static final int SAMPLES = 10;
    private static final String SOURCE_PATH = PROJECT + '/' + SUBJECT + '/' + SOURCE;
    private static final String COLLECTION_NAME = "android_empatica_e4_heartrate_10sec";

    @Rule
    public final ApiClient apiClient = new ApiClient(
            RestApiDetails.getRestApiClientDetails().getApplicationConfig().getUrlString()
                    + BasePath.ANDROID + '/' + STATUS + '/');

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

        Map<String, Document> map = RandomInput.getRandomApplicationStatus(
                SUBJECT, SOURCE);

        Utility.insertMixedDocs(client, map);

        Application expected = Utility.convertDocToApplication(map);
        Application actual = apiClient.requestJson(SOURCE_PATH, Application.class, Status.OK);

        assertEquals(expected, actual);

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
        Utility.dropCollection(client, MongoHelper.DEVICE_CATALOG);
        Utility.dropCollection(client, COLLECTION_NAME);
        Utility.dropCollection(client, AndroidAppDataAccessObject.getInstance().getCollections());
        client.close();
    }

}
