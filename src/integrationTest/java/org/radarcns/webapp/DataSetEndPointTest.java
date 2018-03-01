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

import com.mongodb.client.MongoCollection;
import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response.Status;
import org.bson.Document;
import org.junit.Rule;
import org.junit.Test;
import org.radarcns.domain.restapi.TimeWindow;
import org.radarcns.domain.restapi.dataset.Dataset;
import org.radarcns.domain.restapi.format.Acceleration;
import org.radarcns.domain.restapi.format.Quartiles;
import org.radarcns.domain.restapi.header.Header;
import org.radarcns.integration.MongoRule;
import org.radarcns.integration.util.ApiClient;
import org.radarcns.integration.util.RandomInput;
import org.radarcns.integration.util.RestApiDetails;
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
    private static final String COLLECTION_FOR_TEN_MINUTES =
            "android_empatica_e4_battery_level_output_10min";

    @Rule
    public final ApiClient apiClient = new ApiClient(
            RestApiDetails.getRestApiClientDetails().getApplicationConfig().getUrlString()
                    + DATA + '/');

    @Rule
    public final MongoRule mongoRule = new MongoRule();

    @Test
    public void getRecords() throws IOException {
        MongoCollection<Document> collection = mongoRule.getCollection(COLLECTION_NAME);

        Map<String, Object> docs = RandomInput
                .getDatasetAndDocumentsRandom(PROJECT, SUBJECT, SOURCE,
                        SOURCE_TYPE, SOURCE_DATA_NAME, COUNT, TIME_WINDOW, SAMPLES, false);

        collection.insertMany((List<Document>) docs.get(DOCUMENTS));

        Dataset expected = (Dataset) docs.get(DATASET);

        Dataset actual = assertRequestsMatch(
                REQUEST_PATH + '?' + Parameter.TIME_WINDOW + '=' + TIME_WINDOW, expected);

        assertEquals(expected.getDataset(), actual.getDataset());
    }

    @Test
    public void getAllRecordsForAcceleration() throws IOException {
        MongoCollection<Document> collection = mongoRule.getCollection(ACCELERATION_COLLECTION);
        String sourceDataName = "EMPATICA_E4_v1_ACCELEROMETER";
        Map<String, Object> docs = RandomInput
                .getDatasetAndDocumentsRandom(PROJECT, SUBJECT, SOURCE,
                        SOURCE_TYPE, sourceDataName, AVERAGE, TIME_WINDOW, SAMPLES, false);

        collection.insertMany((List<Document>) docs.get(DOCUMENTS));

        Dataset expected = (Dataset) docs.get(DATASET);
        String requestPath = PROJECT + '/' + SUBJECT + '/' + SOURCE + '/'
                + sourceDataName + '/' + AVERAGE + '?' + Parameter.TIME_WINDOW + '=' + TIME_WINDOW;

        Dataset actual = assertRequestsMatch(requestPath, expected);
        assertEquals(expected.getDataset().size(), actual.getDataset().size());
        Map sample = (HashMap) actual.getDataset().get(0).getSample();
        assertEquals(expected.getDataset().get(0).getSample(),
                new Acceleration(sample.get("x"), sample.get("y"), sample.get("z")));
    }

    @Test
    public void getAllRecordsWithQuartiles() throws IOException {
        MongoCollection<Document> collection = mongoRule.getCollection(COLLECTION_NAME);
        Map<String, Object> docs = RandomInput
                .getDatasetAndDocumentsRandom(PROJECT, SUBJECT, SOURCE,
                        SOURCE_TYPE, SOURCE_DATA_NAME, QUARTILES, TIME_WINDOW, SAMPLES, false);

        collection.insertMany((List<Document>) docs.get(DOCUMENTS));

        Dataset expected = (Dataset) docs.get(DATASET);
        String requestPath = PROJECT + '/' + SUBJECT + '/' + SOURCE + '/'
                + SOURCE_DATA_NAME + '/' + QUARTILES + '?' + Parameter.TIME_WINDOW + '='
                + TIME_WINDOW;

        Dataset actual = assertRequestsMatch(requestPath, expected);
        assertEquals(expected.getDataset().size(), actual.getDataset().size());
        Map sample = (HashMap) actual.getDataset().get(0).getSample();
        assertEquals(expected.getDataset().get(0).getSample(),
                new Quartiles((Double) sample.get("first"), (Double) sample.get("second"),
                        (Double) sample.get("third")));
    }

    @Test
    public void getAllRecordsWithQuartilesInTimeRange() throws IOException {
        Instant now = Instant.now();
        Instant start = now.plus(RadarConverter.getSecond(TIME_WINDOW), SECONDS);
        Instant end = now.plus(7 * RadarConverter.getSecond(TIME_WINDOW), SECONDS);
        MongoCollection<Document> collection = mongoRule.getCollection(COLLECTION_NAME);
        Map<String, Object> docs = RandomInput
                .getDatasetAndDocumentsRandom(PROJECT, SUBJECT, SOURCE,
                        SOURCE_TYPE, SOURCE_DATA_NAME, QUARTILES, TIME_WINDOW, SAMPLES, false);

        collection.insertMany((List<Document>) docs.get(DOCUMENTS));

        Dataset expected = (Dataset) docs.get(DATASET);
        String requestPath = PROJECT + '/' + SUBJECT + '/' + SOURCE + '/'
                + SOURCE_DATA_NAME + '/' + QUARTILES + '/' + '?'
                + Parameter.TIME_WINDOW + '=' + TIME_WINDOW + '&'
                + Parameter.START + '=' + start + '&'
                + Parameter.END + '=' + end;

        Dataset actual = assertRequestsMatch(requestPath, expected);
        assertTrue(actual.getDataset().size() < 7 && actual.getDataset().size() >= 5);
        assertEquals(start,
                actual.getHeader().getTimeFrame().getStartDateTime());
        assertEquals(end,
                actual.getHeader().getTimeFrame().getEndDateTime());
    }

    @Test
    public void getAllRecordsWithQuartilesInTimeRangeWithTenMinutes() throws IOException {
        Instant now = Instant.now();
        TimeWindow window = TimeWindow.TEN_MIN;

        Instant start = now.plus(RadarConverter.getSecond(window), SECONDS);
        Instant end = now.plus(7 * RadarConverter.getSecond(window), SECONDS);
        MongoCollection<Document> collection = mongoRule.getCollection(COLLECTION_FOR_TEN_MINUTES);
        Map<String, Object> docs = RandomInput
                .getDatasetAndDocumentsRandom(PROJECT, SUBJECT, SOURCE,
                        SOURCE_TYPE, SOURCE_DATA_NAME, QUARTILES, window, SAMPLES, false);

        collection.insertMany((List<Document>) docs.get(DOCUMENTS));

        Dataset expected = (Dataset) docs.get(DATASET);
        String requestPath = PROJECT + '/' + SUBJECT + '/' + SOURCE + '/'
                + SOURCE_DATA_NAME + '/' + QUARTILES + '/' + '?'
                + Parameter.TIME_WINDOW + '=' + window + '&'
                + Parameter.START + '=' + start + '&'
                + Parameter.END + '=' + end;

        Dataset actual = assertRequestsMatch(requestPath, expected);
        assertTrue(actual.getDataset().size() < 7 && actual.getDataset().size() >= 5);
        assertEquals(start,
                actual.getHeader().getTimeFrame().getStartDateTime());
        assertEquals(end,
                actual.getHeader().getTimeFrame().getEndDateTime());
        assertEquals(window, actual.getHeader().getTimeWindow());
    }

    private Dataset assertRequestsMatch(String relativeUrl, Dataset expected)
            throws IOException {
        Dataset actual = apiClient.getJson(relativeUrl, Dataset.class, Status.OK);
        assertNotNull(actual);
        Header expectedHeader = expected.getHeader();
        Header actualHeader = actual.getHeader();
        assertEquals(expectedHeader.getProjectId(), actualHeader.getProjectId());
        assertEquals(expectedHeader.getSubjectId(), actualHeader.getSubjectId());
        assertEquals(expectedHeader.getSourceId(), actualHeader.getSourceId());
        return actual;
    }

    @Test
    public void getAllDataTestEmpty() throws IOException {
        Dataset dataset = apiClient.getJson(REQUEST_PATH, Dataset.class, Status.OK);
        assertThat(dataset.getDataset(), is(empty()));
    }
}
