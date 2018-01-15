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

import static org.radarcns.dao.mongo.data.sensor.DataFormat.getQuartiles;
import static org.radarcns.dao.mongo.util.MongoHelper.COUNT;
import static org.radarcns.dao.mongo.util.MongoHelper.FIELDS;
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
        @SuppressWarnings("unchecked")
        List<Document> fields = (List<Document>) doc.get(FIELDS);

        Document x = fields.get(0);
        Document y = fields.get(1);
        Document z = fields.get(2);

        switch (stat) {
            case MEDIAN: return new Acceleration(
                    getQuartiles(x).get(1),
                    getQuartiles(y).get(1),
                    getQuartiles(z).get(1));
            case QUARTILES:
                List<Double> xQuartile = getQuartiles(x);
                List<Double> yQuartile = getQuartiles(y);
                List<Double> zQuartile = getQuartiles(z);

                return new Acceleration(
                        new Quartiles(xQuartile.get(0), xQuartile.get(1), xQuartile.get(2)),
                        new Quartiles(yQuartile.get(0), yQuartile.get(1), yQuartile.get(2)),
                        new Quartiles(zQuartile.get(0), zQuartile.get(1), zQuartile.get(2)));
            case RECEIVED_MESSAGES:
                return new Acceleration(
                    RadarConverter.roundDouble(
                            x.getDouble(field) / RadarConverter.getExpectedMessages(header), 2),
                    RadarConverter.roundDouble(
                            y.getDouble(field) / RadarConverter.getExpectedMessages(header), 2),
                    RadarConverter.roundDouble(
                            z.getDouble(field) / RadarConverter.getExpectedMessages(header), 2));
            default:
                return new Acceleration(
                    x.get(field),
                    y.get(field),
                    z.get(field));
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected int extractCount(Document doc) {
        List<Document> fields = (List<Document>) doc.get(FIELDS);
        return (intProperty(fields.get(0), COUNT)
                + intProperty(fields.get(1), COUNT)
                + intProperty(fields.get(2), COUNT)) / 3;
    }

    private static int intProperty(Document doc, String key) {
        return ((Number)doc.get(key)).intValue();
    }
}