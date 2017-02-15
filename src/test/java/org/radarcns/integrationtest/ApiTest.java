package org.radarcns.integrationtest;

import org.junit.Test;
import org.radarcns.integrationtest.collector.ExpectedArrayValue;
import org.radarcns.integrationtest.collector.ExpectedDoubleValue;
import org.radarcns.integrationtest.config.MockConfig;
import org.radarcns.integrationtest.config.MockDataConfig;
import org.radarcns.integrationtest.util.MockAggregator;
import org.radarcns.integrationtest.util.Parser;
import org.radarcns.integrationtest.util.Parser.ExpectedType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Francesco Nobilia on 10/01/2017.
 */
public class ApiTest {

    private static final Logger logger = LoggerFactory.getLogger(ApiTest.class);

    private final boolean TEST = false;

    @Test
    public void callTest() throws Exception {
        if( TEST ) {
            MockDataConfig config = MockConfig.load(getClass().getClassLoader()).getData().get(1);
            Parser parser =  new Parser(config);

            if ( parser.getExpecedType().equals(ExpectedType.DOUBLE) ){
                ExpectedDoubleValue edv = MockAggregator.simulateSingletonCollector(parser);
                logger.info(edv.toString());
            }
            else if ( parser.getExpecedType().equals(ExpectedType.ARRAY) ){
                ExpectedArrayValue eav = MockAggregator.simulateArrayCollector(parser);
                logger.info(eav.toString());
            }
        }
    }

}



/*
HttpClient client = new DefaultHttpClient();
HttpGet get = new HttpGet("localhost:8080/api/avro/checkPatientDevices/user");
HttpResponse response = client.execute(get);
byte[] content = EntityUtils.toByteArray(response.getEntity());
InputStream is = new ByteArrayInputStream(content);
DatumReader<T> reader = new SpecificDatumReader<>(Statistic.class);
Decoder decoder = DecoderFactory.get().binaryDecoder(is, null);
Statistic fromServer = reader.read(null, decoder);
*/