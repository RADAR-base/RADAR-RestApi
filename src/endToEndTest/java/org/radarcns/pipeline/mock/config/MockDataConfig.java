package org.radarcns.pipeline.mock.config;

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

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.File;
import org.radarcns.avro.restapi.sensor.SensorType;
import org.radarcns.pipeline.config.Config;

public class MockDataConfig extends org.radarcns.mock.MockDataConfig {
    private String sensor;
    @JsonProperty("values_to_test")
    private String valuesToTest;
    private Double frequency;
    private Long magnitude;

    public String getSensor() {
        return sensor;
    }

    public void setSensor(String sensor) {
        this.sensor = sensor;
    }

    /**
     * Converts sensor value string to SensorType.
     *
     * @throws IllegalArgumentException if the specified sensor does not match any of the
     *      already known ones
     */
    public SensorType getSensorType() {
        for (SensorType type : SensorType.values()) {
            if (type.name().equalsIgnoreCase(sensor)) {
                return type;
            }
        }

        throw new IllegalArgumentException(sensor + " unknown sensor");
    }

    public String getValuesToTest() {
        return valuesToTest;
    }

    public void setValuesToTest(String valuesToTest) {
        this.valuesToTest = valuesToTest;
    }

    public Double getFrequency() {
        return frequency;
    }

    public void setFrequency(Double frequency) {
        this.frequency = frequency;
    }

    public File getCvsFile() {
        return super.getDataFile(Config.getBaseFile());
    }

    public Long getMagnitude() {
        return magnitude;
    }

    public void setMagnitude(Long magnitude) {
        this.magnitude = magnitude;
    }
}
