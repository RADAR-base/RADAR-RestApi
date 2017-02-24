package org.radarcns.integrationtest;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import org.radarcns.integrationtest.cvs.CSVValidator;
import org.radarcns.integrationtest.util.HttpClient;
import org.radarcns.integrationtest.util.MockAggregator;
import org.radarcns.integrationtest.util.URLGenerator;
import org.radarcns.util.RadarConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point for testing APIs.
 */
public class ApiTest {

    private static final Logger logger = LoggerFactory.getLogger(ApiTest.class);

    @SuppressWarnings({"checkstyle:AbbreviationAsWordInName","checkstyle:MemberName"})
    private final boolean TEST = false;
    private List<MockDataConfig> configs;

    /**
     * It checks if CSV files respect the constrains.
     *      @see {@link org.radarcns.integrationtest.cvs.CSVValidator}
     **/
    @Before
    public void runBeforeTestMethod() throws Exception {
        if (TEST) {
            configs = MockConfig.load(getClass().getClassLoader()).getData();

            //for ( MockDataConfig config : configs ) {
            //    CSVGenerator.generate(config, 5);
            //}

            // Validate input
            for (MockDataConfig config : configs) {
                CSVValidator.validate(config);
            }
        }
    }


    /**
     * Tester. For each test case
     *
     * <p><ul>
     *  <li>compute all needed {@code Dataset}
     *  <li>call the related REST-API function using the {@code HttpClient}
     *  <li>compare the expected value with the one returned by the API
     * </ul>
     *
     * @see {@link org.radarcns.avro.restapi.dataset.Dataset}
     * @see {@link org.radarcns.integrationtest.util.URLGenerator}
     * @see {@link org.radarcns.integrationtest.util.HttpClient}
     **/
    @Test
    public void test() throws Exception {
        if (TEST) {
            HttpClient client = new HttpClient();
            Map<MockDataConfig, ExpectedValue> map = MockAggregator.getSimulations(configs);

            for (MockDataConfig config : map.keySet()) {
                ExpectedValue expected = map.get(config);

                for (MongoHelper.Stat stat : Stat.values()) {
                    String url = URLGenerator.generate(expected.getUser(), expected.getSource(),
                            RadarConverter.getDescriptiveStatistic(stat), config);
                    logger.info(url);

                    Dataset result = expected.getDataset(
                            RadarConverter.getDescriptiveStatistic(stat), config);

                    SpecificRecord response = client.doGetRequest(url);

                    assertEquals(true, ExpectedValue.compareDatasets(result, response,
                            0.0000001, false));
                }
            }
        }
    }

}