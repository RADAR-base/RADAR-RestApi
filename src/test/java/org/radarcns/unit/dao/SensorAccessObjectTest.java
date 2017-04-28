package org.radarcns.unit.dao;

/*
 *  Copyright 2016 King's College London and The Hyve
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
import java.util.List;
import org.junit.Test;
import org.radarcns.avro.restapi.avro.Message;
import org.radarcns.avro.restapi.data.Acceleration;
import org.radarcns.avro.restapi.data.DoubleValue;
import org.radarcns.avro.restapi.data.Quartiles;
import org.radarcns.avro.restapi.dataset.Dataset;
import org.radarcns.avro.restapi.dataset.Item;
import org.radarcns.avro.restapi.header.EffectiveTimeFrame;
import org.radarcns.avro.restapi.header.Header;
import org.radarcns.avro.restapi.header.TimeFrame;
import org.radarcns.dao.SensorDataAccessObject;
import org.radarcns.util.AvroConverter;
import org.radarcns.util.RadarConverter;

public class SensorAccessObjectTest {

    @Test
    public void instanceAllocationTest() {
        assertEquals(7, SensorDataAccessObject.getInstance().getSupportedSensor().size());
    }
}
