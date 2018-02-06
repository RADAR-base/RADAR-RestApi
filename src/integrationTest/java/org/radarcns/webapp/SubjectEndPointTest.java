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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.radarcns.webapp.resource.BasePath.SUBJECTS;

import com.fasterxml.jackson.databind.ObjectReader;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.core.Response.Status;
import okhttp3.Response;
import org.bson.Document;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.radarcns.domain.restapi.Subject;
import org.radarcns.integration.util.ApiClient;
import org.radarcns.integration.util.RestApiDetails;
import org.radarcns.integration.util.Utility;
import org.radarcns.mongo.util.MongoHelper;
import org.radarcns.util.RadarConverter;
import org.radarcns.webapp.resource.BasePath;

public class SubjectEndPointTest {

    private static final String PROJECT_NAME = "radar";
    private static final String SUBJECT_ID = "sub-1";
    private static final String SOURCE_ID = "03d28e5c-e005-46d4-a9b3-279c27fbbc83";
    private static final String MONITOR_STATISTICS_TOPIC = "source_statistics_empatica_e4";
//    private static final String SOURCE_TYPE = "EMPATICA";
//    private static final String SENSOR_TYPE = "HEART_RATE";
//    private static final TimeWindow TIME_WINDOW = TimeWindow.TEN_SECOND;
//    private static final int SAMPLES = 10;

    @Rule
    public final ApiClient apiClient = new ApiClient(
            RestApiDetails.getRestApiClientDetails().getApplicationConfig().getUrlString());


    @Test
    public void getSubjectsByProjectName200()
            throws IOException, ReflectiveOperationException, URISyntaxException {
        MongoClient mongoClient = Utility.getMongoClient();
        int WINDOWS = 2;
        long start = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(10);
        long end = start + TimeUnit.SECONDS.toMillis(60 / (WINDOWS + 1));
        long later = end + TimeUnit.SECONDS.toMillis(60 / (WINDOWS + 1));
        Document doc = getDocumentsForStatistics(start, end);
        Document second = getDocumentsForStatistics(start, later);
        MongoCollection collection = MongoHelper
                .getCollection(mongoClient, MONITOR_STATISTICS_TOPIC);
        collection.insertMany(Arrays.asList(doc, second));
        Response actual = apiClient
                .request(BasePath.PROJECTS + "/" + PROJECT_NAME + "/" + SUBJECTS,
                        APPLICATION_JSON, Status.OK);
        assertTrue(actual.isSuccessful());

        ObjectReader reader = RadarConverter.readerForCollection(List.class, Subject.class);
        List<Subject> subjects = reader.readValue(actual.body().byteStream());

        assertNotNull(subjects);
        assertTrue(subjects.size() > 0);
        assertEquals(PROJECT_NAME, subjects.get(0).getProject());

    }


    private static Document getDocumentsForStatistics(long start, long end) {
        return new Document(MongoHelper.ID, PROJECT_NAME + "_" + SUBJECT_ID + "-" + SOURCE_ID +
                "-" + start + "-" + end)
                .append(MongoHelper.USER_ID, SUBJECT_ID)
                .append(MongoHelper.SOURCE_ID, SOURCE_ID)
                .append(MongoHelper.PROJECT_ID, PROJECT_NAME)
                .append(MongoHelper.START, new Date(start))
                .append(MongoHelper.END, new Date(end));
    }

    @Test
    public void getSubjectsBySubjectIdAndProjectName200()
            throws IOException, ReflectiveOperationException, URISyntaxException {
        MongoClient mongoClient = Utility.getMongoClient();
        int WINDOWS = 2;
        long start = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(10);
        long end = start + TimeUnit.SECONDS.toMillis(60 / (WINDOWS + 1));
        long later = end + TimeUnit.SECONDS.toMillis(60 / (WINDOWS + 1));
        Document doc = getDocumentsForStatistics(start, end);
        Document second = getDocumentsForStatistics(start, later);
        MongoCollection collection = MongoHelper
                .getCollection(mongoClient, MONITOR_STATISTICS_TOPIC);
        collection.insertMany(Arrays.asList(doc, second));
        Response actual = apiClient
                .request(BasePath.PROJECTS + "/" + PROJECT_NAME + "/" + SUBJECTS + "/"
                                + SUBJECT_ID, APPLICATION_JSON, Status.OK);
        assertTrue(actual.isSuccessful());

        ObjectReader reader = RadarConverter.readerFor(Subject.class);
        Subject subject = reader.readValue(actual.body().byteStream());

        assertNotNull(subject);
        assertEquals(SUBJECT_ID, subject.getSubjectId());
        assertEquals(PROJECT_NAME, subject.getProject());
        assertTrue(subject.getSources().size() > 0);
        assertEquals(PROJECT_NAME, subject.getProject());

    }

    @Test
    public void getSubjectTest404() throws IOException, ReflectiveOperationException {
        Response actual = apiClient
                .request(BasePath.PROJECTS + "/" + PROJECT_NAME + "/" + SUBJECTS + "/"
                                + "OTHER", APPLICATION_JSON, Status.NOT_FOUND);
        assertFalse(actual.isSuccessful());
        assertEquals(actual.code(), Status.NOT_FOUND.getStatusCode());
    }

    @After
    public void dropAndClose() {
        Utility.dropCollection(Utility.getMongoClient(), MONITOR_STATISTICS_TOPIC);
    }


}
