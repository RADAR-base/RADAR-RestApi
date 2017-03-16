package org.radarcns.pipeline.data;

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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import org.radarcns.pipeline.config.Config;
import org.radarcns.security.Param;


/**
 * Generic CSV sensor definition.
 */
public abstract class CsvSensor {

    public static final String userIdMock = "UserID_0";
    public static final String sourceIdMock = "SourceID_0";

    private List<String> headers;
    private String user;
    private String source;

    //private static final Logger logger = LoggerFactory.getLogger(CsvSensor.class);

    /**
     * Constructor.
     * @param headers {@code List<String>} containing the fields name that has to be generated
     */
    public CsvSensor(List<String> headers) {
        this(headers, null, null,
            null);
    }

    /**
     * Constructor.
     * @param headers {@code List<String>} containing the fields name that has to be generated
     * @param user user identifier
     * @param source source identifier
     * @param timeZero initial instant used to compute all needed instants
     */
    public CsvSensor(List<String> headers, String user, String source, Long timeZero) {
        this.headers = new LinkedList<>();
        this.headers.add("userId");
        this.headers.add("sourceId");
        this.headers.add("time");
        this.headers.add("timeReceived");

        this.headers.addAll(headers);

        this.user = user;
        this.source = source;

        if (Param.isNullOrEmpty(this.user)) {
            this.user = userIdMock;
        }

        if (Param.isNullOrEmpty(this.source)) {
            this.source = sourceIdMock;
        }
    }

    public String getUser() {
        return user;
    }

    public String getSource() {
        return source;
    }

    /**
     * @return a comma separated {@code String} containing all header names.
     **/
    public String getHeaders() {
        String result = "";
        for (String header : headers) {
            result += header + ",";
        }

        return result.substring(0, result.length() - 1) + '\n';
    }

    public List<String> getValues(Long duration, int frequency) {
        List<String> list = new LinkedList<>();

        int samples = duration.intValue() * frequency;
        List<Long> timestamps = Metronome.timestamps(samples, frequency, 64);

        for (Long time : timestamps) {
            list.add(getUser() + "," + getSource() + ","
                + getTimestamp(getRandomRoundTripTime(time)) + "," + getTimestamp(time)
                + "," + nextValue() + "\n");
        }

        return list;
    }

    protected abstract String nextValue();

    /**
     * Converts a timestamp in a string stating a timestamp expressed in double
     * @param time value that has to be converted
     * @return a string value representing a timestamp
     */
    public static String getTimestamp(long time) {
        return new Double(time / 1000d).toString();
    }

    /**
     * @return random {@code Double} using {@code ThreadLocalRandom}.
     * @see {@link ThreadLocalRandom}
     **/
    public static Double getRandomDouble() {
        return ThreadLocalRandom.current().nextDouble();
    }

    /**
     * It returns a random {@code Double} between min and max.
     * @param min range lower bound
     * @param max range upper bound
     * @return random {@code Double} using {@code ThreadLocalRandom}
     * @see {@link ThreadLocalRandom}
     **/
    public static Double getRandomDouble(double min, double max) {
        return ThreadLocalRandom.current().nextDouble(min , max);
    }

    /**
     * It returns a random {@code Float} between min and max.
     * @param min range lower bound
     * @param max range upper bound
     * @return random {@code Float} using {@code ThreadLocalRandom}
     * @see {@link ThreadLocalRandom}
     **/
    public static Float getRandomFloat(float min, float max) {
        Double value = ThreadLocalRandom.current().nextDouble(min , max);
        return value.floatValue();
    }

    /**
     * It returns the a random Round Trip Time for the given in input time.
     * @param timeReceived time at which the message has been received
     * @return random {@code Double} representig the Round Trip Time for the given timestamp
     *      using {@code ThreadLocalRandom}
     * @see {@link ThreadLocalRandom}
     **/
    public Long getRandomRoundTripTime(Long timeReceived) {
        return timeReceived - (
                ThreadLocalRandom.current().nextLong(1 , 10));
    }
}
