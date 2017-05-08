package org.radarcns.unit.util;

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

import static org.junit.Assert.assertEquals;
import static org.radarcns.avro.restapi.header.DescriptiveStatistic.AVERAGE;
import static org.radarcns.avro.restapi.header.DescriptiveStatistic.COUNT;
import static org.radarcns.avro.restapi.header.DescriptiveStatistic.INTERQUARTILE_RANGE;
import static org.radarcns.avro.restapi.header.DescriptiveStatistic.MAXIMUM;
import static org.radarcns.avro.restapi.header.DescriptiveStatistic.MEDIAN;
import static org.radarcns.avro.restapi.header.DescriptiveStatistic.MINIMUM;
import static org.radarcns.avro.restapi.header.DescriptiveStatistic.QUARTILES;
import static org.radarcns.avro.restapi.header.DescriptiveStatistic.SUM;
import static org.radarcns.avro.restapi.sensor.SensorType.ACCELEROMETER;
import static org.radarcns.avro.restapi.sensor.SensorType.BATTERY;
import static org.radarcns.avro.restapi.sensor.SensorType.BLOOD_VOLUME_PULSE;
import static org.radarcns.avro.restapi.sensor.SensorType.ELECTRODERMAL_ACTIVITY;
import static org.radarcns.avro.restapi.sensor.SensorType.HEART_RATE;
import static org.radarcns.avro.restapi.sensor.SensorType.INTER_BEAT_INTERVAL;
import static org.radarcns.avro.restapi.sensor.SensorType.THERMOMETER;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.junit.Test;
import org.radarcns.avro.restapi.app.ServerStatus;
import org.radarcns.avro.restapi.source.SourceType;
import org.radarcns.dao.mongo.util.MongoHelper.Stat;
import org.radarcns.util.RadarConverter;

public class RadarConverterTest {

    @Test
    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    public void getISO8601TestFromDate() throws ParseException {
        Date date = new Date();
        Calendar calExpected = Calendar.getInstance();
        calExpected.setTime(date);
        // we will get UTC time from RadarConverter
        calExpected.setTimeZone(TimeZone.getTimeZone("UTC"));

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
    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    public void getISO8601TestFromString() throws ParseException {
        Date date = RadarConverter.getISO8601("2017-03-05T22:37:59Z");

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.setTimeZone(TimeZone.getTimeZone("UTC"));

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
        assertEquals("acceleration", RadarConverter.getSensorName(ACCELEROMETER));
        assertEquals("battery", RadarConverter.getSensorName(BATTERY));
        assertEquals("blood_volume_pulse",
                RadarConverter.getSensorName(BLOOD_VOLUME_PULSE));
        assertEquals("electrodermal_activity",
                RadarConverter.getSensorName(ELECTRODERMAL_ACTIVITY));
        assertEquals("heart_rate", RadarConverter.getSensorName(HEART_RATE));
        assertEquals("inter_beat_interval",
                RadarConverter.getSensorName(INTER_BEAT_INTERVAL));
        assertEquals("temperature", RadarConverter.getSensorName(THERMOMETER));
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
}
