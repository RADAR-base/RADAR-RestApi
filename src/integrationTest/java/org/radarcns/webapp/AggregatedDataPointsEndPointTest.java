package org.radarcns.webapp;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.radarcns.domain.restapi.TimeWindow.ONE_HOUR;
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
import static org.radarcns.webapp.resource.BasePath.AGGREGATED_DATA_POINTS;

import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response.Status;
import okhttp3.Response;
import org.bson.Document;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.radarcns.domain.restapi.AggregateDataSource;
import org.radarcns.domain.restapi.AggregatedDataPoints;
import org.radarcns.domain.restapi.TimeWindow;
import org.radarcns.domain.restapi.format.AggregatedDataItem;
import org.radarcns.integration.util.ApiClient;
import org.radarcns.integration.util.RandomInput;
import org.radarcns.integration.util.RestApiDetails;
import org.radarcns.integration.util.Utility;
import org.radarcns.mongo.util.MongoHelper;
import org.radarcns.util.RadarConverter;
import org.radarcns.webapp.param.DataAggregateParam;
import org.radarcns.webapp.resource.Parameter;

public class AggregatedDataPointsEndPointTest {


    @Rule
    public final ApiClient apiClient = new ApiClient(
            RestApiDetails.getRestApiClientDetails().getApplicationConfig().getUrlString()
                    + AGGREGATED_DATA_POINTS + '/');

    @Test
    public void getAllRecordsWithQuartilesInTimeRange() throws IOException {
        MongoClient client = Utility.getMongoClient();
        Instant now = Instant.now();
        TimeWindow window = TEN_MIN;

        Instant start = now.plus(RadarConverter.getDuration(TEN_MIN));
        Instant end = start.plus(RadarConverter.getDuration(ONE_HOUR));
        MongoCollection<Document> collection = MongoHelper
                .getCollection(client, BATTERY_LEVEL_COLLECTION_FOR_TEN_MINUTES);
        Map<String, Object> docs = RandomInput
                .getDatasetAndDocumentsRandom(PROJECT, SUBJECT, SOURCE,
                        SOURCE_TYPE, BATTERY_LEVEL_SOURCE_DATA_NAME, QUARTILES, window, SAMPLES,
                        false, now);

        MongoCollection<Document> accelerationCollection = MongoHelper
                .getCollection(client, ACCELERATION_COLLECTION_FOR_TEN_MINITES);

        Map<String, Object> accDocs = RandomInput
                .getDatasetAndDocumentsRandom(PROJECT, SUBJECT, SOURCE,
                        SOURCE_TYPE, ACCELEROMETER_SOURCE_DATA_NAME, AVERAGE, TEN_MIN,
                        5, false, Instant.now());

        collection.insertMany((List<Document>) docs.get(DOCUMENTS));

        accelerationCollection.insertMany((List<Document>) accDocs.get(DOCUMENTS));

        DataAggregateParam aggregateParam = new DataAggregateParam(Arrays.asList(
                new AggregateDataSource(SOURCE,
                        Arrays.asList(BATTERY_LEVEL_SOURCE_DATA_NAME,
                                ACCELEROMETER_SOURCE_DATA_NAME))));
        ObjectWriter writer = RadarConverter.writerFor(DataAggregateParam.class);
        String requestPath = PROJECT + '/' + SUBJECT + '?'
                + Parameter.TIME_WINDOW + '=' + window + '&'
                + Parameter.START + '=' + start + '&'
                + Parameter.END + '=' + end;
        Response actual = apiClient.postRequest(requestPath, APPLICATION_JSON, writer
                .writeValueAsBytes(aggregateParam), Status.OK);
        ObjectReader reader = RadarConverter.readerFor(AggregatedDataPoints.class);
        AggregatedDataPoints dateset = reader.readValue(actual.body().byteStream());
        assertNotNull(dateset);
        assertTrue(dateset.getAggregatedDataItemList().size() <= 6);
        List<AggregatedDataItem> dataItems = dateset.getAggregatedDataItemList();
        assertEquals(Integer.valueOf(2), dataItems.get(0).getCount());
        assertEquals(Integer.valueOf(1), dataItems.get(4).getCount());

        dropAndClose(client);
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
        Utility.dropCollection(client, BATTERY_LEVEL_COLLECTION_FOR_TEN_MINUTES);
        Utility.dropCollection(client, ACCELERATION_COLLECTION_FOR_TEN_MINITES);
        client.close();
    }

}
