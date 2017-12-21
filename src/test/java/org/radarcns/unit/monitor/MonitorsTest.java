package org.radarcns.unit.monitor;

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
import static org.radarcns.unit.config.TestCatalog.BIOVOTION;
import static org.radarcns.unit.config.TestCatalog.EMPATICA;

import java.io.IOException;
import java.util.HashMap;
import javax.servlet.ServletException;
import org.junit.Test;
import org.radarcns.catalogue.ProcessingState;
import org.radarcns.catalogue.Unit;
import org.radarcns.monitor.Monitors;
import org.radarcns.restapi.spec.SensorSpecification;
import org.radarcns.restapi.spec.SourceSpecification;

public class MonitorsTest {

    @Test(expected = UnsupportedOperationException.class)
    public void test() throws ServletException, IOException {
        HashMap<String, SensorSpecification> sensors =  new HashMap<>();
        sensors.put("ACCELEROMETER",
                new SensorSpecification("ACCELEROMETER", ProcessingState.RAW,
                    32.0, Unit.G));
        sensors.put("THERMOMETER",
                new SensorSpecification("THERMOMETER", ProcessingState.RAW,
                    4.0, Unit.CELSIUS));
        sensors.put("ELECTRODERMAL_ACTIVITY",
                new SensorSpecification("ELECTRODERMAL_ACTIVITY", ProcessingState.RAW,
                    4.0, Unit.MICRO_SIEMENS));
        sensors.put("INTER_BEAT_INTERVAL",
                new SensorSpecification("INTER_BEAT_INTERVAL", ProcessingState.VENDOR,
                    1.0, Unit.SECOND));
        sensors.put("BLOOD_VOLUME_PULSE",
                new SensorSpecification("BLOOD_VOLUME_PULSE", ProcessingState.RAW,
                    64.0, Unit.NANO_WATT));
        sensors.put("BATTERY",
                new SensorSpecification("BATTERY", ProcessingState.RAW,
                    1.0, Unit.PERCENTAGE));
        sensors.put("HEART_RATE",
                new SensorSpecification("HEART_RATE", ProcessingState.RADAR,
                    1.0, Unit.BEATS_PER_MIN));

        SourceSpecification spec = Monitors.getInstance().getSpecification(EMPATICA);

        for (SensorSpecification sensorSpec : spec.getSensors().values()) {
            SensorSpecification tmp = sensors.get(sensorSpec.getName());
            if (tmp.getFrequency().compareTo(sensorSpec.getFrequency()) == 0) {
                sensors.remove(sensorSpec.getName());
            }
        }

        assertEquals(true, sensors.isEmpty());

        Monitors.getInstance().getSpecification(BIOVOTION);
    }

}
