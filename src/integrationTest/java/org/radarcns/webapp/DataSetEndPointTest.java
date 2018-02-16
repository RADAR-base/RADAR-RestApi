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

import static java.time.temporal.ChronoUnit.SECONDS;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.radarcns.domain.restapi.header.DescriptiveStatistic.AVERAGE;
import static org.radarcns.domain.restapi.header.DescriptiveStatistic.COUNT;
import static org.radarcns.domain.restapi.header.DescriptiveStatistic.QUARTILES;
import static org.radarcns.integration.util.RandomInput.DATASET;
import static org.radarcns.integration.util.RandomInput.DOCUMENTS;
import static org.radarcns.webapp.resource.BasePath.DATA;
import static org.radarcns.webapp.resource.BasePath.LATEST;

import com.fasterxml.jackson.databind.ObjectReader;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response.Status;
import okhttp3.Response;
import org.bson.Document;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.radarcns.domain.restapi.TimeWindow;
import org.radarcns.domain.restapi.dataset.Dataset;
import org.radarcns.domain.restapi.format.Acceleration;
import org.radarcns.domain.restapi.format.Quartiles;
import org.radarcns.integration.util.ApiClient;
import org.radarcns.integration.util.RandomInput;
import org.radarcns.integration.util.RestApiDetails;
import org.radarcns.integration.util.Utility;
import org.radarcns.mongo.util.MongoHelper;
import org.radarcns.util.RadarConverter;
import org.radarcns.webapp.resource.Parameter;

public class DataSetEndPointTest {

    private static final String PROJECT = "radar";
    private static final String SUBJECT = "sub-1";
    private static final String SOURCE = "03d28e5c-e005-46d4-a9b3-279c27fbbc83";
    private static final String SOURCE_TYPE = "empatica_e4_v1";
    private static final String SOURCE_DATA_NAME = "EMPATICA_E4_v1_BATTERY";
    private static final TimeWindow TIME_WINDOW = TimeWindow.TEN_SECOND;
    private static final int SAMPLES = 10;
    private static final String REQUEST_PATH = PROJECT + '/' + SUBJECT + '/' + SOURCE + '/'
            + SOURCE_DATA_NAME + '/' + COUNT;
    private static final String COLLECTION_NAME = "android_empatica_e4_battery_level_output";
    private static final String ACCELERATION_COLLECTION = "android_empatica_e4_acceleration_output";

    @Rule
    public final ApiClient apiClient = new ApiClient(
            RestApiDetails.getRestApiClientDetails().getApplicationConfig().getUrlString()
                    + DATA + '/');

    @Test
    public void getLatestRecord()
            throws IOException, ReflectiveOperationException, URISyntaxException {
        MongoClient client = Utility.getMongoClient();

        MongoCollection<Document> collection = MongoHelper.getCollection(client, COLLECTION_NAME);

        Map<String, Object> docs = RandomInput
                .getDatasetAndDocumentsRandom(PROJECT, SUBJECT, SOURCE,
                        SOURCE_TYPE, SOURCE_DATA_NAME, COUNT, TIME_WINDOW, SAMPLES, false);

        collection.insertMany((List<Document>) docs.get(DOCUMENTS));

        Dataset expected = (Dataset) docs.get(DATASET);

        Response actual = apiClient
                .request(REQUEST_PATH + '/' + LATEST
                                + '?' + Parameter.TIME_WINDOW + '=' + TIME_WINDOW, APPLICATION_JSON,
                        Status.OK);
        assertTrue(actual.isSuccessful());
        ObjectReader reader = RadarConverter.readerFor(Dataset.class);
        Dataset dataset = reader.readValue(actual.body().byteStream());
        assertNotNull(dataset);
        assertEquals(expected.getHeader().projectId, dataset.getHeader().getProjectId());
        assertEquals(expected.getHeader().subjectId, dataset.getHeader().getSubjectId());
        assertEquals(expected.getHeader().sourceId, dataset.getHeader().getSourceId());
        assertEquals(1, dataset.getDataset().size());
        assertEquals(expected.getDataset().get(0), dataset.dataset.get(0));

        dropAndClose(client);
    }

