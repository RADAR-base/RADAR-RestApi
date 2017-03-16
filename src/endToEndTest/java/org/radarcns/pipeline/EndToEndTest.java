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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import okhttp3.Response;
import org.apache.avro.specific.SpecificRecord;
import org.junit.Test;
import org.radarcns.avro.restapi.dataset.Dataset;
import org.radarcns.avro.restapi.dataset.Item;
import org.radarcns.avro.restapi.dataset.Quartiles;
import org.radarcns.avro.restapi.header.DescriptiveStatistic;
import org.radarcns.avro.restapi.sensor.SensorType;
import org.radarcns.integration.aggregator.ExpectedValue;
import org.radarcns.integration.util.Utility;
import org.radarcns.pipeline.config.Config;
import org.radarcns.pipeline.data.CsvGenerator;
import org.radarcns.pipeline.data.CsvSensor;
import org.radarcns.pipeline.data.CsvValidator;
import org.radarcns.pipeline.data.Metronome;
import org.radarcns.pipeline.mock.MockAggregator;
import org.radarcns.pipeline.mock.Producer;
import org.radarcns.pipeline.mock.config.MockDataConfig;
import org.radarcns.util.AvroConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EndToEndTest {

    private static final Logger logger = LoggerFactory.getLogger(EndToEndTest.class);
    private static final double MAGNITUDE = 0;

    private Producer producer;
    private Map<DescriptiveStatistic, Map<MockDataConfig, Dataset>> expectedDataset;

    @Test
    public void test() {

        List<Long> timestamps = Metronome.timestamps(24*60*60, 1, 64);
    }

//    @Test
//    public void endToEnd() throws Exception {
////        produceInputFile();
//
//        produceExpectedDataset();
//
////        streamToKafka();
//
//        fetchRestApi();
//    }


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

                assertDatasetEquals(datasets.get(config), actual, getEpsilon());
            }

        }
    }

    private void assertDatasetEquals(Dataset expected, Dataset actual, double epsilon) {

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

            int index = expectedRecord.getSchema().getField("value").pos();

            switch (actual.getHeader().getDescriptiveStatistic()) {
                case QUARTILES:
                    Quartiles expectedQuartiles = ((Quartiles) expectedRecord.get(index));
                    Quartiles actualQuartiles = ((Quartiles) actualRecord.get(index));

                    assertEquals(expectedQuartiles.getFirst(), actualQuartiles.getFirst(), epsilon);
                    assertEquals(expectedQuartiles.getSecond(),
                            actualQuartiles.getSecond(), epsilon);
                    assertEquals(expectedQuartiles.getThird(), actualQuartiles.getThird(), epsilon);
                    break;
                default:
                    assertEquals((Double) expectedRecord.get(index),
                            (Double) actualRecord.get(index), epsilon);
            }
        }

        assertEquals(false, actualItems.hasNext());
    }

    private double getEpsilon() {
        return MAGNITUDE == 0 ? 0.0 : Math.pow(10.0, -1.0 * MAGNITUDE);
    }

}
