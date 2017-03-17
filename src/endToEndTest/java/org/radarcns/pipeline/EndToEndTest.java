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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import okhttp3.Response;
import org.apache.avro.specific.SpecificRecord;
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

    private static final Logger logger = LoggerFactory.getLogger(EndToEndTest.class);

    private Producer producer;
    private Map<DescriptiveStatistic, Map<MockDataConfig, Dataset>> expectedDataset;

    @Test
    public void endToEnd() throws Exception {
        produceInputFile();

        produceExpectedDataset();

        streamToKafka();

        Thread.sleep(TimeUnit.SECONDS.toMillis(30));

        fetchRestApi();
    }


    private void produceInputFile()
        throws IOException, ClassNotFoundException, NoSuchMethodException,
            InvocationTargetException, ParseException, IllegalAccessException {
        logger.info("Generating CVS files ...");
        for (MockDataConfig config : Config.getMockConfig().getData()) {
            CsvGenerator.generate(config, Config.getPipelineConfig().getDuration());
            CsvValidator.validate(config);
        }
    }

    private void produceExpectedDataset() throws Exception{
        logger.info("Computing expected dataset ...");
        int tastCase = Config.getMockConfig().getData().size();

        Map<MockDataConfig, ExpectedValue> expectedValue = MockAggregator.getSimulations();
        assertEquals(tastCase, expectedValue.size());

        expectedDataset = computeExpectedDataset(expectedValue);

        assertEquals(DescriptiveStatistic.values().length - 2, expectedDataset.size());

        for (Map<MockDataConfig, Dataset> datasets : expectedDataset.values()) {
            assertEquals(tastCase, datasets.size());
        }
    }

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

    private void streamToKafka() throws IOException, InterruptedException {
        logger.info("Streaming data into Kafka ...");
        producer = new Producer();
        producer.start();
        producer.shutdown();
    }

    private void fetchRestApi() throws IOException {
        logger.info("Fetching APIs ...");

        String server = Config.getPipelineConfig().getRestApiInstance();
        String path = server + "sensor/avro/{sensor}/{stat}/{userID}/{sourceID}";
        path = path.replace("{userID}", CsvSensor.userIdMock);
        path = path.replace("{sourceID}", CsvSensor.sourceIdMock);

        for (DescriptiveStatistic stat : expectedDataset.keySet()) {
            String pathStat = path.replace("{stat}", stat.name());

            Map<MockDataConfig, Dataset> datasets = expectedDataset.get(stat);

            for (MockDataConfig config : datasets.keySet()) {
                String pathSensor = pathStat.replace("{sensor}", config.getSensorType().name());

                logger.info("Requesting {}", pathSensor);

                Response response = Utility.makeRequest(pathSensor);
                assertEquals(200, response.code());

                Dataset actual = null;

                if (response.code() == 200) {
                    actual = AvroConverter.avroByteToAvro(response.body().bytes(),
                        Dataset.getClassSchema());
                }

                assertDatasetEquals(config.getSensorType(), datasets.get(config), actual,
                        getEpsilon(config));
            }

        }
    }

    private void assertDatasetEquals(SensorType sensorType, Dataset expected, Dataset actual,
            double epsilon) {

//        try {
//            System.out.println(RadarConverter.getPrettyJSON(expected));
//            System.out.println(RadarConverter.getPrettyJSON(actual));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

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
                            (Acceleration) expectedRecord, (Acceleration) actualRecord, epsilon);
                    break;
                default:
                    compareSingletonItem(actual.getHeader().getDescriptiveStatistic(),
                            expectedRecord, actualRecord, epsilon);
            }
        }

        assertEquals(false, actualItems.hasNext());
    }

    private double getEpsilon(MockDataConfig config) {
        return config.getMagnitude() == 0 ? 0.0 : Math.pow(10.0, -1.0 * config.getMagnitude());
    }

    private void compareSingletonItem(DescriptiveStatistic stat, SpecificRecord expectedRecord,
            SpecificRecord actualRecord, double epsilon) {
        int index = expectedRecord.getSchema().getField("value").pos();

        switch (stat) {
            case QUARTILES:
                compareQuartiles(((Quartiles) expectedRecord.get(index)),
                        ((Quartiles) actualRecord.get(index)), epsilon);
                break;
            default:
                assertEquals((Double) expectedRecord.get(index),
                    (Double) actualRecord.get(index), epsilon);
        }
    }

    private void compareAccelerationItem(DescriptiveStatistic stat, Acceleration expectedRecord,
            Acceleration actualRecord, double epsilon) {
        switch (stat) {
            case QUARTILES:
                compareQuartiles(((Quartiles) expectedRecord.getX()),
                    ((Quartiles) actualRecord.getX()), epsilon);
                compareQuartiles(((Quartiles) expectedRecord.getY()),
                    ((Quartiles) actualRecord.getY()), epsilon);
                compareQuartiles(((Quartiles) expectedRecord.getZ()),
                    ((Quartiles) actualRecord.getZ()), epsilon);
                break;
            default:
                assertEquals(((Double) expectedRecord.getX()),
                    ((Double) actualRecord.getX()), epsilon);
                assertEquals(((Double) expectedRecord.getY()),
                    ((Double) actualRecord.getY()), epsilon);
                assertEquals(((Double) expectedRecord.getZ()),
                    ((Double) actualRecord.getZ()), epsilon);
        }
    }

    private void compareQuartiles(Quartiles expectedQuartiles, Quartiles actualQuartiles,
            double epsilon) {
        assertEquals(expectedQuartiles.getFirst(), actualQuartiles.getFirst(), epsilon);
        assertEquals(expectedQuartiles.getSecond(),
            actualQuartiles.getSecond(), epsilon);
        assertEquals(expectedQuartiles.getThird(), actualQuartiles.getThird(), epsilon);
    }

}
