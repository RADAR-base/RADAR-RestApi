package org.radarcns.util;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import org.apache.avro.specific.SpecificRecord;
import org.radarcns.avro.restapi.app.ServerStatus;
import org.radarcns.avro.restapi.header.DescriptiveStatistic;
import org.radarcns.avro.restapi.header.Header;
import org.radarcns.avro.restapi.header.TimeFrame;
import org.radarcns.avro.restapi.sensor.SensorType;
import org.radarcns.avro.restapi.source.SourceType;
import org.radarcns.dao.mongo.util.MongoHelper;
import org.radarcns.dao.mongo.util.MongoHelper.Stat;
import org.radarcns.security.Param;
import org.radarcns.source.SourceCatalog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Set of converting functions.
 */
public class RadarConverter {

    private static Logger logger = LoggerFactory.getLogger(RadarConverter.class);

    /**
     * Converts {@code long} to ISO8601 {@code String}.
     * @param value input {@code long} that has to be converted
     * @return a {@code String} representing a date in ISO8601 format
     * @see <a href="http://www.iso.org/iso/home/standards/iso8601.htm>ISO8601 specification</a>
     **/
    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    public static String getISO8601(long value) {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(tz);
        return df.format(new Date(value));
    }

    /**
     * Converts {@link Date} to ISO8601 {@code String}.
     * @param value input {@link Date} that has to be converted
     * @return a {@code String} representing a date in ISO8601 format
     * @see <a href="http://www.iso.org/iso/home/standards/iso8601.htm>ISO8601 specification</a>
     **/
    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    public static String getISO8601(Date value) {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(tz);
        return df.format(value);
    }

    /**
     * Converts ISO8601 {@code String} to java {@link Date}.
     * @param value input {@code String} formatted in ISO8601
     * @return {@link Date} object according to the given input
     * @see <a href="http://www.iso.org/iso/home/standards/iso8601.htm>ISO8601 specification</a>
     **/
    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    public static Date getISO8601(String value) throws ParseException {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(tz);
        return df.parse(value);
    }

    /**
     * Converts a {@code MongoHelper.Stat} to {@code DescriptiveStatistic}.
     **/
    public static DescriptiveStatistic getDescriptiveStatistic(MongoHelper.Stat stat) {
        switch (stat) {
            case avg: return DescriptiveStatistic.AVERAGE;
            case count: return DescriptiveStatistic.COUNT;
            case iqr: return DescriptiveStatistic.INTERQUARTILE_RANGE;
            case max: return DescriptiveStatistic.MAXIMUM;
            case min: return DescriptiveStatistic.MINIMUM;
            case sum: return DescriptiveStatistic.SUM;
            case quartile: return DescriptiveStatistic.QUARTILES;
            case median: return DescriptiveStatistic.MEDIAN;
            case receivedMessage: return DescriptiveStatistic.RECEIVED_MESSAGES;
            default: throw new IllegalArgumentException("MongoHelper.Stat type cannot be"
                + "converted. " + stat.name() + " is unknown");
        }
    }

    /**
     * Converts a {@code DescriptiveStatistic} to {@code MongoHelper.Stat}.
     **/
    public static MongoHelper.Stat getMongoStat(DescriptiveStatistic stat) {
        switch (stat) {
            case AVERAGE: return MongoHelper.Stat.avg;
            case COUNT: return MongoHelper.Stat.count;
            case INTERQUARTILE_RANGE: return MongoHelper.Stat.iqr;
            case MAXIMUM: return MongoHelper.Stat.max;
            case MEDIAN: return MongoHelper.Stat.median;
            case MINIMUM: return MongoHelper.Stat.min;
            case QUARTILES: return MongoHelper.Stat.quartile;
            case RECEIVED_MESSAGES: return Stat.receivedMessage;
            case SUM: return MongoHelper.Stat.sum;
            default: throw new IllegalArgumentException("DescriptiveStatistic type cannot be"
                    + "converted. " + stat.name() + "is unknown");
        }
    }

    /**
     * Rounds a double input.
     * @param value input
     * @param places the required decimal places precision
     **/
    public static double roundDouble(double value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }

        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    /**
     * Converts the input String in Server Status.
     **/
    public static ServerStatus getServerStatus(String value) {
        if (Param.isNullOrEmpty(value)) {
            return ServerStatus.UNKNOWN;
        }

        String temp = value.toUpperCase();
        if (temp.equals(ServerStatus.CONNECTED.toString())) {
            return ServerStatus.CONNECTED;
        } else if (temp.equals(ServerStatus.DISCONNECTED.toString())) {
            return ServerStatus.DISCONNECTED;
        } else if (temp.equals(ServerStatus.UNKNOWN.toString())) {
            return ServerStatus.UNKNOWN;
        } else {
            logger.warn("Unsupported ServerStatus. Value is {}", value);
            return ServerStatus.UNKNOWN;
        }
    }

    /**
     * Converts the SensorType to the related data name used to convert AVRO to JSON.
     **/
    public static String getSensorName(SensorType sensor) {
        switch (sensor) {
            case ACCELEROMETER: return "acceleration";
            case BATTERY: return "battery";
            case BLOOD_VOLUME_PULSE: return "blood_volume_pulse";
            case ELECTRODERMAL_ACTIVITY: return "electrodermal_activity";
            case HEART_RATE: return "heart_rate";
            case INTER_BEAT_INTERVAL: return "inter_beat_interval";
            case THERMOMETER: return "temperature";
            default: throw new IllegalArgumentException("Sensor type cannot be converted. "
                    + sensor.name() + "is unknown");
        }
    }

    /**
     * Converts a String to the related source type.
     **/
    public static SourceType getSourceType(String value) {
        for (SourceType source : SourceType.values()) {
            if (source.name().equalsIgnoreCase(value)) {
                return source;
            }
        }

        throw new IllegalArgumentException(value + " cannot be converted to SourceDefinition type");
    }

    /**
     * Converts AVRO objects in pretty JSON.
     * @param record Specific Record that has to be converted
     * @return String with the object serialised in pretty JSON
     */
    public static String getPrettyJson(SpecificRecord record) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Object json = mapper.readValue(record.toString(), Object.class);
        return mapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(json);
    }

    /**
     * Returns the amount of expected data related to the {@link SourceType}, {@link SensorType} and
     *      {@link TimeFrame} specified in the {@link Header}.
     *
     * @param header {@link Header} to provide data context
     *
     * @return the number of expected messages
     */
    public static Double getExpectedMessages(Header header) {
        return SourceCatalog.getInstance(header.getSource()).getFrequency(
                header.getSensor()) * getSecond(header.getTimeFrame()).doubleValue();
    }

    /**
     * Converts a {@link TimeFrame} to seconds.
     *
     * @param timeFrame {@link TimeFrame} that has to be converted in seconds
     *
     * @return a {@link Long} representing the amount of seconds
     */
    public static Long getSecond(TimeFrame timeFrame) {
        switch (timeFrame) {
            case TEN_SECOND: return TimeUnit.SECONDS.toSeconds(10);
            case THIRTY_SECOND: return TimeUnit.SECONDS.toSeconds(30);
            case ONE_MIN: return TimeUnit.MINUTES.toSeconds(1);
            case TEN_MIN: return TimeUnit.MINUTES.toSeconds(10);
            case ONE_HOUR: return TimeUnit.HOURS.toSeconds(1);
            case ONE_DAY: return TimeUnit.DAYS.toSeconds(1);
            case ONE_WEEK: return TimeUnit.DAYS.toSeconds(7);
            default: throw new IllegalArgumentException(timeFrame.name() + " is not yet supported");
        }
    }
}
