package org.radarcns.dao.mongo.data.sensor;

/*
 *  Copyright 2016 King's College London and The Hyve
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

import java.util.ArrayList;
import org.bson.Document;
import org.radarcns.avro.restapi.data.DoubleValue;
import org.radarcns.avro.restapi.data.Quartiles;
import org.radarcns.avro.restapi.header.DescriptiveStatistic;
import org.radarcns.avro.restapi.sensor.SensorType;
import org.radarcns.dao.mongo.util.MongoSensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data Access Object for sensors which are represented by a single double value.
 */
public class DoubleFormat extends MongoSensor {

    private static final Logger LOGGER = LoggerFactory.getLogger(DoubleFormat.class);

    public DoubleFormat(SensorType sensorType) {
        super(DataFormat.DOUBLE_FORMAT, sensorType);
    }

    @Override
    protected Object docToAvro(Document doc, String field, DescriptiveStatistic stat) {
        if (stat.equals(DescriptiveStatistic.MEDIAN)
                || stat.equals(DescriptiveStatistic.QUARTILES)) {

            ArrayList<Document> quartilesList = (ArrayList<Document>) doc.get(field);

            if (stat.equals(DescriptiveStatistic.QUARTILES)) {
                return new DoubleValue( new Quartiles(
                        quartilesList.get(0).getDouble("25"),
                        quartilesList.get(1).getDouble("50"),
                        quartilesList.get(2).getDouble("75")));
            } else if (stat.equals(DescriptiveStatistic.MEDIAN)) {
                return new DoubleValue(quartilesList.get(1).getDouble("50"));
            }

        } else {
            return new DoubleValue(doc.getDouble(field));
        }

        LOGGER.warn("Returning null value for the tuple: <{},{},{}>",field,stat,doc.toJson());
        return null;
    }

}