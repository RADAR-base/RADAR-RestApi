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

package org.radarcns.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.radarcns.restapi.header.DescriptiveStatistic.AVERAGE;
import static org.radarcns.restapi.header.DescriptiveStatistic.COUNT;
import static org.radarcns.restapi.header.DescriptiveStatistic.INTERQUARTILE_RANGE;
import static org.radarcns.restapi.header.DescriptiveStatistic.MAXIMUM;
import static org.radarcns.restapi.header.DescriptiveStatistic.MEDIAN;
import static org.radarcns.restapi.header.DescriptiveStatistic.MINIMUM;
import static org.radarcns.restapi.header.DescriptiveStatistic.QUARTILES;
import static org.radarcns.restapi.header.DescriptiveStatistic.SUM;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.Temporal;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.junit.Test;
import org.radarcns.catalogue.TimeWindow;
import org.radarcns.dao.mongo.util.MongoHelper.Stat;
import org.radarcns.monitor.application.ServerStatus;

public class RadarConverterTest {

    private static final String ANDROID = "ANDROID";
    private static final String BIOVOTION = "BIOVOTION";
    private static final String EMPATICA = "EMPATICA";

    @Test
    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    public void getISO8601TestFromDate() throws ParseException {
        Date date = new Date();
        Calendar calExpected = Calendar.getInstance();
        calExpected.setTime(date);
        // we will get UTC time from RadarConverter
        calExpected.setTimeZone(TimeZone.getTimeZone("UTC"));

        String dateString = RadarConverter.getISO8601(date);

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
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
        assertEquals("acceleration", RadarConverter.getSensorName("ACCELEROMETER"));
        assertEquals("battery", RadarConverter.getSensorName("BATTERY"));
        assertEquals("blood_volume_pulse",
                RadarConverter.getSensorName("BLOOD_VOLUME_PULSE"));
        assertEquals("electrodermal_activity",
                RadarConverter.getSensorName("ELECTRODERMAL_ACTIVITY"));
        assertEquals("heart_rate", RadarConverter.getSensorName("HEART_RATE"));
        assertEquals("inter_beat_interval",
                RadarConverter.getSensorName("INTER_BEAT_INTERVAL"));
        assertEquals("temperature", RadarConverter.getSensorName("THERMOMETER"));
    }

    @Test
    public void getSourceTypeTest() {
        assertEquals(ANDROID,
                RadarConverter.getSourceType(ANDROID));
        assertEquals(BIOVOTION,
                RadarConverter.getSourceType(BIOVOTION));
        assertEquals(EMPATICA,
                RadarConverter.getSourceType(EMPATICA));
        assertEquals("PEBBLE",
                RadarConverter.getSourceType("PEBBLE"));
    }

    @Test
    public void getSecondTest() {
        assertEquals(10, RadarConverter.getSecond(TimeWindow.TEN_SECOND), 0);
        assertEquals(60, RadarConverter.getSecond(TimeWindow.ONE_MIN), 0);
        assertEquals(600, RadarConverter.getSecond(TimeWindow.TEN_MIN), 0);
        assertEquals(3600, RadarConverter.getSecond(TimeWindow.ONE_HOUR), 0);
        assertEquals(3600 * 24, RadarConverter.getSecond(TimeWindow.ONE_DAY), 0);
        assertEquals(3600 * 24 * 7, RadarConverter.getSecond(TimeWindow.ONE_WEEK), 0);
    }

    @Test
    public void isThresholdPassed() {
        Temporal hourAgo = Instant.now().minus(Duration.ofHours(1));
        Duration lessThanAnHour = Duration.ofHours(1).minus(Duration.ofMinutes(1));
        Duration moreThanAnHour = lessThanAnHour.plus(Duration.ofMinutes(2));
        assertTrue(RadarConverter.isThresholdPassed(hourAgo, lessThanAnHour));
        assertFalse(RadarConverter.isThresholdPassed(hourAgo, moreThanAnHour));
    }
}
