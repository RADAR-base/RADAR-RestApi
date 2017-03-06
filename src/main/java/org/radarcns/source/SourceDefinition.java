package org.radarcns.source;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.radarcns.avro.restapi.sensor.SensorSpecification;
import org.radarcns.avro.restapi.sensor.SensorType;
import org.radarcns.avro.restapi.sensor.Unit;
import org.radarcns.avro.restapi.source.SourceSpecification;
import org.radarcns.avro.restapi.source.SourceType;

/**
 * Generic Source Definition.
 */
public abstract class SourceDefinition {

    //private static final Logger logger = LoggerFactory.getLogger(SourceDefinition.class);

    private final Map<SensorType, SensorSpecification> specification;
    private final SourceType sourceType;

    /**
     * Constructor.
     **/
    public SourceDefinition() {
        this.specification = setSpecification();
        this.sourceType = setType();
    }

    /**
     * Returns Source Specification.
     * @return the source specification that is used to compute the state
     * @implSpec this function must be override by the subclass.
     */
    protected abstract Map<SensorType, SensorSpecification> setSpecification();

    /**
     * Sets the instance's source type.
     * @return the source type taken into account by the monitor
     * @implSpec this function must be override by the subclass.
     */
    protected abstract SourceType setType();

    /**
     * Returns the SourceDefinition Specification used by the monitor associated with the monitor.
     *
     * @return {@code SourceSpecification} containing all sensor names and related frequencies
     * @see {@link SensorSpecification}
     * @see {@link SourceSpecification}
     */
    public SourceSpecification getSpecification() {
        List<SensorSpecification> sensors = new LinkedList<>();

        for (SensorType type : specification.keySet()) {
            sensors.add(new SensorSpecification(type,
                    specification.get(type).getFrequency(),
                    specification.get(type).getUnit()));
        }

        return new SourceSpecification(sourceType, sensors);
    }

    public SourceType getType() {
        return sourceType;
    }

    /**
     * Returns all on board Sensor Type.
     *
     * @return {@code Collection<SensorType>} for the given source
     */
    public Collection<SensorType> getSensorTypes() {
        return specification.keySet();
    }

    /**
     * Returns the Unit associated with the source.
     *
     * @return {@code Unit} for the given source
     */
    public Unit getMeasurementUnit(SensorType sensor) {
        return specification.get(sensor).getUnit();
    }

    /**
     * Returns the frequency associated with the sensor.
     *
     * @return {@code Double} stating the sensor frequency
     */
    public Double getFrequency(SensorType sensor) {
        return specification.get(sensor).getFrequency();
    }
}
