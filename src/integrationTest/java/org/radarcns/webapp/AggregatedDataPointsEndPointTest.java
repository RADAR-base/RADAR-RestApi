package org.radarcns.webapp;

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.radarcns.domain.restapi.TimeWindow.TEN_MIN;
import static org.radarcns.domain.restapi.header.DescriptiveStatistic.AVERAGE;
import static org.radarcns.domain.restapi.header.DescriptiveStatistic.QUARTILES;
import static org.radarcns.integration.util.RandomInput.DOCUMENTS;
import static org.radarcns.webapp.SampleDataHandler.ACCELERATION_COLLECTION_FOR_TEN_MINITES;
import static org.radarcns.webapp.SampleDataHandler.ACCELEROMETER_SOURCE_DATA_NAME;
import static org.radarcns.webapp.SampleDataHandler.BATTERY_LEVEL_COLLECTION_FOR_TEN_MINUTES;
import static org.radarcns.webapp.SampleDataHandler.BATTERY_LEVEL_SOURCE_DATA_NAME;
import static org.radarcns.webapp.SampleDataHandler.PROJECT;
import static org.radarcns.webapp.SampleDataHandler.SAMPLES;
import static org.radarcns.webapp.SampleDataHandler.SOURCE;
import static org.radarcns.webapp.SampleDataHandler.SOURCE_TYPE;
import static org.radarcns.webapp.SampleDataHandler.SUBJECT;
import static org.radarcns.webapp.resource.BasePath.AGGREGATE;
import static org.radarcns.webapp.resource.BasePath.DISTINCT;

import com.mongodb.client.MongoCollection;
import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response.Status;
import org.bson.Document;
import org.junit.Rule;
import org.junit.Test;
import org.radarcns.domain.restapi.AggregateDataSource;
import org.radarcns.domain.restapi.TimeWindow;
import org.radarcns.domain.restapi.dataset.AggregatedDataPoints;
import org.radarcns.domain.restapi.dataset.DataItem;
import org.radarcns.integration.MongoRule;
import org.radarcns.integration.util.ApiClient;
import org.radarcns.integration.util.RandomInput;
import org.radarcns.integration.util.RestApiDetails;
import org.radarcns.util.RadarConverter;
import org.radarcns.webapp.param.DataAggregateParam;
import org.radarcns.webapp.resource.Parameter;

public class AggregatedDataPointsEndPointTest {
    @Rule
    public final ApiClient apiClient = new ApiClient(
            RestApiDetails.getRestApiClientDetails().getApplicationConfig().getUrlString()
                    + AGGREGATE + '/');

    @Rule
    public final MongoRule mongoRule = new MongoRule();

    @Test
    public void getAllRecordsWithAggregatedDataPointsInTimeRange() throws IOException {
        Instant now = Instant.now();
        TimeWindow window = TEN_MIN;

        // injects 10 records for last 100 min
        MongoCollection<Document> collection = mongoRule.getCollection(
                BATTERY_LEVEL_COLLECTION_FOR_TEN_MINUTES);

        Map<String, Object> docs = RandomInput.getDatasetAndDocumentsRandom(
                PROJECT, SUBJECT, SOURCE, SOURCE_TYPE, BATTERY_LEVEL_SOURCE_DATA_NAME, QUARTILES,
                window, SAMPLES, false, now);
        collection.insertMany((List<Document>) docs.get(DOCUMENTS));

        MongoCollection<Document> accelerationCollection = mongoRule.getCollection(
                ACCELERATION_COLLECTION_FOR_TEN_MINITES);

        // injects 5 records for acceleration, for last 50 minutes
        Map<String, Object> accDocs = RandomInput.getDatasetAndDocumentsRandom(
                PROJECT, SUBJECT, SOURCE, SOURCE_TYPE, ACCELEROMETER_SOURCE_DATA_NAME, AVERAGE,
                TEN_MIN,5, false, now);

        accelerationCollection.insertMany((List<Document>) accDocs.get(DOCUMENTS));

        AggregateDataSource aggregateDataSource = new AggregateDataSource();
        aggregateDataSource.setSourceId(SOURCE);
        aggregateDataSource.setSourceDataNames(Arrays.asList(BATTERY_LEVEL_SOURCE_DATA_NAME,
                ACCELEROMETER_SOURCE_DATA_NAME));
        DataAggregateParam aggregateParam = new DataAggregateParam(
                Collections.singletonList(aggregateDataSource));

        // reduces 70 min
        Instant start = now
                .minus(RadarConverter.getSecond(TEN_MIN) * 7, SECONDS);
        Instant end = now.minus(RadarConverter.getDuration(TEN_MIN));

        String requestPath = PROJECT + '/' + SUBJECT + '/' + DISTINCT + '?'
                + Parameter.TIME_WINDOW + '=' + window + '&'
                + Parameter.START + '=' + start + '&'
                + Parameter.END + '=' + end;
        AggregatedDataPoints dataset = apiClient.postJson(requestPath, aggregateParam,
                AggregatedDataPoints.class, Status.OK);
        assertNotNull(dataset);
        assertTrue(dataset.getDataset().size() <= 6);
        List<DataItem> dataItems = dataset.getDataset();
        assertEquals(1, dataItems.get(0).getValue());
        assertEquals(2, dataItems.get(4).getValue());
    }
}
