package org.radarcns.webapp;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.radarcns.domain.restapi.TimeWindow.ONE_MIN;
import static org.radarcns.domain.restapi.TimeWindow.TEN_SECOND;
import static org.radarcns.domain.restapi.header.DescriptiveStatistic.QUARTILES;
import static org.radarcns.integration.util.RandomInput.DOCUMENTS;
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
import org.radarcns.domain.restapi.AggregatedData;
import org.radarcns.integration.util.ApiClient;
import org.radarcns.integration.util.RandomInput;
import org.radarcns.integration.util.RestApiDetails;
import org.radarcns.integration.util.Utility;
import org.radarcns.mongo.util.MongoHelper;
import org.radarcns.util.RadarConverter;
import org.radarcns.webapp.param.DataAggregateParam;
import org.radarcns.webapp.resource.Parameter;

public class AggregatedDataPointsEndPointTest {

    private static final String PROJECT = "radar";
    private static final String SUBJECT = "sub-1";
    private static final String SOURCE = "03d28e5c-e005-46d4-a9b3-279c27fbbc83";
    private static final String SOURCE_TYPE = "empatica_e4_v1";
    private static final String SOURCE_DATA_NAME = "EMPATICA_E4_v1_BATTERY";
    private static final int SAMPLES = 10;
    private static final String COLLECTION_NAME = "android_empatica_e4_battery_level_output";
    private static final String ACCELERATION_COLLECTION = "android_empatica_e4_acceleration_output";

    @Rule
    public final ApiClient apiClient = new ApiClient(
            RestApiDetails.getRestApiClientDetails().getApplicationConfig().getUrlString()
                    + AGGREGATED_DATA_POINTS + '/');

    @Test
    public void getAllRecordsWithQuartilesInTimeRange() throws IOException {
        MongoClient client = Utility.getMongoClient();
        Instant now = Instant.now();

        Instant start = now.plus(RadarConverter.getDuration(TEN_SECOND));
        Instant end = start.plus(RadarConverter.getDuration(ONE_MIN));
        MongoCollection<Document> collection = MongoHelper
                .getCollection(client, COLLECTION_NAME);
        Map<String, Object> docs = RandomInput
                .getDatasetAndDocumentsRandom(PROJECT, SUBJECT, SOURCE,
                        SOURCE_TYPE, SOURCE_DATA_NAME, QUARTILES, TEN_SECOND, SAMPLES, false);

        String sourceDataName = "EMPATICA_E4_v1_ACCELEROMETER";
        Map<String, Object> accelerationDocs = RandomInput
                .getDatasetAndDocumentsRandom(PROJECT, SUBJECT, SOURCE,
                        SOURCE_TYPE, sourceDataName, QUARTILES, TEN_SECOND, SAMPLES, false);
        MongoCollection<Document> accCollection = MongoHelper
                .getCollection(client, ACCELERATION_COLLECTION);

        collection.insertMany((List<Document>) docs.get(DOCUMENTS));
        accCollection.insertMany((List<Document>) accelerationDocs.get(DOCUMENTS));

        String requestPath = PROJECT + '/' + SUBJECT + '?'
                + Parameter.TIME_WINDOW + '=' + TEN_SECOND + '&'
                + Parameter.START + '=' + start + '&'
                + Parameter.END + '=' + end;

        DataAggregateParam aggregateParam = new DataAggregateParam();
        AggregateDataSource aggregate = new AggregateDataSource();
        aggregate.setSourceId(SOURCE);
        aggregate.setSourceDataName(Arrays.asList(SOURCE_DATA_NAME, sourceDataName));
        aggregateParam.setSources(Arrays.asList(aggregate));
        ObjectWriter writer = RadarConverter.writerFor(DataAggregateParam.class);
        Response actual = apiClient.postRequest(requestPath, APPLICATION_JSON, writer
                .writeValueAsBytes(aggregateParam), Status.OK);
        ObjectReader reader = RadarConverter.readerFor(AggregatedData.class);
        AggregatedData dateset = reader.readValue(actual.body().byteStream());

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
        Utility.dropCollection(client, COLLECTION_NAME);
        Utility.dropCollection(client, ACCELERATION_COLLECTION);
        client.close();
    }

}
