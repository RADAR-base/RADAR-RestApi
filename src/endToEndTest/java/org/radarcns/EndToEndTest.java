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

package org.radarcns;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.radarcns.config.ExposedConfigTest.OPENAPI_JSON;
import static org.radarcns.webapp.resource.BasePath.DATA;
import static org.radarcns.webapp.resource.Parameter.SOURCE_DATA_NAME;
import static org.radarcns.webapp.resource.Parameter.STAT;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.core.Response.Status;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.avro.specific.SpecificRecord;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.radarcns.config.PipelineConfig;
import org.radarcns.config.YamlConfigLoader;
import org.radarcns.domain.restapi.TimeWindow;
import org.radarcns.domain.restapi.dataset.DataItem;
import org.radarcns.domain.restapi.dataset.Dataset;
import org.radarcns.domain.restapi.format.Acceleration;
import org.radarcns.domain.restapi.format.Quartiles;
import org.radarcns.domain.restapi.header.DescriptiveStatistic;
import org.radarcns.domain.restapi.header.Header;
import org.radarcns.integration.util.ApiClient;
import org.radarcns.integration.util.ExpectedDataSetFactory;
import org.radarcns.integration.util.Utility;
import org.radarcns.mock.MockProducer;
import org.radarcns.mock.config.MockDataConfig;
import org.radarcns.mock.data.CsvGenerator;
import org.radarcns.mock.data.MockRecordValidator;
import org.radarcns.mock.model.ExpectedValue;
import org.radarcns.mock.model.MockAggregator;
import org.radarcns.producer.rest.RestClient;
import org.radarcns.util.RadarConverter;
import org.radarcns.wiremock.ManagementPortalWireMock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EndToEndTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(EndToEndTest.class);

    private static final String PROJECT_NAME_MOCK = "radar";
    private static final String USER_ID_MOCK = "sub-1";
    private static final String SOURCE_ID_MOCK = "SourceID_0";

    private Map<DescriptiveStatistic, Map<MockDataConfig, Dataset>> expectedDataset;

    private static final ExpectedDataSetFactory expectedDataSetFactory = new ExpectedDataSetFactory();

    private static final TimeWindow TIME_WINDOW = TimeWindow.TEN_SECOND;

    public static final String PIPELINE_CONFIG = "pipeline.yml";
    private static final String[] REQUIRED_TOPICS = {
            "android_empatica_e4_acceleration",
            "android_empatica_e4_acceleration_10sec",
            "android_empatica_e4_battery_level",
            "android_empatica_e4_battery_level_10sec",
            "android_empatica_e4_blood_volume_pulse",
            "android_empatica_e4_blood_volume_pulse_10sec",
            "android_empatica_e4_electrodermal_activity",
            "android_empatica_e4_electrodermal_activity_10sec",
            "android_empatica_e4_heart_rate_10sec",
            "android_empatica_e4_inter_beat_interval",
            "android_empatica_e4_inter_beat_interval_10sec",
            "android_empatica_e4_temperature",
            "android_empatica_e4_temperature_10sec",
            "application_server_status",
            "application_record_counts",
            "application_uptime"
    };

    // Latency expressed in second
    private static final long LATENCY = 180;

    private static File dataRoot;
    private static PipelineConfig pipelineConfig;

    @Rule
    public ManagementPortalWireMock wireMock = new ManagementPortalWireMock();

    @SuppressWarnings("ConstantConditions")
    @Rule
    public final ApiClient apiClient = new ApiClient(pipelineConfig.getRestApi());

    @SuppressWarnings("ConstantConditions")
    @Rule
    public final ApiClient frontendClient = new ApiClient(pipelineConfig.getFrontend());

    /**
     * Test initialisation. It loads the config file and waits that the infrastructure is ready to
     * accept requests.
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
        URL configResource = EndToEndTest.class.getClassLoader().getResource(PIPELINE_CONFIG);
        assertNotNull(configResource);
        File configFile = new File(configResource.getFile());
        try {
            pipelineConfig = new YamlConfigLoader().load(configFile, PipelineConfig.class);
        } catch (IOException e) {
            throw new AssertionError("Cannot load pipeline", e);
        }
        dataRoot = configFile.getAbsoluteFile().getParentFile();

        waitForInfrastructure();
    }

    @Test
    public void endToEnd() throws Exception {
        produceInputFile();

        Map<MockDataConfig, ExpectedValue> expectedValue = MockAggregator.getSimulations(
                pipelineConfig.getData(), dataRoot);

        produceExpectedDataset(expectedValue);

        streamToKafka();

        LOGGER.info("Waiting on data ({} seconds) ... ", LATENCY);
        Thread.sleep(TimeUnit.SECONDS.toMillis(LATENCY));

        assertRestApiMatches();
    }

    private static void waitForInfrastructure() throws InterruptedException, IOException {
        LOGGER.info("Waiting on infrastructure ... ");
        Collection<String> expectedTopics = new HashSet<>(Arrays.asList(REQUIRED_TOPICS));

        int retry = 60;
        long sleep = 1000;

        try (RestClient client = new RestClient(pipelineConfig.getRestProxy())) {
            Request request = client.requestBuilder("topics").build();
            for (int i = 0; i < retry; i++) {
                Response response = client.request(request);
                ResponseBody body = response.body();
                if (response.isSuccessful() && body != null) {
                    String topics = body.string();
                    String[] topicArray = topics.substring(1, topics.length() - 1).replace(
                            "\"", "").split(",");

                    expectedTopics.removeAll(Arrays.asList(topicArray));

                    if (expectedTopics.isEmpty()) {
                        break;
                    }
                }

                Thread.sleep(sleep * (i + 1));
            }
        }

        assertEquals("missing " + expectedTopics, 0, expectedTopics.size());
    }

    /**
     * Generates new random CSV files.
     */
    private void produceInputFile() throws IOException {
        LOGGER.info("Generating CSV files ...");
        for (MockDataConfig config : pipelineConfig.getData()) {
            new CsvGenerator().generate(config, pipelineConfig.getDuration(), dataRoot);
            new MockRecordValidator(config, pipelineConfig.getDuration(), dataRoot).validate();
        }
    }

    /**
     * Starting from the expected values computed using the available CSV files, it computes all the
     * expected Datasets used to test REST-API.
     *
     * @see ExpectedValue
     */
    private void produceExpectedDataset(Map<MockDataConfig, ExpectedValue> expectedValue)
            throws Exception {
        LOGGER.info("Computing expected dataset ...");
        int size = pipelineConfig.getData().size();

        assertEquals(size, expectedValue.size());

        expectedDataset = computeExpectedDataset(expectedValue);

        assertEquals(DescriptiveStatistic.values().length - 2, expectedDataset.size());

        for (Map<MockDataConfig, Dataset> datasets : expectedDataset.values()) {
            assertEquals(size, datasets.size());
        }
    }

    /**
     * This is the actual generator of Datasets exploited by {@link #produceExpectedDataset(Map)}.
     *
     * @see ExpectedValue
     */
    private Map<DescriptiveStatistic, Map<MockDataConfig, Dataset>> computeExpectedDataset(
            Map<MockDataConfig, ExpectedValue> expectedValue) throws Exception {
        Map<DescriptiveStatistic, Map<MockDataConfig, Dataset>> datasets = new HashMap<>();

        for (DescriptiveStatistic stat : DescriptiveStatistic.values()) {
            if (stat.equals(DescriptiveStatistic.LOWER_QUARTILE)
                    || stat.equals(DescriptiveStatistic.UPPER_QUARTILE)
                    || stat.equals(DescriptiveStatistic.RECEIVED_MESSAGES)) {
                continue;
            }

            datasets.put(stat, getExpectedDataset(expectedValue, stat));
        }

        datasets.put(DescriptiveStatistic.RECEIVED_MESSAGES, getReceivedMessage(
                datasets.get(DescriptiveStatistic.COUNT)));

        return datasets;
    }

    private static Map<MockDataConfig, Dataset> getReceivedMessage(
            Map<MockDataConfig, Dataset> expectedCount) {
        Map<MockDataConfig, Dataset> expectedReceivedMessage = new HashMap<>();

        for (MockDataConfig config : expectedCount.keySet()) {
            MockDataConfig updatedConfig = new MockDataConfig();
            updatedConfig.setSensor(config.getSensor());
            updatedConfig.setMaximumDifference(Double.valueOf("1e-2"));

            Dataset dataset = Utility.cloneDataset(expectedCount.get(config));

            Header updatedHeader = dataset.getHeader();
            updatedHeader.setDescriptiveStatistic(DescriptiveStatistic.RECEIVED_MESSAGES);
            updatedHeader.setUnit("PERCENTAGE");
            dataset.setHeader(updatedHeader);

            for (DataItem item : dataset.getDataset()) {
                if (item.getSample() instanceof Double) {
                    Double sample = (Double) item.getSample();
                    item.setSample(RadarConverter.roundDouble(
                            sample / RadarConverter.getExpectedMessages(
                                    updatedHeader.getTimeWindow(), 1), 2));
                } else if (item.getSample() instanceof Acceleration) {
                    Acceleration sample = (Acceleration) item.getSample();
                    item.setSample(new Acceleration(
                            RadarConverter.roundDouble(
                                    (Double) sample.getX() / RadarConverter
                                            .getExpectedMessages(updatedHeader.getTimeWindow(),
                                                    1), 2),
                            RadarConverter.roundDouble(
                                    (Double) sample.getY() / RadarConverter
                                            .getExpectedMessages(updatedHeader.getTimeWindow(),
                                                    1), 2),
                            RadarConverter.roundDouble(
                                    (Double) sample.getZ() / RadarConverter
                                            .getExpectedMessages(updatedHeader.getTimeWindow(),
                                                    1), 2)));
                } else {
                    throw new IllegalArgumentException(
                            item.getSample().getClass().getCanonicalName()
                                    + " is not supported yet");
                }
            }

            expectedReceivedMessage.put(updatedConfig, dataset);
        }

        return expectedReceivedMessage;
    }

    /**
     * Simulates all possible test case scenarios configured in mock-configuration. For each data,
     * it generates one dataset per statistical function. The measurement units are taken from an
     * Empatica device.
     *
     * @param expectedValue {@code Map} of key {@code MockDataConfig} and value {@code
     * ExpectedValue} containing all expected values
     * @param stat statistical value that has be tested
     * @return {@code Map} of key {@code MockDataConfig} and value {@code Dataset}.
     * @see ExpectedValue
     **/
    public static Map<MockDataConfig, Dataset> getExpectedDataset(
            Map<MockDataConfig, ExpectedValue> expectedValue, DescriptiveStatistic stat)
            throws IllegalAccessException, InstantiationException {
        Map<MockDataConfig, Dataset> map = new HashMap<>();

        for (MockDataConfig config : expectedValue.keySet()) {
            map.put(config, expectedDataSetFactory.getDataset(
                    expectedValue.get(config), PROJECT_NAME_MOCK, USER_ID_MOCK, SOURCE_ID_MOCK,
                    "EMPATICA",
                    getSensorType(config), stat, TIME_WINDOW));
        }

        return map;
    }

    /**
     * Streams data stored in CSV files into Kafka.
     */
    private void streamToKafka() throws IOException, InterruptedException {
        LOGGER.info("Streaming data into Kafka ...");
        MockProducer producer = new MockProducer(pipelineConfig, dataRoot);
        producer.start();
        producer.shutdown();
    }

    /**
     * Queries the REST-API for each statistical function and for each data.
     */
    private void assertRestApiMatches()
            throws IOException, ReflectiveOperationException {
        LOGGER.info("Fetching APIs ...");

        final String path =
                DATA + "/{" + SOURCE_DATA_NAME + "}/{" + STAT + "}/" + TimeWindow.TEN_SECOND
                        + '/' + PROJECT_NAME_MOCK + '/' + USER_ID_MOCK + "/" + SOURCE_ID_MOCK;

        for (DescriptiveStatistic stat : expectedDataset.keySet()) {
            String pathStat = path.replace("{" + STAT + "}", stat.name());

            Map<MockDataConfig, Dataset> datasets = expectedDataset.get(stat);

            for (MockDataConfig config : datasets.keySet()) {
                String pathSensor = pathStat.replace("{" + SOURCE_DATA_NAME + "}",
                        getSensorType(config));

                Dataset actual = apiClient.requestJson(pathSensor, Dataset.class, Status.OK);

                assertDatasetEquals(getSensorType(config), datasets.get(config), actual,
                        config.getMaximumDifference());
            }
        }
    }

    /**
     * Checks if the two given datasets are equals. Double values are compared using a constant
     * representing the maximum delta for which both numbers are still considered equal.
     *
     * @see Dataset
     */
    private void assertDatasetEquals(String sensorType, Dataset expected, Dataset actual,
            double delta) {
        assertEquals(expected.getHeader(), actual.getHeader());

        Iterator<DataItem> expectedItems = expected.getDataset().iterator();
        Iterator<DataItem> actualItems = actual.getDataset().iterator();

        while (expectedItems.hasNext()) {
            DataItem expectedItem = expectedItems.next();
            DataItem actualItem = actualItems.next();

            assertEquals(expectedItem.getStartDateTime(), actualItem.getStartDateTime());

            SpecificRecord expectedRecord = (SpecificRecord) expectedItem.getSample();
            SpecificRecord actualRecord = (SpecificRecord) actualItem.getSample();

            switch (sensorType) {
                case "ACCELEROMETER":
                    compareAccelerationItem(expected.getHeader().getDescriptiveStatistic(),
                            (Acceleration) expectedRecord, (Acceleration) actualRecord, delta);
                    break;
                default:
                    compareSingletonItem(actual.getHeader().getDescriptiveStatistic(),
                            expectedRecord, actualRecord, delta);
                    break;
            }
        }

        assertEquals(false, actualItems.hasNext());
    }

    /**
     * Checks if the two given list of Item are equals. Double values are compared using a constant
     * representing the maximum delta for which both numbers are still considered equal.
     *
     * @see DataItem
     */
    private void compareSingletonItem(DescriptiveStatistic stat, SpecificRecord expectedRecord,
            SpecificRecord actualRecord, double delta) {
        int index = expectedRecord.getSchema().getField("value").pos();

        switch (stat) {
            case QUARTILES:
                compareQuartiles(((Quartiles) expectedRecord.get(index)),
                        ((Quartiles) actualRecord.get(index)), delta);
                break;
            default:
                assertEquals((Double) expectedRecord.get(index),
                        (Double) actualRecord.get(index), delta);
                break;
        }
    }

    /**
     * Checks if the two given list of Item of type Acceleration values are equals. Double values
     * are compared using a constant representing the maximum delta for which both numbers are still
     * considered equal.
     *
     * @see DataItem
     * @see Acceleration
     */
    private void compareAccelerationItem(DescriptiveStatistic stat, Acceleration expectedRecord,
            Acceleration actualRecord, double delta) {
        switch (stat) {
            case QUARTILES:
                compareQuartiles(((Quartiles) expectedRecord.getX()),
                        ((Quartiles) actualRecord.getX()), delta);
                compareQuartiles(((Quartiles) expectedRecord.getY()),
                        ((Quartiles) actualRecord.getY()), delta);
                compareQuartiles(((Quartiles) expectedRecord.getZ()),
                        ((Quartiles) actualRecord.getZ()), delta);
                break;
            default:
                assertEquals(((Double) expectedRecord.getX()),
                        ((Double) actualRecord.getX()), delta);
                assertEquals(((Double) expectedRecord.getY()),
                        ((Double) actualRecord.getY()), delta);
                assertEquals(((Double) expectedRecord.getZ()),
                        ((Double) actualRecord.getZ()), delta);
                break;
        }
    }

    /**
     * Checks if the two given list of Item of Quartiles values are equals. Double values are
     * compared using a constant representing the maximum delta for which both numbers are still
     * considered equal.
     *
     * @see DataItem
     * @see Quartiles
     */
    private void compareQuartiles(Quartiles expectedQuartiles, Quartiles actualQuartiles,
            double delta) {
        assertEquals(expectedQuartiles.getFirst(), actualQuartiles.getFirst(), delta);
        assertEquals(expectedQuartiles.getSecond(),
                actualQuartiles.getSecond(), delta);
        assertEquals(expectedQuartiles.getThird(), actualQuartiles.getThird(), delta);
    }


    /**
     * Converts data value string to SensorType.
     */
    private static String getSensorType(MockDataConfig config) {
        if (config.getSensor().equals("BATTERY_LEVEL")) {
            return "BATTERY";
        }
        return config.getSensor().toUpperCase();
    }

    /**
     * Checks the correctness of the generated swagger documentation making the request via NGINX.
     *
     * @throws MalformedURLException if the used URL is malformed
     */
    @Test
    public void checkSwaggerConfig() throws IOException {
        String response = apiClient.requestString(OPENAPI_JSON, APPLICATION_JSON, Status.OK);
        JsonNode node = new ObjectMapper().readTree(response);
        assertTrue(node.has("openapi"));
        assertTrue(node.get("openapi").asText().startsWith("3."));
    }
}