    @Test
    public void getAllRecords()
            throws IOException, ReflectiveOperationException, URISyntaxException {
        MongoClient client = Utility.getMongoClient();

        MongoCollection<Document> collection = MongoHelper.getCollection(client, COLLECTION_NAME);

        Map<String, Object> docs = RandomInput
                .getDatasetAndDocumentsRandom(PROJECT, SUBJECT, SOURCE,
                        SOURCE_TYPE, SOURCE_DATA_NAME, COUNT, TIME_WINDOW, SAMPLES, false);

        collection.insertMany((List<Document>) docs.get(DOCUMENTS));

        Dataset expected = (Dataset) docs.get(DATASET);

        Response actual = apiClient
                .request(REQUEST_PATH + '?' + Parameter.TIME_WINDOW + '=' + TIME_WINDOW,
                        APPLICATION_JSON,
                        Status.OK);
        assertTrue(actual.isSuccessful());
        ObjectReader reader = RadarConverter.readerFor(Dataset.class);
        Dataset dataset = reader.readValue(actual.body().byteStream());
        assertNotNull(dataset);
        assertEquals(expected.getHeader().projectId, dataset.getHeader().getProjectId());
        assertEquals(expected.getHeader().subjectId, dataset.getHeader().getSubjectId());
        assertEquals(expected.getHeader().sourceId, dataset.getHeader().getSourceId());
        assertEquals(expected.getDataset().size(), dataset.dataset.size());
        assertEquals(expected.getDataset().get(0), dataset.dataset.get(0));

        dropAndClose(client);
    }

    @Test
    public void getAllRecordsForAcceleration()
            throws IOException, ReflectiveOperationException, URISyntaxException {
        MongoClient client = Utility.getMongoClient();

        MongoCollection<Document> collection = MongoHelper
                .getCollection(client, ACCELERATION_COLLECTION);
        String sourceDataName = "EMPATICA_E4_v1_ACCELEROMETER";
        Map<String, Object> docs = RandomInput
                .getDatasetAndDocumentsRandom(PROJECT, SUBJECT, SOURCE,
                        SOURCE_TYPE, sourceDataName, AVERAGE, TIME_WINDOW, SAMPLES, false);

        collection.insertMany((List<Document>) docs.get(DOCUMENTS));

        Dataset expected = (Dataset) docs.get(DATASET);
        String requestPath = PROJECT + '/' + SUBJECT + '/' + SOURCE + '/'
                + sourceDataName + '/' + AVERAGE + '?' + Parameter.TIME_WINDOW + '=' + TIME_WINDOW;

        Response actual = apiClient.request(requestPath, APPLICATION_JSON, Status.OK);
        assertTrue(actual.isSuccessful());
        ObjectReader reader = RadarConverter.readerFor(Dataset.class);
        Dataset dataset = reader.readValue(actual.body().byteStream());
        assertNotNull(dataset);
        assertEquals(expected.getHeader().projectId, dataset.getHeader().getProjectId());
        assertEquals(expected.getHeader().subjectId, dataset.getHeader().getSubjectId());
        assertEquals(expected.getHeader().sourceId, dataset.getHeader().getSourceId());
        assertEquals(expected.getDataset().size(), dataset.dataset.size());
        Map sample = (HashMap) dataset.getDataset().get(0).getSample();
        assertEquals(expected.getDataset().get(0).getSample(),
                new Acceleration(sample.get("x"), sample.get("y"), sample.get("z")));

        dropAndClose(client);
    }

