package org.radarcns.source;

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

import java.util.Collection;
import java.util.HashMap;
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
        Map<String, SensorSpecification> sensors = new HashMap();

        for (SensorType type : specification.keySet()) {
            sensors.put(type.name(), new SensorSpecification(type,
                    specification.get(type).getDataType(),
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
