package org.radarcns.unit.util;

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
import static org.radarcns.avro.restapi.header.DescriptiveStatistic.COUNT;
import static org.radarcns.avro.restapi.sensor.SensorType.HEART_RATE;
import static org.radarcns.avro.restapi.sensor.Unit.G;
import static org.radarcns.avro.restapi.source.SourceType.EMPATICA;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.junit.Test;
import org.radarcns.avro.restapi.avro.Message;
import org.radarcns.avro.restapi.data.Acceleration;
import org.radarcns.avro.restapi.data.DoubleSample;
import org.radarcns.avro.restapi.data.Quartiles;
import org.radarcns.avro.restapi.dataset.Dataset;
import org.radarcns.avro.restapi.dataset.Item;
import org.radarcns.avro.restapi.header.EffectiveTimeFrame;
import org.radarcns.avro.restapi.header.Header;
import org.radarcns.avro.restapi.header.TimeFrame;
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
        List<JsonNode> testCases = new LinkedList<>();

        EffectiveTimeFrame etf = new EffectiveTimeFrame(
                RadarConverter.getISO8601(new Date()),
                RadarConverter.getISO8601(new Date()));
        Header header = new Header("User", "Source", EMPATICA, HEART_RATE,
                COUNT, G, TimeFrame.TEN_SECOND, etf);

        LinkedList<Item> item = new LinkedList<>();
        item.add(new Item(new DoubleSample(1.0), RadarConverter.getISO8601(new Date())));
        item.add(new Item(new DoubleSample(2.0), RadarConverter.getISO8601(new Date())));
        testCases.add(AvroConverter.avroToJsonNode(new Dataset(header, item)));

        item = new LinkedList<>();
        item.add(new Item(new Quartiles(1.0, 1.0, 1.0),
                RadarConverter.getISO8601(new Date())));
        item.add(new Item(new Quartiles(2.0, 2.0, 2.0),
                RadarConverter.getISO8601(new Date())));
        testCases.add(AvroConverter.avroToJsonNode(new Dataset(header, item)));

        item = new LinkedList<>();
        item.add(new Item(new Acceleration(1.0, 1.0, 1.0),
                RadarConverter.getISO8601(new Date())));
        item.add(new Item(new Acceleration(2.0, 2.0, 2.0),
                RadarConverter.getISO8601(new Date())));
        testCases.add(AvroConverter.avroToJsonNode(new Dataset(header, item)));

        item = new LinkedList<>();
        item.add(new Item(new Acceleration(new Quartiles(1.0, 1.0, 1.0),
                new Quartiles(1.0, 1.0, 1.0),
                new Quartiles(1.0, 1.0, 1.0)),
                RadarConverter.getISO8601(new Date())));
        item.add(new Item(new Acceleration(new Quartiles(2.0, 2.0, 2.0),
                new Quartiles(2.0, 2.0, 2.0),
                new Quartiles(2.0, 2.0, 2.0)),
                RadarConverter.getISO8601(new Date())));
        testCases.add(AvroConverter.avroToJsonNode(new Dataset(header, item)));

        for (JsonNode jsonNode : testCases) {
            Iterator<JsonNode> iterator = jsonNode.get("dataset").elements();
            while (iterator.hasNext()) {
                Iterator<String> names = iterator.next().fieldNames();

                String sample = names.next();
                String startDateTime = names.next();
                assertEquals("sample", sample);
                assertEquals("startDateTime", startDateTime);
            }
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
