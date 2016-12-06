package org.radarcns.util;

import org.radarcns.avro.restapi.header.DescriptiveStatistic;
import org.radarcns.dao.mongo.util.MongoDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Francesco Nobilia on 27/10/2016.
 */
public class RadarConverter {

    private static Logger logger = LoggerFactory.getLogger(RadarConverter.class);

    public static String getISO8601(Date value){
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(tz);
        return df.format(value);
    }

    public static DescriptiveStatistic getDescriptiveStatistic(MongoDAO.Stat stat){
        switch (stat){
            case avg: return DescriptiveStatistic.average;
            case count: return DescriptiveStatistic.count;
            case iqr: return DescriptiveStatistic.interquartile_range;
            case max: return DescriptiveStatistic.maximum;
            case min: return DescriptiveStatistic.minimum;
            case sum: return DescriptiveStatistic.sum;
            case quartile: return DescriptiveStatistic.quartiles;
            case median: return DescriptiveStatistic.median;
            default: logger.info("No translation for {}",stat); return null;
        }
    }

}
