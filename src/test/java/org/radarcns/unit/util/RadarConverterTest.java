package org.radarcns.unit.util;

import static org.junit.Assert.assertEquals;
import static org.radarcns.avro.restapi.header.DescriptiveStatistic.AVERAGE;
import static org.radarcns.avro.restapi.header.DescriptiveStatistic.COUNT;
import static org.radarcns.avro.restapi.header.DescriptiveStatistic.INTERQUARTILE_RANGE;
import static org.radarcns.avro.restapi.header.DescriptiveStatistic.MAXIMUM;
import static org.radarcns.avro.restapi.header.DescriptiveStatistic.MEDIAN;
import static org.radarcns.avro.restapi.header.DescriptiveStatistic.MINIMUM;
import static org.radarcns.avro.restapi.header.DescriptiveStatistic.QUARTILES;
import static org.radarcns.avro.restapi.header.DescriptiveStatistic.SUM;
import static org.radarcns.avro.restapi.sensor.SensorType.ACC;
import static org.radarcns.avro.restapi.sensor.SensorType.BAT;
import static org.radarcns.avro.restapi.sensor.SensorType.BVP;
import static org.radarcns.avro.restapi.sensor.SensorType.EDA;
import static org.radarcns.avro.restapi.sensor.SensorType.HR;
import static org.radarcns.avro.restapi.sensor.SensorType.IBI;
import static org.radarcns.avro.restapi.sensor.SensorType.TEMP;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import org.junit.Test;
import org.radarcns.avro.restapi.app.ServerStatus;
import org.radarcns.avro.restapi.source.SourceType;
import org.radarcns.dao.mongo.util.MongoHelper.Stat;
import org.radarcns.util.RadarConverter;

/**
 * Created by francesco on 05/03/2017.
 */
public class RadarConverterTest {

    @Test
    public void getISO8601TestFromDate() throws ParseException {
        Date date = new Date();
        Calendar calExpected = Calendar.getInstance();
        calExpected.setTime(date);

        String dateString = RadarConverter.getISO8601(date);

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        Date dateResult = format.parse(dateString);
        Calendar calActual = Calendar.getInstance();
        calActual.setTime(dateResult);

        assertEquals(calExpected.get(Calendar.YEAR), calActual.get(Calendar.YEAR));
        assertEquals(calExpected.get(Calendar.MONTH), calActual.get(Calendar.MONTH));
        assertEquals(calExpected.get(Calendar.DAY_OF_MONTH), calActual.get(Calendar.DAY_OF_MONTH));
        assertEquals(calExpected.get(Calendar.HOUR), calActual.get(Calendar.HOUR));
        assertEquals(calExpected.get(Calendar.MINUTE), calActual.get(Calendar.MINUTE));
        assertEquals(calExpected.get(Calendar.SECOND), calActual.get(Calendar.SECOND));
    }

    @Test
    public void getISO8601TestFromString() throws ParseException {
        Date date = RadarConverter.getISO8601("2017-03-05T22:37:59Z");

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        assertEquals(2017, cal.get(Calendar.YEAR));
        assertEquals(2, cal.get(Calendar.MONTH));
        assertEquals(5, cal.get(Calendar.DAY_OF_MONTH));

        assertEquals(10, cal.get(Calendar.HOUR));
        assertEquals(37, cal.get(Calendar.MINUTE));
        assertEquals(59, cal.get(Calendar.SECOND));
        assertEquals(1, cal.get(Calendar.AM_PM));
    }

    @Test
    public void getDescriptiveStatisticTest() throws ParseException {
        assertEquals(AVERAGE, RadarConverter.getDescriptiveStatistic(Stat.avg));
        assertEquals(COUNT, RadarConverter.getDescriptiveStatistic(Stat.count));
        assertEquals(MAXIMUM, RadarConverter.getDescriptiveStatistic(Stat.max));
        assertEquals(MEDIAN, RadarConverter.getDescriptiveStatistic(Stat.median));
        assertEquals(MINIMUM, RadarConverter.getDescriptiveStatistic(Stat.min));
        assertEquals(INTERQUARTILE_RANGE, RadarConverter.getDescriptiveStatistic(Stat.iqr));
        assertEquals(SUM, RadarConverter.getDescriptiveStatistic(Stat.sum));
    }

    @Test
    public void getMongoStatTest() throws ParseException {
        assertEquals(Stat.avg, RadarConverter.getMongoStat(AVERAGE));
        assertEquals(Stat.count, RadarConverter.getMongoStat(COUNT));
        assertEquals(Stat.iqr, RadarConverter.getMongoStat(INTERQUARTILE_RANGE));
        assertEquals(Stat.max, RadarConverter.getMongoStat(MAXIMUM));
        assertEquals(Stat.median, RadarConverter.getMongoStat(MEDIAN));
        assertEquals(Stat.min, RadarConverter.getMongoStat(MINIMUM));
        assertEquals(Stat.quartile, RadarConverter.getMongoStat(QUARTILES));
        assertEquals(Stat.iqr, RadarConverter.getMongoStat(INTERQUARTILE_RANGE));
    }

    @Test
    public void roundDoubleTest() throws ParseException {
        double value = 1234.56789;
        double expected = 1234.57;
        assertEquals(expected, RadarConverter.roundDouble(value, 2), 0);
    }

    @Test
    public void getServerStatusTest() throws ParseException {
        assertEquals(ServerStatus.CONNECTED,
                RadarConverter.getServerStatus(ServerStatus.CONNECTED.toString()));
        assertEquals(ServerStatus.DISCONNECTED,
                RadarConverter.getServerStatus(ServerStatus.DISCONNECTED.toString()));
        assertEquals(ServerStatus.UNKNOWN,
                RadarConverter.getServerStatus(ServerStatus.UNKNOWN.toString()));
        assertEquals(ServerStatus.UNKNOWN,
                RadarConverter.getServerStatus("test"));
    }

    @Test
    public void getSensorNameTest() {
        assertEquals("acceleration", RadarConverter.getSensorName(ACC));
        assertEquals("battery", RadarConverter.getSensorName(BAT));
        assertEquals("blood_volume_pulse", RadarConverter.getSensorName(BVP));
        assertEquals("electrodermal_activity", RadarConverter.getSensorName(EDA));
        assertEquals("heart_rate", RadarConverter.getSensorName(HR));
        assertEquals("inter_beat_interval", RadarConverter.getSensorName(IBI));
        assertEquals("temperature", RadarConverter.getSensorName(TEMP));
    }

    @Test( expected = IllegalArgumentException.class)
    public void getSourceTypeTest() {
        assertEquals(SourceType.ANDROID,
                RadarConverter.getSourceType(SourceType.ANDROID.toString()));
        assertEquals(SourceType.BIOVOTION,
                RadarConverter.getSourceType(SourceType.BIOVOTION.toString()));
        assertEquals(SourceType.EMPATICA,
                RadarConverter.getSourceType(SourceType.EMPATICA.toString()));
        assertEquals(SourceType.PEBBLE,
                RadarConverter.getSourceType(SourceType.PEBBLE.toString()));

        RadarConverter.getSourceType(SourceType.PEBBLE.toString().concat("test"));
    }


//
//    /**
//     * Converts a String to the related source type.
//     **/
//    public static SourceType getSourceType(String value) {
//        for (SourceType source : SourceType.values()) {
//            if (source.name().toLowerCase().equals(value.toLowerCase())) {
//                return source;
//            }
//        }
//
//        throw new IllegalArgumentException(value + " cannot be converted to SourceDefinition type");
//    }
//
}