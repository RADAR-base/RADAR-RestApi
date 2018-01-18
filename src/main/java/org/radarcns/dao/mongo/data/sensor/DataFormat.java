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
import org.radarcns.source.SourceCatalog;

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
     * @param sensorType sensor for which the MongoSensor has to be instantiated
     * @return {@code MongoSensor}
     *
     * @see MongoSensor
     */
    public static MongoSensor getMongoSensor(String sensorType) {
        DataFormat dataFormat = SourceCatalog.getInstance().getFormat(sensorType);
        switch (dataFormat) {
            case ACCELERATION_FORMAT: return new AccelerationFormat(sensorType);
            case DOUBLE_FORMAT: return new DoubleFormat(sensorType);
            default: throw new IllegalArgumentException(dataFormat.format + ": unknown DataFormat");
        }
    }

    @SuppressWarnings("unchecked")
    public static List<Double> getQuartiles(Document doc) {
        return (List<Double>)doc.get(QUARTILE);
    }
}
