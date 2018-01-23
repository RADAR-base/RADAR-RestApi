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

package org.radarcns.dao.mongo.data.sensor;

import static org.radarcns.dao.mongo.util.MongoHelper.QUARTILE;

import java.util.List;
import org.bson.Document;
import org.radarcns.dao.mongo.util.MongoSensor;
import org.radarcns.managementportal.SourceData;

public enum DataFormat {
    ACCELERATION_FORMAT("AccelerationFormat"),
    DOUBLE_FORMAT("DoubleFormat");

    private String format;

    DataFormat(String format) {
        this.format = format;
    }

    public String format() {
        return format;
    }

    /**
     * Returns the {@code MongoSensor} associated with the given sensor.
     * @param sourceData sensor for which the MongoSensor has to be instantiated
     * @return {@code MongoSensor}
     *
     * @see MongoSensor
     */
    public static MongoSensor getMongoSensor(SourceData sourceData) {
        String sourceDataType = sourceData.getSourceDataType();
        switch (sourceDataType) {
            case "ACCELEROMETER": return new AccelerationFormat(sourceDataType);
            default: return new DoubleFormat(sourceDataType);
        }
    }

    @SuppressWarnings("unchecked")
    public static List<Double> getQuartiles(Document doc) {
        return (List<Double>)doc.get(QUARTILE);
    }
}
