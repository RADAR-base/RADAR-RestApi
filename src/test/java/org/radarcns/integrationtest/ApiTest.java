package org.radarcns.integrationtest;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;
import java.util.Stack;
import org.apache.avro.specific.SpecificRecord;
import org.junit.Before;
import org.junit.Test;
import org.radarcns.avro.restapi.avro.Message;
import org.radarcns.avro.restapi.dataset.Dataset;
import org.radarcns.avro.restapi.dataset.Item;
import org.radarcns.avro.restapi.header.DescriptiveStatistic;
import org.radarcns.avro.restapi.header.EffectiveTimeFrame;
import org.radarcns.avro.restapi.header.Header;
import org.radarcns.avro.restapi.header.Unit;
import org.radarcns.avro.restapi.sensor.HeartRate;
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

    private final boolean TEST = false;
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

                    Dataset result = expected.getDataset(
                        RadarConverter.getDescriptiveStatistic(stat), config);

                    SpecificRecord response = client.doGetRequest(url);

                    assertEquals(true, compare(result, response));
                }
            }
        }
    }

    private boolean compare(Dataset a, Object test){
        if ( !(test instanceof Dataset) ) return false;
        Dataset b = (Dataset) test;

        if ( a == null && b == null ) {
            return true;
        }
        else if ( a != null && b != null ) {
            Header ha = a.getHeader();
            Header hb = b.getHeader();

            DescriptiveStatistic dsa = ha.getDescriptiveStatistic();
            DescriptiveStatistic dsb = hb.getDescriptiveStatistic();
            if ( dsa.name().equals(dsb.name().toString()) ) {

                Unit ua = ha.getUnit();
                Unit ub = hb.getUnit();
                if ( ua.name().equals(ub.name().toString()) ) {

                    EffectiveTimeFrame etfa = ha.getEffectiveTimeFrame();
                    EffectiveTimeFrame etfb = hb.getEffectiveTimeFrame();
                    if ( etfa.getStartDateTime().equals(etfb.getStartDateTime()) &&
                        etfa.getEndDateTime().equals(etfb.getEndDateTime()) ) {

                        if ( a.getDataset().size() == b.getDataset().size() ){
                            for (int i=0; i<a.getDataset().size(); i++) {
                                Item ia = a.getDataset().get(i);
                                Item ib = b.getDataset().get(i);

                                etfa = ia.getEffectiveTimeFrame();
                                etfb = ib.getEffectiveTimeFrame();
                                if ( etfa.getStartDateTime().equals(etfb.getStartDateTime()) &&
                                    etfa.getEndDateTime().equals(etfb.getEndDateTime()) ) {

                                    HeartRate hra = (HeartRate) ia.getValue();
                                    HeartRate hrb = (HeartRate) ib.getValue();

                                    if ( hra.getValue().equals(hrb.getValue()) ) {
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

}