package org.radarcns.source;

import java.util.HashMap;
import java.util.Map;
import org.radarcns.avro.restapi.sensor.SensorSpecification;
import org.radarcns.avro.restapi.sensor.SensorType;
import org.radarcns.avro.restapi.sensor.Unit;
import org.radarcns.avro.restapi.source.SourceType;

/**
 * Empatica status monitor.
 */
public class Empatica extends SourceDefinition {

    @Override
    protected Map<SensorType, SensorSpecification> setSpecification() {
        Map<SensorType, SensorSpecification> frequencies = new HashMap<>();

        frequencies.put(SensorType.ACC,
                new SensorSpecification(SensorType.ACC, 32.0, Unit.G));
        frequencies.put(SensorType.BAT,
            new SensorSpecification(SensorType.BAT, 1.0, Unit.PERCENTAGE));
        frequencies.put(SensorType.BVP,
            new SensorSpecification(SensorType.BVP, 64.0, Unit.NW));
        frequencies.put(SensorType.EDA,
            new SensorSpecification(SensorType.EDA, 4.0, Unit.MICROSIEMENS));
        frequencies.put(SensorType.HR,
            new SensorSpecification(SensorType.HR, 1.0, Unit.HZ));
        frequencies.put(SensorType.IBI,
            new SensorSpecification(SensorType.IBI, 1.0, Unit.SEC));
        frequencies.put(SensorType.TEMP,
            new SensorSpecification(SensorType.TEMP, 4.0, Unit.CELSIUS));

        return frequencies;
    }

    @Override
    protected SourceType setType() {
        return SourceType.EMPATICA;
    }
}