    @Test
    public void getAllRecordsWithQuartiles()
            throws IOException, ReflectiveOperationException, URISyntaxException {
        MongoClient client = Utility.getMongoClient();

        MongoCollection<Document> collection = MongoHelper
                .getCollection(client, COLLECTION_NAME);
        Map<String, Object> docs = RandomInput
                .getDatasetAndDocumentsRandom(PROJECT, SUBJECT, SOURCE,
                        SOURCE_TYPE, SOURCE_DATA_NAME, QUARTILES, TIME_WINDOW, SAMPLES, false);

        collection.insertMany((List<Document>) docs.get(DOCUMENTS));

        Dataset expected = (Dataset) docs.get(DATASET);
        String requestPath = PROJECT + '/' + SUBJECT + '/' + SOURCE + '/'
                + SOURCE_DATA_NAME + '/' + QUARTILES + '?' + Parameter.TIME_WINDOW + '='
                + TIME_WINDOW;

        Response actual = apiClient.request(requestPath, APPLICATION_JSON, Status.OK);
        assertTrue(actual.isSuccessful());
        ObjectReader reader = RadarConverter.readerFor(Dataset.class);
        Dataset dataset = reader.readValue(actual.body().byteStream());
        assertNotNull(dataset);
        assertEquals(expected.getHeader().projectId, dataset.getHeader().getProjectId());
        assertEquals(expected.getHeader().subjectId, dataset.getHeader().getSubjectId());
        assertEquals(expected.getHeader().sourceId, dataset.getHeader().getSourceId());
        assertEquals(expected.getDataset().size(), dataset.dataset.size());
        Map sample = (HashMap) dataset.getDataset().get(0).getSample();
        assertEquals(expected.getDataset().get(0).getSample(),
                new Quartiles((Double) sample.get("first"), (Double) sample.get("second"),
                        (Double) sample.get("third")));
        dropAndClose(client);
    }

    @Test
    public void getAllRecordsWithQuartilesInTimeRange()
            throws IOException, ReflectiveOperationException, URISyntaxException {
        MongoClient client = Utility.getMongoClient();
        Instant now = Instant.now();
        Date start = Date.from(now.plus(RadarConverter.getSecond(TIME_WINDOW), SECONDS));
        Date end = Date.from(now.plus(7 * RadarConverter.getSecond(TIME_WINDOW), SECONDS));
        MongoCollection<Document> collection = MongoHelper
                .getCollection(client, COLLECTION_NAME);
        Map<String, Object> docs = RandomInput
                .getDatasetAndDocumentsRandom(PROJECT, SUBJECT, SOURCE,
                        SOURCE_TYPE, SOURCE_DATA_NAME, QUARTILES, TIME_WINDOW, SAMPLES, false);

        collection.insertMany((List<Document>) docs.get(DOCUMENTS));

        Dataset expected = (Dataset) docs.get(DATASET);
        String requestPath = PROJECT + '/' + SUBJECT + '/' + SOURCE + '/'
                + SOURCE_DATA_NAME + '/' + QUARTILES + '/' + '?'
                + Parameter.TIME_WINDOW + '=' + TIME_WINDOW + '&'
                + Parameter.START + '=' + RadarConverter.getISO8601(start) + '&'
                + Parameter.END + '=' + RadarConverter.getISO8601(end);

        Response actual = apiClient.request(requestPath, APPLICATION_JSON, Status.OK);
        assertTrue(actual.isSuccessful());
        ObjectReader reader = RadarConverter.readerFor(Dataset.class);
        Dataset dataset = reader.readValue(actual.body().byteStream());
        assertNotNull(dataset);
        assertEquals(expected.getHeader().projectId, dataset.getHeader().getProjectId());
        assertEquals(expected.getHeader().subjectId, dataset.getHeader().getSubjectId());
        assertEquals(expected.getHeader().sourceId, dataset.getHeader().getSourceId());
        assertTrue(dataset.getDataset().size() < 7 && dataset.getDataset().size() >= 5);
        assertEquals(RadarConverter.getISO8601(start),
                dataset.getHeader().getEffectiveTimeFrame().getStartDateTime());
        assertEquals(RadarConverter.getISO8601(end),
                dataset.getHeader().getEffectiveTimeFrame().getEndDateTime());

        dropAndClose(client);
    }

    @Test
    public void getAllDataTestEmpty() throws IOException {
        Dataset dataset = apiClient.requestJson(REQUEST_PATH, Dataset.class, Status.OK);
        assertThat(dataset.getDataset(), is(empty()));
    }

    @After
    public void dropAndClose() {
        dropAndClose(Utility.getMongoClient());
    }

    /**
     * Drops all used collections to bring the database back to the initial state, and close the
     * database connection.
     **/
    private void dropAndClose(MongoClient client) {
        Utility.dropCollection(client, COLLECTION_NAME);
        Utility.dropCollection(client, ACCELERATION_COLLECTION);
        client.close();
    }

}
