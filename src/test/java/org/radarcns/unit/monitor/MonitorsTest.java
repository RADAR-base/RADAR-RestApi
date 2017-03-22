package org.radarcns.unit.monitor;

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
import java.util.HashMap;
import javax.servlet.ServletException;
import org.junit.Test;
import org.radarcns.avro.restapi.sensor.DataType;
import org.radarcns.avro.restapi.sensor.SensorSpecification;
import org.radarcns.avro.restapi.sensor.SensorType;
import org.radarcns.avro.restapi.sensor.Unit;
import org.radarcns.avro.restapi.source.SourceSpecification;
import org.radarcns.avro.restapi.source.SourceType;
import org.radarcns.monitor.Monitors;

public class MonitorsTest {

    @Test(expected = UnsupportedOperationException.class)
    public void test() throws ServletException, IOException {
        SourceSpecification spec = Monitors.getInstance().getSpecification(SourceType.EMPATICA);

        HashMap<SensorType, SensorSpecification> sensors =  new HashMap<>();
        sensors.put(SensorType.ACCELEROMETER,
                new SensorSpecification(SensorType.ACCELEROMETER, DataType.RAW,
                    32.0, Unit.G));
        sensors.put(SensorType.THERMOMETER,
                new SensorSpecification(SensorType.THERMOMETER, DataType.RAW,
                    4.0, Unit.CELSIUS));
        sensors.put(SensorType.ELECTRODERMAL_ACTIVITY,
                new SensorSpecification(SensorType.ELECTRODERMAL_ACTIVITY, DataType.RAW,
                    4.0, Unit.MICROSIEMENS));
        sensors.put(SensorType.INTER_BEAT_INTERVAL,
                new SensorSpecification(SensorType.INTER_BEAT_INTERVAL, DataType.VENDOR,
                    1.0, Unit.SECOND));
        sensors.put(SensorType.BLOOD_VOLUME_PULSE,
                new SensorSpecification(SensorType.BLOOD_VOLUME_PULSE, DataType.RAW,
                    64.0, Unit.NANOWATT));
        sensors.put(SensorType.BATTERY,
                new SensorSpecification(SensorType.BATTERY, DataType.RAW,
                    1.0, Unit.PERCENTAGE));
        sensors.put(SensorType.HEART_RATE,
                new SensorSpecification(SensorType.HEART_RATE, DataType.RADAR,
                    1.0, Unit.BEATS_PER_MIN));

        for (SensorSpecification sensorSpec : spec.getSensors().values()) {
            SensorSpecification tmp = sensors.get(sensorSpec.getName());
            if (tmp.getFrequency().compareTo(sensorSpec.getFrequency()) == 0) {
                sensors.remove(sensorSpec.getName());
            }
        }

        assertEquals(true, sensors.isEmpty());

        Monitors.getInstance().getSpecification(SourceType.BIOVOTION);
    }

}
