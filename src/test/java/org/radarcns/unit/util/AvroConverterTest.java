package org.radarcns.unit.util;

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
import static org.radarcns.avro.restapi.header.DescriptiveStatistic.COUNT;
import static org.radarcns.avro.restapi.sensor.Unit.G;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import org.junit.Test;
import org.radarcns.avro.restapi.avro.Message;
import org.radarcns.avro.restapi.dataset.Dataset;
import org.radarcns.avro.restapi.dataset.Item;
import org.radarcns.avro.restapi.header.EffectiveTimeFrame;
import org.radarcns.avro.restapi.header.Header;
import org.radarcns.avro.restapi.sensor.HeartRate;
import org.radarcns.util.AvroConverter;
import org.radarcns.util.RadarConverter;

public class AvroConverterTest {

    @Test
    public void avroToJsonTest() {
        Message mex = new Message("Text message");
        JsonNode jsonNode = AvroConverter.avroToJsonNode(mex);
        assertEquals(mex.toString().replaceAll(": ", ":"), jsonNode.toString());
    }

    @Test
    public void avroToJsonNodeTest() throws IOException {
        EffectiveTimeFrame etf = new EffectiveTimeFrame(
            RadarConverter.getISO8601(new Date()),
            RadarConverter.getISO8601(new Date()));
        Header header = new Header(COUNT, G, etf);

        LinkedList<Item> item = new LinkedList<>();
        item.add(new Item(new HeartRate(1.0), etf));
        item.add(new Item(new HeartRate(2.0), etf));

        Dataset dataset = new Dataset(header, item);

        JsonNode jsonNode = AvroConverter.avroToJsonNode(dataset, "heart_rate");

        Iterator<JsonNode> iterator = jsonNode.get("dataset").elements();
        while (iterator.hasNext()) {
            Iterator<String> names = iterator.next().fieldNames();
            String eftString = names.next();
            String sensor = names.next();
            assertEquals("effectiveTimeFrame", eftString);
            assertEquals("heart_rate", sensor);
        }
    }

    @Test
    public void avroBytes() throws IOException {
        Message mex = new Message("Test");
        byte[] bytes = AvroConverter.avroToAvroByte(mex);
        Message test = AvroConverter.avroByteToAvro(bytes, Message.getClassSchema());
        assertEquals(mex.getMessage(), test.getMessage());
    }

}
