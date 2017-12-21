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
import static org.radarcns.restapi.header.DescriptiveStatistic.MEDIAN;
import static org.radarcns.restapi.header.DescriptiveStatistic.QUARTILES;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.radarcns.dao.mongo.util.MongoSensor;
import org.radarcns.restapi.data.Acceleration;
import org.radarcns.restapi.data.Quartiles;
import org.radarcns.restapi.header.DescriptiveStatistic;
import org.radarcns.restapi.header.Header;
import org.radarcns.util.RadarConverter;

/**
 * Data Access Object for Acceleration values.
 */
public class AccelerationFormat extends MongoSensor {

    public static final String X_LABEL = "x";
    public static final String Y_LABEL = "y";
    public static final String Z_LABEL = "z";

    //private static final Logger LOGGER = LoggerFactory.getLogger(AccelerationFormat.class);

    public AccelerationFormat(String sensorType) {
        super(DataFormat.ACCELERATION_FORMAT, sensorType);
    }

    @Override
    protected Object docToAvro(Document doc, String field, DescriptiveStatistic stat,
            Header header) {
        Document component = (Document) doc.get(field);

        @SuppressWarnings("checkstyle:LocalVariableName")
        ArrayList<Document> x = null;
        @SuppressWarnings("checkstyle:LocalVariableName")
        ArrayList<Document> y = null;
        @SuppressWarnings("checkstyle:LocalVariableName")
        ArrayList<Document> z = null;

        Document data = null;

        if (stat.equals(MEDIAN) || stat.equals(QUARTILES)) {
            x = (ArrayList<Document>) component.get(X_LABEL);
            y = (ArrayList<Document>) component.get(Y_LABEL);
            z = (ArrayList<Document>) component.get(Z_LABEL);
        } else {
            data = (Document) doc.get(field);
        }

        switch (stat) {
            case MEDIAN: return new Acceleration(
                    x.get(1).getDouble(SECOND_QUARTILE),
                    y.get(1).getDouble(SECOND_QUARTILE),
                    z.get(1).getDouble(SECOND_QUARTILE));
            case QUARTILES: return new Acceleration(
                    new Quartiles(
                        x.get(0).getDouble(FIRST_QUARTILE),
                        x.get(1).getDouble(FIRST_QUARTILE),
                        x.get(2).getDouble(FIRST_QUARTILE)),
                    new Quartiles(
                        y.get(0).getDouble(FIRST_QUARTILE),
                        y.get(1).getDouble(SECOND_QUARTILE),
                        y.get(2).getDouble(THIRD_QUARTILE)),
                    new Quartiles(
                        z.get(0).getDouble(FIRST_QUARTILE),
                        z.get(1).getDouble(SECOND_QUARTILE),
                        z.get(2).getDouble(THIRD_QUARTILE)));
            case RECEIVED_MESSAGES:
                return new Acceleration(
                    RadarConverter.roundDouble(data.getDouble(X_LABEL)
                            / RadarConverter.getExpectedMessages(header), 2),
                    RadarConverter.roundDouble(data.getDouble(Y_LABEL)
                            / RadarConverter.getExpectedMessages(header), 2),
                    RadarConverter.roundDouble(data.getDouble(Z_LABEL)
                            / RadarConverter.getExpectedMessages(header), 2)
                );
            default:
                return new Acceleration(
                    data.getDouble(X_LABEL),
                    data.getDouble(Y_LABEL),
                    data.getDouble(Z_LABEL));
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected int extractCount(Object value) {
        List<Document> docs = (List<Document>) value;
        return (intProperty(docs.get(0), X_LABEL)
                + intProperty(docs.get(1), Y_LABEL)
                + intProperty(docs.get(2), Z_LABEL)) / 3;
    }

    private static int intProperty(Document doc, String key) {
        return ((Number)doc.get(key)).intValue();
    }
}