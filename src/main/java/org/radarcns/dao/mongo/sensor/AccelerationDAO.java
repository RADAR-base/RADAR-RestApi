package org.radarcns.dao.mongo.sensor;

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

import java.util.ArrayList;
import org.bson.Document;
import org.radarcns.avro.restapi.dataset.Quartiles;
import org.radarcns.avro.restapi.header.DescriptiveStatistic;
import org.radarcns.avro.restapi.sensor.Acceleration;
import org.radarcns.dao.mongo.util.MongoSensorDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data Access Object for Acceleration values.
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public class AccelerationDAO extends MongoSensorDAO {

    private final Logger logger = LoggerFactory.getLogger(AccelerationDAO.class);

    private static final AccelerationDAO instance = new AccelerationDAO();

    public static AccelerationDAO getInstance() {
        return instance;
    }

    private AccelerationDAO() {
    }

    @Override
    protected Object docToAvro(Document doc, String field, DescriptiveStatistic stat) {
        if (stat.equals(DescriptiveStatistic.MEDIAN)
                || stat.equals(DescriptiveStatistic.QUARTILES)) {
            Document component = (Document) doc.get(field);

            @SuppressWarnings("checkstyle:LocalVariableName")
            ArrayList<Document> x = (ArrayList<Document>) component.get("x");
            @SuppressWarnings("checkstyle:LocalVariableName")
            ArrayList<Document> y = (ArrayList<Document>) component.get("y");
            @SuppressWarnings("checkstyle:LocalVariableName")
            ArrayList<Document> z = (ArrayList<Document>) component.get("z");

            if (stat.equals(DescriptiveStatistic.QUARTILES)) {
                return new Acceleration(
                    new Quartiles(
                        x.get(0).getDouble("25"),
                        x.get(1).getDouble("50"),
                        x.get(2).getDouble("75")),
                    new Quartiles(
                        y.get(0).getDouble("25"),
                        y.get(1).getDouble("50"),
                        y.get(2).getDouble("75")),
                    new Quartiles(
                        z.get(0).getDouble("25"),
                        z.get(1).getDouble("50"),
                        z.get(2).getDouble("75")));
            } else if (stat.equals(DescriptiveStatistic.MEDIAN)) {
                return new Acceleration(
                        x.get(1).getDouble("50"),
                        y.get(1).getDouble("50"),
                        z.get(1).getDouble("50"));
            }

        } else {
            logger.debug(doc.toJson());
            Document data = (Document) doc.get(field);
            logger.debug(data.toJson());
            return new Acceleration(
                    data.getDouble("x"),
                    data.getDouble("y"),
                    data.getDouble("z"));
        }

        logger.warn("Returning null value for the tuple: <{},{},{}>",field,stat,doc.toJson());
        return null;
    }

    @Override
    protected double extractCount(Document doc ) {
        return (doc.getDouble("x") + doc.getDouble("y") + doc.getDouble("z")) / 3.0d;
    }

    @Override
    public String getEmpaticaCollection() {
        return "android_empatica_e4_acceleration_output";
    }
}