package org.radarcns.monitor;

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

import com.mongodb.MongoClient;
import java.net.ConnectException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.radarcns.avro.restapi.sensor.Sensor;
import org.radarcns.avro.restapi.sensor.SensorType;
import org.radarcns.avro.restapi.source.Source;
import org.radarcns.avro.restapi.source.SourceSummary;
import org.radarcns.avro.restapi.source.State;
import org.radarcns.dao.SensorDataAccessObject;
import org.radarcns.source.SourceDefinition;
import org.radarcns.util.RadarConverter;

/**
 * Generic source monitor.
 */
public class SourceMonitor {

    //private static final Logger logger = LoggerFactory.getLogger(SourceMonitor.class);

    private final SourceDefinition specification;

    /** Constructor. **/
    public SourceMonitor(SourceDefinition source) {
        this.specification = source;
    }

    /**
     * Checks the status for the given source counting the number of received messages and
     *      checking whether it respects the sensor frequencies. There is a check for each sensor.
     *
     * @param user identifier
     * @param source identifier
     * @param client is the MongoDB client
     * @return {@code SourceDefinition} representing a source source
     * @throws ConnectException if the connection with MongoDb is faulty
     *
     * @see {@link Source}
     */
    public Source getState(String user, String source, MongoClient client)
            throws ConnectException {

        long end = (System.currentTimeMillis() / 10000) * 10000;
        long start = end - 60000;

        System.out.println("Start: " + RadarConverter.getISO8601(new Date(start)));
        System.out.println("End: " + RadarConverter.getISO8601(new Date(end)));

        return getState(user, source, start, end, client);
    }

    /**
     * Checks the status for the given source counting the number of received messages and
     *      checking whether it respects the sensor frequencies. There is a check for each sensor.
     *
     * @param user identifier
     * @param source identifier
     * @oaram start initial time that has to be monitored
     * @param end final time that has to be monitored
     * @param client is the MongoDB client
     * @return {@code SourceDefinition} representing a source source
     * @throws ConnectException if the connection with MongoDb is faulty
     *
     * @see {@link Source}
     */
    public Source getState(String user, String source, long start, long end, MongoClient client)
            throws ConnectException {
        Map<String, Sensor> sensorMap = new HashMap<>();

        double countTemp;
        double percentTemp;
        for (SensorType type : specification.getSensorTypes()) {

            countTemp = SensorDataAccessObject.getInstance().countSamplesByUserSourceWindow(
                user, source, start, end, type, specification.getType(), client);

            percentTemp = getPercentage(countTemp, specification.getFrequency(type) * 60);

            sensorMap.put(type.name(), new Sensor(type, getStatus(percentTemp), (int)countTemp,
                    RadarConverter.roundDouble(1.0 - percentTemp, 2)));
        }

        double countMex = 0;
        double avgPerc = 0;
        for (Sensor sensor : sensorMap.values()) {
            countMex += sensor.getReceivedMessage();
            avgPerc += sensor.getMessageLoss();
        }

        avgPerc = avgPerc / 7.0;

        SourceSummary sourceState = new SourceSummary(getStatus(1 - avgPerc),
                (int)countMex, RadarConverter.roundDouble(avgPerc, 2), sensorMap);

        Source device = new Source(source, specification.getType(), sourceState);

        return device;
    }

    /**
     * Returns the percentage of received message with respect to the expected value.
     *
     * @param count received messages
     * @param expected expected messages
     * @return the ratio of count over expected
     */
    public static double getPercentage(double count, double expected) {
        return count / expected;
    }

    /**
     * Convert numerical percentage to source status.
     *
     * @param percentage numerical value that has to be converted int Status
     * @return the current {@code Status}
     *
     * @see {@link State}
     */
    public static State getStatus(double percentage) {
        if (percentage > 0.95) {
            return State.FINE;
        } else if (percentage > 0.80 && percentage <= 0.95) {
            return State.OK;
        } else if (percentage > 0.0 && percentage <= 0.80) {
            return State.WARNING;
        } else if (percentage == 0.0 ) {
            return State.DISCONNECTED;
        } else {
            return State.UNKNOWN;
        }
    }

    public SourceDefinition getSource() {
        return specification;
    }
}
