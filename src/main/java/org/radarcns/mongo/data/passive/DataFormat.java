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

package org.radarcns.mongo.data.passive;

import static org.radarcns.mongo.util.MongoHelper.QUARTILE;

import java.util.List;
import org.bson.Document;
import org.radarcns.domain.managementportal.SourceDataDTO;

public enum DataFormat {
    ACCELERATION_FORMAT("AccelerationFormat"),
    DOUBLE_FORMAT("DoubleFormat");

    private final String format;

    DataFormat(String format) {
        this.format = format;
    }

    /**
     * Returns the {@code SourceDataMongoWrapper} associated with the given SourceData.
     *
     * @param sourceData sourceData for which the SourceDataMongoWrapper has to be instantiated
     * @return {@code SourceDataMongoWrapper}
     * @see SourceDataMongoWrapper
     */
    public static SourceDataMongoWrapper getMongoSensor(SourceDataDTO sourceData) {
        String sourceDataType = sourceData.getSourceDataType();
        switch (sourceDataType) {
            case "ACCELEROMETER":
                return new AccelerationFormat(sourceData);
            default:
                return new DoubleFormat(sourceData);
        }
    }

    @SuppressWarnings("unchecked")
    public static List<Double> getQuartiles(Document doc) {
        return (List<Double>) doc.get(QUARTILE);
    }

    public String format() {
        return format;
    }
}
