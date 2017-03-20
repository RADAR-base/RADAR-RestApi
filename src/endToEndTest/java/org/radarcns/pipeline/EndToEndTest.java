package org.radarcns.pipeline;

/*
 *  Copyright 2016 Kings College London and The Hyve
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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import okhttp3.Response;
import org.apache.avro.specific.SpecificRecord;
import org.bson.Document;
import org.junit.Test;
import org.radarcns.avro.restapi.dataset.Dataset;
import org.radarcns.avro.restapi.dataset.Item;
import org.radarcns.avro.restapi.dataset.Quartiles;
import org.radarcns.avro.restapi.header.DescriptiveStatistic;
import org.radarcns.avro.restapi.sensor.Acceleration;
import org.radarcns.avro.restapi.sensor.SensorType;
import org.radarcns.integration.aggregator.ExpectedValue;
import org.radarcns.integration.util.Utility;
import org.radarcns.pipeline.config.Config;
import org.radarcns.pipeline.data.CsvGenerator;
import org.radarcns.pipeline.data.CsvSensor;
import org.radarcns.pipeline.data.CsvValidator;
import org.radarcns.pipeline.mock.MockAggregator;
import org.radarcns.pipeline.mock.Producer;
import org.radarcns.pipeline.mock.config.MockDataConfig;
import org.radarcns.util.AvroConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EndToEndTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(EndToEndTest.class);

    private Producer producer;
    private Map<DescriptiveStatistic, Map<MockDataConfig, Dataset>> expectedDataset;

    private Map<String, Collection<Document>> expectedDocument;

    // Latency expressed in second
    private static final long LATENCY = 30;

    @Test
    public void endToEnd() throws Exception {
        waitInfrastructure();

        produceInputFile();

        Map<MockDataConfig, ExpectedValue> expectedValue = MockAggregator.getSimulations();

        produceExpectedDataset(expectedValue);

//        Map<MockDataConfig, Collection<Document>> expectedDuments =
//            getExpecetedDocument(expectedValue);

        streamToKafka();

        LOGGER.info("Waiting data ({} seconds) ... ", LATENCY);
        Thread.sleep(TimeUnit.SECONDS.toMillis(LATENCY));

        fetchRestApi();
    }

    /**
     * Checks if the test bed is ready to accept data.
     */
    private void waitInfrastructure() throws InterruptedException {
        LOGGER.info("Waiting infrastructure ... ");
        int retry = 60;
        long sleep = 1000;
        int count = 0;

        List<String> expectedTopics = new LinkedList<>();
        expectedTopics.add("android_empatica_e4_acceleration");
        expectedTopics.add("android_empatica_e4_acceleration_output");
        expectedTopics.add("android_empatica_e4_battery_level");
        expectedTopics.add("android_empatica_e4_battery_level_output");
        expectedTopics.add("android_empatica_e4_blood_volume_pulse");
        expectedTopics.add("android_empatica_e4_blood_volume_pulse_output");
        expectedTopics.add("android_empatica_e4_electrodermal_activity");
        expectedTopics.add("android_empatica_e4_electrodermal_activity_output");
        expectedTopics.add("android_empatica_e4_heartrate");
        expectedTopics.add("android_empatica_e4_inter_beat_interval");
        expectedTopics.add("android_empatica_e4_inter_beat_interval_output");
        expectedTopics.add("android_empatica_e4_sensor_status");
        expectedTopics.add("android_empatica_e4_sensor_status_output");
        expectedTopics.add("android_empatica_e4_temperature");
        expectedTopics.add("android_empatica_e4_temperature_output");
        expectedTopics.add("application_server_status");
        expectedTopics.add("application_record_counts");
        expectedTopics.add("application_uptime");

        for (int i = 0; i < retry; i++) {
            count = 0;

            Response response = null;
            try {
                response = Utility.makeRequest(
                        Config.getPipelineConfig().getRestProxyInstance() + "/topics");
                if (response.code() == 200) {
                    String topics = response.body().string().toString();
                    String[] topicArray = topics.substring(1, topics.length() - 1).replace("\"", "")
                        .split(",");

                    for (String topic : topicArray) {
                        if (expectedTopics.contains(topic)) {
                            count++;
                        }
                    }

                    if (count == expectedTopics.size()) {
                        break;
                    }
                }
            } catch (IOException exec) {
                LOGGER.info("Error while waiting infrastructure", exec);
            }

            Thread.sleep(sleep * (i + 1));
        }

        assertEquals(expectedTopics.size(), count);
    }

    /**
     * Generates new random CSV files.
     */
    private void produceInputFile()
        throws IOException, ClassNotFoundException, NoSuchMethodException,
            InvocationTargetException, ParseException, IllegalAccessException {
        LOGGER.info("Generating CVS files ...");
        for (MockDataConfig config : Config.getMockConfig().getData()) {
            CsvGenerator.generate(config, Config.getPipelineConfig().getDuration());
            CsvValidator.validate(config);
        }
    }

    /**
     * Starting from the expected values computed using the available CSV files, it computes all
     *      the expected Datasets used to test REST-API.
     * @see {@link org.radarcns.integration.aggregator.ExpectedValue}
     */
    private void produceExpectedDataset(Map<MockDataConfig, ExpectedValue> expectedValue)
            throws Exception {
        LOGGER.info("Computing expected dataset ...");
        int tastCase = Config.getMockConfig().getData().size();

        assertEquals(tastCase, expectedValue.size());

        expectedDataset = computeExpectedDataset(expectedValue);

        assertEquals(DescriptiveStatistic.values().length - 2, expectedDataset.size());

        for (Map<MockDataConfig, Dataset> datasets : expectedDataset.values()) {
            assertEquals(tastCase, datasets.size());
        }
    }

    /**
     * This is the actual generator of Datasets exploited by {@link #produceExpectedDataset(Map)}.
     * @see {@link org.radarcns.integration.aggregator.ExpectedValue}
     */
    private Map<DescriptiveStatistic, Map<MockDataConfig, Dataset>> computeExpectedDataset(
            Map<MockDataConfig, ExpectedValue> expectedValue) throws Exception {
        Map<DescriptiveStatistic, Map<MockDataConfig, Dataset>> datasets = new HashMap<>();

        for (DescriptiveStatistic stat : DescriptiveStatistic.values()) {

            if (stat.equals(DescriptiveStatistic.LOWER_QUARTILE)
                    || stat.equals(DescriptiveStatistic.UPPER_QUARTILE)) {
                continue;
            }

            datasets.put(stat, MockAggregator.getExpecetedDataset(expectedValue, stat));
        }

        return datasets;
    }

    /**
     * Streams data stored in CSV files into Kafka.
     */
    private void streamToKafka() throws IOException, InterruptedException {
        LOGGER.info("Streaming data into Kafka ...");
        producer = new Producer();
        producer.start();
        producer.shutdown();
    }

    /**
     * Queries the REST-API for each statistical function and for each sensor.
     */
    private void fetchRestApi() throws IOException {
        LOGGER.info("Fetching APIs ...");

        String server = Config.getPipelineConfig().getRestApiInstance();
        String path = server + "sensor/avro/{sensor}/{stat}/{userID}/{sourceID}";
        path = path.replace("{userID}", CsvSensor.userIdMock);
        path = path.replace("{sourceID}", CsvSensor.sourceIdMock);

        for (DescriptiveStatistic stat : expectedDataset.keySet()) {
            String pathStat = path.replace("{stat}", stat.name());

            Map<MockDataConfig, Dataset> datasets = expectedDataset.get(stat);

            for (MockDataConfig config : datasets.keySet()) {
                String pathSensor = pathStat.replace("{sensor}", config.getSensorType().name());

                LOGGER.info("Requesting {}", pathSensor);

                Response response = Utility.makeRequest(pathSensor);
                assertEquals(200, response.code());

                Dataset actual = null;

                if (response.code() == 200) {
                    actual = AvroConverter.avroByteToAvro(response.body().bytes(),
                        Dataset.getClassSchema());
                }

                assertDatasetEquals(config.getSensorType(), datasets.get(config), actual,
                        getDelta(config));
            }

        }
    }

    /**
     * Checks if the two given datasets are equals. Double values are compared using a constant
     *      representing the maximum delta for which both numbers are still considered equal.
     * @see {@link Dataset}
     */
    private void assertDatasetEquals(SensorType sensorType, Dataset expected, Dataset actual,
            double delta) {
        assertEquals(expected.getHeader(), actual.getHeader());

        Iterator<Item> expectedItems = expected.getDataset().iterator();
        Iterator<Item> actualItems = actual.getDataset().iterator();

        while (expectedItems.hasNext()) {
            Item expectedItem = expectedItems.next();
            Item actualItem = actualItems.next();

            assertEquals(expectedItem.getEffectiveTimeFrame(), actualItem.getEffectiveTimeFrame());

            SpecificRecord expectedRecord = (SpecificRecord) expectedItem.getValue();
            SpecificRecord actualRecord = (SpecificRecord) actualItem.getValue();

            switch (sensorType) {
                case ACCELEROMETER:
                    compareAccelerationItem(expected.getHeader().getDescriptiveStatistic(),
                            (Acceleration) expectedRecord, (Acceleration) actualRecord, delta);
                    break;
                default:
                    compareSingletonItem(actual.getHeader().getDescriptiveStatistic(),
                            expectedRecord, actualRecord, delta);
            }
        }

        assertEquals(false, actualItems.hasNext());
    }

    private double getDelta(MockDataConfig config) {
        return config.getMagnitude() == 0 ? 0.0 : Math.pow(10.0, -1.0 * config.getMagnitude());
    }

    /**
     * Checks if the two given list of Item are equals. Double values are compared using a constant
     *      representing the maximum delta for which both numbers are still considered equal.
     * @see {@link Item}
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
        }
    }

    /**
     * Checks if the two given list of Item of type Acceleration values are equals. Double values
     *      are compared using a constant representing the maximum delta for which both numbers are
     *      still considered equal.
     * @see {@link Item}
     * @see {@link Acceleration}
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
        }
    }

    /**
     * Checks if the two given list of Item of Quartiles values are equals. Double values are
     *      compared using a constant representing the maximum delta for which both numbers are
     *      still considered equal.
     * @see {@link Item}
     * @see {@link Quartiles}
     */
    private void compareQuartiles(Quartiles expectedQuartiles, Quartiles actualQuartiles,
            double delta) {
        assertEquals(expectedQuartiles.getFirst(), actualQuartiles.getFirst(), delta);
        assertEquals(expectedQuartiles.getSecond(),
                actualQuartiles.getSecond(), delta);
        assertEquals(expectedQuartiles.getThird(), actualQuartiles.getThird(), delta);
    }

//    /**
//     * Starting from the expected values computed using the available CSV files, it computes all
//     *      the expected Collection of Bson Document that the MongoDb Connector have to be present
//     *      in MongoDb.
//     * @see {@link org.radarcns.integration.aggregator.ExpectedValue}
//     */
//    private Map<MockDataConfig, Collection<Document>> getExpecetedDocument(
//            Map<MockDataConfig, ExpectedValue> expectedValue) {
//        return MockAggregator.getExpecetedDocument(expectedValue);
//    }

}
