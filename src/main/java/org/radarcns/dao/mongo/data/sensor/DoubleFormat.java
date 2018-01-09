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

import static org.radarcns.dao.mongo.util.MongoHelper.FIRST_QUARTILE;
import static org.radarcns.dao.mongo.util.MongoHelper.SECOND_QUARTILE;
import static org.radarcns.dao.mongo.util.MongoHelper.THIRD_QUARTILE;

import java.util.ArrayList;
import org.bson.Document;
import org.radarcns.dao.mongo.util.MongoSensor;
import org.radarcns.restapi.data.DoubleSample;
import org.radarcns.restapi.data.Quartiles;
import org.radarcns.restapi.header.DescriptiveStatistic;
import org.radarcns.restapi.header.Header;
import org.radarcns.util.RadarConverter;

/**
 * Data Access Object for sensors which are represented by a single double value.
 */
public class DoubleFormat extends MongoSensor {

    //private static final Logger LOGGER = LoggerFactory.getLogger(DoubleFormat.class);

    public DoubleFormat(String sensorType) {
        super(DataFormat.DOUBLE_FORMAT, sensorType);
    }

    @Override
    protected Object docToAvro(Document doc, String field, DescriptiveStatistic stat,
            Header header) {
        switch (stat) {
            case MEDIAN:
                return new DoubleSample(((ArrayList<Document>) doc.get(field)).get(1).getDouble(
                        SECOND_QUARTILE));
            case QUARTILES:
                ArrayList<Document> quartilesList = (ArrayList<Document>) doc.get(field);
                return new DoubleSample( new Quartiles(
                    quartilesList.get(0).getDouble(FIRST_QUARTILE),
                    quartilesList.get(1).getDouble(SECOND_QUARTILE),
                    quartilesList.get(2).getDouble(THIRD_QUARTILE)));
            case RECEIVED_MESSAGES:
                return new DoubleSample(RadarConverter.roundDouble(
                        doc.getDouble(field) / RadarConverter.getExpectedMessages(header),
                    2));
            default: return new DoubleSample(doc.getDouble(field));
        }
    }

}