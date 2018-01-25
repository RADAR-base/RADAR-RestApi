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
import static org.radarcns.mongo.util.MongoHelper.FIELDS;

import java.util.List;
import org.bson.Document;
import org.radarcns.mongo.util.MongoSensor;
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
                List<Double> xq = getQuartiles(x);
                List<Double> yq = getQuartiles(y);
                List<Double> zq = getQuartiles(z);

                return new Acceleration(
                        new Quartiles(xq.get(0), xq.get(1), xq.get(2)),
                        new Quartiles(yq.get(0), yq.get(1), yq.get(2)),
                        new Quartiles(zq.get(0), zq.get(1), zq.get(2)));
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
        List<Document> fields = (List<Document>) doc.get(COUNT);
        return (intProperty(fields.get(0), X_LABEL)
                + intProperty(fields.get(1), Y_LABEL)
                + intProperty(fields.get(2), Z_LABEL)) / 3;
    }

    private static int intProperty(Document doc, String key) {
        return ((Number)doc.get(key)).intValue();
    }
}