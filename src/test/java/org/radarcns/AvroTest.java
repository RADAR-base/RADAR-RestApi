package org.radarcns;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.LinkedList;
import java.util.Random;
import org.junit.Test;
import org.radarcns.avro.restapi.avro.Message;
import org.radarcns.avro.restapi.dataset.Dataset;
import org.radarcns.avro.restapi.dataset.Item;
import org.radarcns.avro.restapi.header.DescriptiveStatistic;
import org.radarcns.avro.restapi.header.EffectiveTimeFrame;
import org.radarcns.avro.restapi.header.Header;
import org.radarcns.avro.restapi.header.Unit;
import org.radarcns.avro.restapi.sensor.HeartRate;
import org.radarcns.integrationtest.collector.ExpectedValue;
import org.radarcns.util.AvroConverter;
import org.radarcns.util.RadarConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Francesco Nobilia on 10/01/2017.
 */
public class AvroTest {

    private static final Logger logger = LoggerFactory.getLogger(AvroTest.class);

    private final boolean TEST = false;

    @Test
    public void testMessage() throws Exception {
        if( TEST ) {
            Message mex = new Message("Test");
            byte[] bytes = AvroConverter.avroToAvroByte(mex);
            Message test = AvroConverter.avroByteToAvro(bytes, Message.getClassSchema());
            assertEquals(mex.getMessage(), test.getMessage());
        }
    }

    @Test
    public void testDataset() throws Exception {
        if( TEST ) {
            Dataset dataset =  getRandomDataset();
            byte[] bytes = AvroConverter.avroToAvroByte(dataset);
            Dataset test = AvroConverter.avroByteToAvro(bytes, Dataset.getClassSchema());

            assertEquals(true, ExpectedValue.compareDatasets(dataset, test));
            assertEquals(false, ExpectedValue.compareDatasets(getRandomDataset(), test));
        }
    }

    private Dataset getRandomDataset(){
        Random random = new Random();

        Date start = new Date();

        LinkedList<Item> list = new LinkedList<>();

        Date startEvent = new Date();
        Date endEvent = new Date();

        startEvent.setTime(start.getTime());
        endEvent.setTime(startEvent.getTime() + 10000);
        EffectiveTimeFrame etf = new EffectiveTimeFrame(RadarConverter.getISO8601(startEvent), RadarConverter.getISO8601(endEvent));
        Item item = new Item(new HeartRate(new Double(random.nextDouble())),etf);
        list.addLast(item);

        startEvent.setTime(endEvent.getTime());
        endEvent.setTime(startEvent.getTime() + 10000);
        etf = new EffectiveTimeFrame(RadarConverter.getISO8601(start), RadarConverter.getISO8601(endEvent));
        item = new Item(new HeartRate(new Double(random.nextDouble())),etf);
        list.addLast(item);

        etf = new EffectiveTimeFrame(RadarConverter.getISO8601(start),RadarConverter.getISO8601(endEvent));
        Header header = new Header(DescriptiveStatistic.count, Unit.dimensionless,etf);

        Dataset hrd = new Dataset(header,list);

        return hrd;
    }
}
