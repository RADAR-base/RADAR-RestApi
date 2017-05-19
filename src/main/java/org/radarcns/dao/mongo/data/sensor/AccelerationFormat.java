package org.radarcns.dao.mongo.data.sensor;

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

import static org.radarcns.dao.mongo.util.MongoHelper.FIRST_QUARTILE;
import static org.radarcns.dao.mongo.util.MongoHelper.SECOND_QUARTILE;
import static org.radarcns.dao.mongo.util.MongoHelper.THIRD_QUARTILE;

import java.util.ArrayList;
import org.bson.Document;
import org.radarcns.avro.restapi.data.Acceleration;
import org.radarcns.avro.restapi.data.Quartiles;
import org.radarcns.avro.restapi.header.DescriptiveStatistic;
import org.radarcns.avro.restapi.sensor.SensorType;
import org.radarcns.dao.mongo.util.MongoSensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data Access Object for Acceleration values.
 */
public class AccelerationFormat extends MongoSensor {

    public static final String X_LABEL = "x";
    public static final String Y_LABEL = "y";
    public static final String Z_LABEL = "z";

    private static final Logger LOGGER = LoggerFactory.getLogger(AccelerationFormat.class);

    public AccelerationFormat(SensorType sensorType) {
        super(DataFormat.ACCELERATION_FORMAT, sensorType);
    }

    @Override
    protected Object docToAvro(Document doc, String field, DescriptiveStatistic stat) {
        if (stat.equals(DescriptiveStatistic.MEDIAN)
                || stat.equals(DescriptiveStatistic.QUARTILES)) {
            Document component = (Document) doc.get(field);

            @SuppressWarnings("checkstyle:LocalVariableName")
            ArrayList<Document> x = (ArrayList<Document>) component.get(X_LABEL);
            @SuppressWarnings("checkstyle:LocalVariableName")
            ArrayList<Document> y = (ArrayList<Document>) component.get(Y_LABEL);
            @SuppressWarnings("checkstyle:LocalVariableName")
            ArrayList<Document> z = (ArrayList<Document>) component.get(Z_LABEL);

            if (stat.equals(DescriptiveStatistic.QUARTILES)) {
                return new Acceleration(
                    new Quartiles(
                        x.get(0).getDouble(FIRST_QUARTILE),
                        x.get(1).getDouble(SECOND_QUARTILE),
                        x.get(2).getDouble(THIRD_QUARTILE)),
                    new Quartiles(
                        y.get(0).getDouble(FIRST_QUARTILE),
                        y.get(1).getDouble(SECOND_QUARTILE),
                        y.get(2).getDouble(THIRD_QUARTILE)),
                    new Quartiles(
                        z.get(0).getDouble(FIRST_QUARTILE),
                        z.get(1).getDouble(SECOND_QUARTILE),
                        z.get(2).getDouble(THIRD_QUARTILE)));
            } else if (stat.equals(DescriptiveStatistic.MEDIAN)) {
                return new Acceleration(
                        x.get(1).getDouble(SECOND_QUARTILE),
                        y.get(1).getDouble(SECOND_QUARTILE),
                        z.get(1).getDouble(SECOND_QUARTILE));
            }

        } else {
            LOGGER.debug(doc.toJson());
            Document data = (Document) doc.get(field);
            LOGGER.debug(data.toJson());
            return new Acceleration(
                    data.getDouble(X_LABEL),
                    data.getDouble(Y_LABEL),
                    data.getDouble(Z_LABEL));
        }

        LOGGER.warn("Returning null value for the tuple: <{},{},{}>",field,stat,doc.toJson());
        return null;
    }

    @Override
    protected double extractCount(Document doc ) {
        return (doc.getDouble(X_LABEL) + doc.getDouble(Y_LABEL) + doc.getDouble(Z_LABEL)) / 3.0d;
    }
}