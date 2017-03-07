package org.radarcns.source;

import java.util.HashMap;
import java.util.Map;
import org.radarcns.avro.restapi.sensor.DataType;
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

        frequencies.put(SensorType.ACCELEROMETER,
                new SensorSpecification(SensorType.ACCELEROMETER, DataType.RAW, 32.0,
                        Unit.G));
        frequencies.put(SensorType.BATTERY,
                new SensorSpecification(SensorType.BATTERY, DataType.RAW, 1.0,
                        Unit.PERCENTAGE));
        frequencies.put(SensorType.BLOOD_VOLUME_PULSE,
                new SensorSpecification(SensorType.BLOOD_VOLUME_PULSE, DataType.RAW,
                        64.0, Unit.NANOWATT));
        frequencies.put(SensorType.ELECTRODERMAL_ACTIVITY,
                new SensorSpecification(SensorType.ELECTRODERMAL_ACTIVITY, DataType.RAW,
                        4.0, Unit.MICROSIEMENS));
        frequencies.put(SensorType.HEART_RATE,
                new SensorSpecification(SensorType.HEART_RATE, DataType.RADAR, 1.0,
                        Unit.BEATS_PER_MIN));
        frequencies.put(SensorType.INTER_BEAT_INTERVAL,
                new SensorSpecification(SensorType.INTER_BEAT_INTERVAL, DataType.VENDOR,
                        1.0, Unit.SECOND));
        frequencies.put(SensorType.THERMOMETER,
                new SensorSpecification(SensorType.THERMOMETER, DataType.RAW,
                        4.0, Unit.CELSIUS));

        return frequencies;
    }

    @Override
    protected SourceType setType() {
        return SourceType.EMPATICA;
    }
}
