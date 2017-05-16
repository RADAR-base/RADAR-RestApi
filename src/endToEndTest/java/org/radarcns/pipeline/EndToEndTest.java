package org.radarcns.pipeline;

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

import static org.junit.Assert.assertEquals;
import static org.radarcns.integration.testcase.config.ExposedConfigTest.CONFIG_JSON;
import static org.radarcns.integration.testcase.config.ExposedConfigTest.checkFrontEndConfig;
import static org.radarcns.integration.testcase.config.ExposedConfigTest.getSwaggerBasePath;
import static org.radarcns.webapp.Parameter.INTERVAL;
import static org.radarcns.webapp.Parameter.SENSOR;
import static org.radarcns.webapp.Parameter.SOURCE_ID;
import static org.radarcns.webapp.Parameter.STAT;
import static org.radarcns.webapp.Parameter.SUBJECT_ID;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import okhttp3.Response;
import org.apache.avro.specific.SpecificRecord;
import org.junit.Test;
import org.radarcns.avro.restapi.data.Acceleration;
import org.radarcns.avro.restapi.data.Quartiles;
import org.radarcns.avro.restapi.dataset.Dataset;
import org.radarcns.avro.restapi.dataset.Item;
import org.radarcns.avro.restapi.header.DescriptiveStatistic;
import org.radarcns.avro.restapi.header.TimeFrame;
import org.radarcns.avro.restapi.sensor.SensorType;
import org.radarcns.avro.restapi.source.SourceType;
import org.radarcns.config.Properties;
import org.radarcns.config.YamlConfigLoader;
import org.radarcns.integration.aggregator.MockAggregator;
import org.radarcns.integration.model.ExpectedValue;
import org.radarcns.integration.util.ExpectedDataSetFactory;
import org.radarcns.integration.util.Utility;
import org.radarcns.mock.CsvGenerator;
import org.radarcns.mock.CsvSensorDataModel;
import org.radarcns.mock.MockDataConfig;
import org.radarcns.mock.MockProducer;
import org.radarcns.pipeline.config.PipelineConfig;
import org.radarcns.pipeline.data.CsvValidator;
import org.radarcns.util.AvroConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EndToEndTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(EndToEndTest.class);

    private Map<DescriptiveStatistic, Map<MockDataConfig, Dataset>> expectedDataset;

    private static ExpectedDataSetFactory expectedDataSetFactory = new ExpectedDataSetFactory();

    private static final TimeFrame TIME_FRAME = TimeFrame.TEN_SECOND;

    private static PipelineConfig config = null;

    public static final String PIPELINE_CONFIG = "pipeline.yml";
    // Latency expressed in second
    private static final long LATENCY = 30;

    private static class BaseFile {
        private static final File file = new File(
                EndToEndTest.class.getClassLoader().getResource(PIPELINE_CONFIG).getFile());
    }

    @Test
    public void endToEnd() throws Exception {
        getPipelineConfig();

        waitInfrastructure();

        produceInputFile();

        Map<MockDataConfig, ExpectedValue> expectedValue = MockAggregator
                .getSimulations(getPipelineConfig().getData());

        produceExpectedDataset(expectedValue);

        streamToKafka();

        LOGGER.info("Waiting data ({} seconds) ... ", LATENCY);
        Thread.sleep(TimeUnit.SECONDS.toMillis(LATENCY));

        fetchRestApi();

        checkSwaggerConfig();

        checkFrontendConfig();
    }

    private static PipelineConfig getPipelineConfig() {
        if (config == null) {

            try {
                config = new YamlConfigLoader().load(
                        new File(
                                EndToEndTest.class.getClassLoader()
                                        .getResource(PIPELINE_CONFIG).getFile()
                        ), PipelineConfig.class);
            } catch (IOException exec) {
                exec.printStackTrace();
            }
        }
        return config;
    }

    /**
     * Checks if the test bed is ready to accept data.
     */
    private void waitInfrastructure() throws InterruptedException {
        LOGGER.info("Waiting infrastructure ... ");

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

        int retry = 60;
        long sleep = 1000;
        int count = 0;

        for (int i = 0; i < retry; i++) {
            count = 0;

            Response response = null;
            try {
                response = Utility.makeUnsafeRequest(
                        getPipelineConfig().getRestProxy().getUrlString() + "topics");
                if (response.code() == 200) {
                    String topics = response.body().string().toString();
                    String[] topicArray = topics.substring(1, topics.length() - 1).replace(
                                "\"", "").split(",");

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
            } catch (NoSuchAlgorithmException exec) {
                LOGGER.info("Error while waiting infrastructure", exec);
            } catch (KeyManagementException exec) {
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
        for (MockDataConfig config : getPipelineConfig().getData()) {
            CsvGenerator.generate(config, getPipelineConfig().getDuration(), BaseFile.file);
            CsvValidator.validate(config, getPipelineConfig().getDuration());
        }
    }

    /**
     * Starting from the expected values computed using the available CSV files, it computes all
     * the expected Datasets used to test REST-API.
     *
     * @see {@link ExpectedValue}
     */
    private void produceExpectedDataset(Map<MockDataConfig, ExpectedValue> expectedValue)
            throws Exception {
        LOGGER.info("Computing expected dataset ...");
        int tastCase = getPipelineConfig().getData().size();

        assertEquals(tastCase, expectedValue.size());

        expectedDataset = computeExpectedDataset(expectedValue);

        assertEquals(DescriptiveStatistic.values().length - 2, expectedDataset.size());

        for (Map<MockDataConfig, Dataset> datasets : expectedDataset.values()) {
            assertEquals(tastCase, datasets.size());
        }
    }

    /**
     * This is the actual generator of Datasets exploited by {@link #produceExpectedDataset(Map)}.
     *
     * @see {@link ExpectedValue}
     */
    private Map<DescriptiveStatistic, Map<MockDataConfig, Dataset>> computeExpectedDataset(
            Map<MockDataConfig, ExpectedValue> expectedValue) throws Exception {
        Map<DescriptiveStatistic, Map<MockDataConfig, Dataset>> datasets = new HashMap<>();

        for (DescriptiveStatistic stat : DescriptiveStatistic.values()) {

            if (stat.equals(DescriptiveStatistic.LOWER_QUARTILE)
                    || stat.equals(DescriptiveStatistic.UPPER_QUARTILE)) {
                continue;
            }

            datasets.put(stat, getExpecetedDataset(expectedValue, stat));
        }

        return datasets;
    }

    /**
     * Simulates all possible test case scenarios configured in mock-configuration. For each data,
     * it generates one dataset per statistical function. The measurement units are taken from
     * an Empatica device.
     *
     * @param expectedValue {@code Map} of key {@code MockDataConfig} and value {@code
     * ExpectedValue} containing all expected values
     * @param stat statistical value that has be tested
     * @return {@code Map} of key {@code MockDataConfig} and value {@code Dataset}.
     * @see {@link ExpectedValue}.
     **/
    public static Map<MockDataConfig, Dataset> getExpecetedDataset(
            Map<MockDataConfig, ExpectedValue> expectedValue, DescriptiveStatistic stat)
            throws ClassNotFoundException, NoSuchMethodException, IOException,
            IllegalAccessException, InvocationTargetException, InstantiationException {
        Map<MockDataConfig, Dataset> map = new HashMap<>();

        for (MockDataConfig config : expectedValue.keySet()) {
            map.put(config, expectedDataSetFactory
                    .getDataset(expectedValue.get(config), CsvSensorDataModel.USER_ID_MOCK,
                        CsvSensorDataModel.SOURCE_ID_MOCK, SourceType.EMPATICA,
                            getSensorType(config), stat, TIME_FRAME));
        }

        return map;
    }

    /**
     * Streams data stored in CSV files into Kafka.
     */
    private void streamToKafka() throws IOException, InterruptedException {
        LOGGER.info("Streaming data into Kafka ...");
        MockProducer producer = new MockProducer(getPipelineConfig());
        producer.start();
        producer.shutdown();
    }

    /**
     * Queries the REST-API for each statistical function and for each data.
     */
    private void fetchRestApi() throws IOException, KeyManagementException,
            NoSuchAlgorithmException {
        LOGGER.info("Fetching APIs ...");

        String server = getPipelineConfig().getRestApi().getUrlString();
        String path = server + "data/avro/{" + SENSOR + "}/{" + STAT + "}/{" + INTERVAL + "}/{"
                + SUBJECT_ID + "}/{" + SOURCE_ID + "}";
        path = path.replace("{" + SUBJECT_ID + "}", CsvSensorDataModel.USER_ID_MOCK);
        path = path.replace("{" + SOURCE_ID + "}", CsvSensorDataModel.SOURCE_ID_MOCK);

        path = path.replace("{" + INTERVAL + "}", TimeFrame.TEN_SECOND.name());

        for (DescriptiveStatistic stat : expectedDataset.keySet()) {
            String pathStat = path.replace("{" + STAT + "}", stat.name());

            Map<MockDataConfig, Dataset> datasets = expectedDataset.get(stat);

            for (MockDataConfig config : datasets.keySet()) {
                String pathSensor = pathStat.replace("{" + SENSOR + "}",
                        getSensorType(config).name());

                Response response = Utility.makeUnsafeRequest(pathSensor);

                LOGGER.info("[{}] Requesting {}", response.code(), pathSensor);

                assertEquals(200, response.code());

                Dataset actual = null;

                if (response.code() == 200) {
                    actual = AvroConverter.avroByteToAvro(response.body().bytes(),
                            Dataset.getClassSchema());
                }

                assertDatasetEquals(getSensorType(config), datasets.get(config), actual,
                        getDelta(config));
            }

        }
    }

    /**
     * Checks if the two given datasets are equals. Double values are compared using a constant
     * representing the maximum delta for which both numbers are still considered equal.
     *
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

            assertEquals(expectedItem.getStartDateTime(), actualItem.getStartDateTime());

            SpecificRecord expectedRecord = (SpecificRecord) expectedItem.getSample();
            SpecificRecord actualRecord = (SpecificRecord) actualItem.getSample();

            switch (sensorType) {
                case ACCELEROMETER:
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

    private double getDelta(MockDataConfig config) {
        return config.getMagnitude() == 0 ? 0.0 : Math.pow(10.0, -1.0 * config.getMagnitude());
    }

    /**
     * Checks if the two given list of Item are equals. Double values are compared using a constant
     * representing the maximum delta for which both numbers are still considered equal.
     *
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
                break;
        }
    }

    /**
     * Checks if the two given list of Item of type Acceleration values are equals. Double values
     * are compared using a constant representing the maximum delta for which both numbers are
     * still considered equal.
     *
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
                break;
        }
    }

    /**
     * Checks if the two given list of Item of Quartiles values are equals. Double values are
     * compared using a constant representing the maximum delta for which both numbers are
     * still considered equal.
     *
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


    /**
     * Converts data value string to SensorType.
     *
     * @throws IllegalArgumentException if the specified data does not match any of the already
     *          known ones
     */
    public static SensorType getSensorType(MockDataConfig config) {
        if (config.getSensor().equals("BATTERY_LEVEL")) {
            return SensorType.BATTERY;
        }
        for (SensorType type : SensorType.values()) {

            if (type.name().equalsIgnoreCase(config.getSensor())) {
                return type;
            }
        }

        throw new IllegalArgumentException(config.getSensor() + " unknown data");
    }

    /**
     * Checks the correctness of the generated swagger documentation making the request via NGINX.
     *
     * @throws MalformedURLException if the used URL is malformed
     */
    private static void checkSwaggerConfig()
        throws IOException, NoSuchAlgorithmException, KeyManagementException {
        LOGGER.info("Checking Swagger ...");

        assertEquals(Properties.getApiConfig().getApiBasePath(), getSwaggerBasePath(
                getPipelineConfig().getRestApi().getUrl(),
                getPipelineConfig().getRestApi().isUnsafe()));
    }

    /**
     * Checks the correctness of the deployed frontend configuration file making the request via
     *      NGINX.
     *
     * @throws IOException either if the used URL is malformed or the response containing the
     *      downloaded file cannot be parsed.
     */
    private static void checkFrontendConfig()
            throws IOException, NoSuchAlgorithmException, KeyManagementException {
        LOGGER.info("Checking Frontend config ...");

        URL url = new URL(
                config.getRestApi().getProtocol(),
                config.getRestApi().getHost(),
                config.getRestApi().getPort(),
                "/frontend/config/");

        String actual = checkFrontEndConfig(url, getPipelineConfig().getRestApi().isUnsafe());

        String expected = Utility.fileToString(
                EndToEndTest.class.getClassLoader().getResource(CONFIG_JSON).getFile());

        assertEquals(expected, actual);
    }
}
