package org.radarcns.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import org.radarcns.avro.restapi.app.ServerStatus;
import org.radarcns.avro.restapi.header.DescriptiveStatistic;
import org.radarcns.dao.mongo.util.MongoHelper;
import org.radarcns.security.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Set of converting functions.
 */
public class RadarConverter {

    private static Logger logger = LoggerFactory.getLogger(RadarConverter.class);

    /**
     * Convert java Date to ISO8601 String.
     * @param value input {@code Date} that has to be converted
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
     * Convert ISO8601 {@code String} to java {@code Date}.
     * @param value input {@code String} formatted in ISO8601
     * @return {@code Date} object according to the given input
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
     * It converts a {@code MongoHelper.Stat} to {@code DescriptiveStatistic}.
     **/
    public static DescriptiveStatistic getDescriptiveStatistic(MongoHelper.Stat stat) {
        switch (stat) {
            case avg: return DescriptiveStatistic.average;
            case count: return DescriptiveStatistic.count;
            case iqr: return DescriptiveStatistic.interquartile_range;
            case max: return DescriptiveStatistic.maximum;
            case min: return DescriptiveStatistic.minimum;
            case sum: return DescriptiveStatistic.sum;
            case quartile: return DescriptiveStatistic.quartiles;
            case median: return DescriptiveStatistic.median;
            default: logger.info("No translation for {}",stat);
                    return null;
        }
    }

    /**
     * It converts a {@code DescriptiveStatistic} to {@code MongoHelper.Stat}.
     **/
    public static MongoHelper.Stat getDescriptiveStatistic(DescriptiveStatistic stat) {
        switch (stat) {
            case average: return MongoHelper.Stat.avg;
            case count: return MongoHelper.Stat.count;
            case interquartile_range: return MongoHelper.Stat.iqr;
            case maximum: return MongoHelper.Stat.max;
            case minimum: return MongoHelper.Stat.min;
            case sum: return MongoHelper.Stat.sum;
            case quartiles: return MongoHelper.Stat.quartile;
            case median: return MongoHelper.Stat.median;
            default: logger.info("No translation for {}",stat);
                    return null;
        }
    }

    /**
     * It rounds a double input.
     * @param value input
     * @param places the required decimal places precision
     **/
    public static double roundDouble(double value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }

        long factor = (long) Math.pow(10, places);
        double valueTemp = value * factor;
        long tmp = Math.round(valueTemp);
        return (double) tmp / factor;
    }

    /**
     * It converts the input String in Server Status.
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

}
