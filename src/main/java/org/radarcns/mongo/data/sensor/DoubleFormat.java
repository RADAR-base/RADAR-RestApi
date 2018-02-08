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

package org.radarcns.mongo.data.sensor;

import static org.radarcns.mongo.data.sensor.DataFormat.getQuartiles;
import static org.radarcns.mongo.util.MongoHelper.COUNT;

import java.util.List;
import org.bson.Document;
import org.radarcns.domain.managementportal.SourceData;
import org.radarcns.domain.restapi.format.Quartiles;
import org.radarcns.domain.restapi.header.DescriptiveStatistic;
import org.radarcns.domain.restapi.header.Header;
import org.radarcns.mongo.util.MongoSourceDataWrapper;
import org.radarcns.util.RadarConverter;

/**
 * Data Access Object for sensors which are represented by a single double value.
 */
public class DoubleFormat extends MongoSourceDataWrapper {

    public DoubleFormat(SourceData sourceData) {
        super(sourceData);
    }

    @Override
    public DataFormat getDataFormat() {
        return DataFormat.DOUBLE_FORMAT;
    }

    @Override
    protected Object docToAvro(Document doc, String field, DescriptiveStatistic stat,
            Header header) {
        switch (stat) {
            case MEDIAN:
                return getQuartiles(doc).get(1);
            case QUARTILES:
                List<Double> quartiles = getQuartiles(doc);
                return new Quartiles(
                        quartiles.get(0),
                        quartiles.get(1),
                        quartiles.get(2));
            case RECEIVED_MESSAGES:
                return RadarConverter.roundDouble(
                        doc.getDouble(field) / RadarConverter.getExpectedMessages(header),
                        2);
            default:
                return doc.getDouble(field);
        }
    }


    @Override
    @SuppressWarnings("unchecked")
    protected int extractCount(Document doc) {
        return ((Number) doc.get(COUNT)).intValue();
    }
}