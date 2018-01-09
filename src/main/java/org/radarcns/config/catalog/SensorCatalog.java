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

package org.radarcns.config.catalog;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import org.radarcns.catalogue.ProcessingState;
import org.radarcns.catalogue.Unit;
import org.radarcns.dao.mongo.data.sensor.DataFormat;

/**
 * Sensor specification. It is used to define a sensor providing all available information.
 */
public class SensorCatalog {

    /** Sensor name. **/
    private String name;

    /** Sample rate measured in Hz: number of samples generated within a second. **/
    private Double frequency;

    /**
     * Measurement {@link Unit}.
     **/
    private Unit unit;

    /** Explains if the sample is a RAW data or it is the result of a computation. In the second
     *      case, it reports who has computed the value.
     **/
    @JsonProperty("data_type")
    private ProcessingState dataType;

    /**
     * Defines at which {@code Data Class} the sensor belongs to. A {@code Data Class} is a
     *      generalised value within the RADAR Platform. For instance, all data coming from sensors
     *      generating a single {@code Double} is generalised using
     *      {@link DataFormat#DOUBLE_FORMAT}.
     * @see DataFormat
     */
    @JsonProperty("data_class")
    private DataFormat dataFormat;

    private HashMap<String,String> collections;

    public SensorCatalog() {
        // POJO initializer
    }

    /** Constructor. **/
    public SensorCatalog(String name, Double frequency, Unit unit,
            ProcessingState dataType, DataFormat dataFormat, HashMap<String, String> collections) {
        this.name = name;
        this.frequency = frequency;
        this.unit = unit;
        this.dataType = dataType;
        this.dataFormat = dataFormat;
        this.collections = collections;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getFrequency() {
        return frequency;
    }

    public void setFrequency(Double frequency) {
        this.frequency = frequency;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public ProcessingState getDataType() {
        return dataType;
    }

    public void setDataType(ProcessingState dataType) {
        this.dataType = dataType;
    }

    public DataFormat getDataFormat() {
        return dataFormat;
    }

    public void setDataFormat(DataFormat dataFormat) {
        this.dataFormat = dataFormat;
    }

    public HashMap<String, String> getCollections() {
        return collections;
    }

    public void setCollections(HashMap<String, String> collections) {
        this.collections = collections;
    }

    @Override
    public String toString() {
        return "Sensor{"
            + "name='" + name + '\''
            + ", frequency=" + frequency
            + ", unit=" + unit
            + ", dataType=" + dataType
            + ", dataClass='" + dataFormat + '\''
            + ", collections=" + collections
            + '}';
    }
}
