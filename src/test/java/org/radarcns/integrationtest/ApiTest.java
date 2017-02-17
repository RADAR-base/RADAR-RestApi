package org.radarcns.integrationtest;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;
import org.apache.avro.specific.SpecificRecord;
import org.junit.Before;
import org.junit.Test;
import org.radarcns.avro.restapi.dataset.Dataset;
import org.radarcns.dao.mongo.util.MongoHelper;
import org.radarcns.dao.mongo.util.MongoHelper.Stat;
import org.radarcns.integrationtest.collector.ExpectedValue;
import org.radarcns.integrationtest.config.MockConfig;
import org.radarcns.integrationtest.config.MockDataConfig;
import org.radarcns.integrationtest.util.CSVValidator;
import org.radarcns.integrationtest.util.HttpClient;
import org.radarcns.integrationtest.util.MockAggregator;
import org.radarcns.integrationtest.util.URLGenerator;
import org.radarcns.util.RadarConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Francesco Nobilia on 10/01/2017.
 */
public class ApiTest {

    private static final Logger logger = LoggerFactory.getLogger(ApiTest.class);

    private final boolean TEST = true;
    private List<MockDataConfig> configs;

    @Before
    public void runBeforeTestMethod() throws Exception{
        if( TEST ) {
            configs = MockConfig.load(getClass().getClassLoader()).getData();

            // Validate input
            for ( MockDataConfig config : configs ) {
                CSVValidator.validate(config);
            }
        }
    }


    @Test
    public void test() throws Exception {
        if( TEST ) {
            HttpClient client = new HttpClient();
            Map<MockDataConfig, ExpectedValue> map = MockAggregator.getSimulations(configs);

            for ( MockDataConfig config : map.keySet() ) {
                ExpectedValue expected = map.get(config);

                for ( MongoHelper.Stat stat : Stat.values() ) {
                    String url = URLGenerator.generate(expected.getUser(), expected.getSource(),
                        RadarConverter.getDescriptiveStatistic(stat), config);
                    logger.info(url);

                    Dataset result = expected.getDataset(
                        RadarConverter.getDescriptiveStatistic(stat), config);

                    SpecificRecord response = client.doGetRequest(url);

                    assertEquals(true, ExpectedValue.compareDatasets(result, response,
                        0.0000001));
                }
            }
        }
    }

}