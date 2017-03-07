package org.radarcns.unit.monitor;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.HashMap;
import javax.servlet.ServletException;
import org.junit.Test;
import org.radarcns.avro.restapi.sensor.SensorSpecification;
import org.radarcns.avro.restapi.sensor.SensorType;
import org.radarcns.avro.restapi.sensor.Unit;
import org.radarcns.avro.restapi.source.SourceSpecification;
import org.radarcns.avro.restapi.source.SourceType;
import org.radarcns.monitor.Monitors;

/**
 * Created by francesco on 05/03/2017.
 */
public class MonitorsTest {

    @Test(expected = UnsupportedOperationException.class)
    public void test() throws ServletException, IOException {
        SourceSpecification spec = Monitors.getInstance().getSpecification(SourceType.EMPATICA);

        HashMap<SensorType, SensorSpecification> sensors =  new HashMap<>();
        sensors.put(SensorType.ACC,
                new SensorSpecification(SensorType.ACC, 32.0, Unit.G));
        sensors.put(SensorType.TEMP,
                new SensorSpecification(SensorType.TEMP, 4.0, Unit.CELSIUS));
        sensors.put(SensorType.EDA,
                new SensorSpecification(SensorType.EDA, 4.0, Unit.MICROSIEMENS));
        sensors.put(SensorType.IBI,
                new SensorSpecification(SensorType.IBI, 1.0, Unit.SEC));
        sensors.put(SensorType.BVP,
                new SensorSpecification(SensorType.BVP, 64.0, Unit.NW));
        sensors.put(SensorType.BAT,
                new SensorSpecification(SensorType.BAT, 1.0, Unit.PERCENTAGE));
        sensors.put(SensorType.HR,
                new SensorSpecification(SensorType.HR, 1.0, Unit.HZ));

        for (SensorSpecification sensorSpec : spec.getSensors()) {
            SensorSpecification tmp = sensors.get(sensorSpec.getName());
            if (tmp.getFrequency().compareTo(sensorSpec.getFrequency()) == 0) {
                sensors.remove(sensorSpec.getName());
            }
        }

        assertEquals(true, sensors.isEmpty());

        Monitors.getInstance().getSpecification(SourceType.BIOVOTION);
    }

}
