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
import static org.radarcns.domain.restapi.header.DescriptiveStatistic.AVERAGE;
import static org.radarcns.domain.restapi.header.DescriptiveStatistic.COUNT;
import static org.radarcns.domain.restapi.header.DescriptiveStatistic.INTERQUARTILE_RANGE;
import static org.radarcns.domain.restapi.header.DescriptiveStatistic.MAXIMUM;
import static org.radarcns.domain.restapi.header.DescriptiveStatistic.MEDIAN;
import static org.radarcns.domain.restapi.header.DescriptiveStatistic.MINIMUM;
import static org.radarcns.domain.restapi.header.DescriptiveStatistic.QUARTILES;
import static org.radarcns.domain.restapi.header.DescriptiveStatistic.SUM;

import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.time.temporal.Temporal;
import org.junit.Test;
import org.radarcns.domain.restapi.ServerStatus;
import org.radarcns.mongo.util.MongoHelper.Stat;

public class RadarConverterTest {

    private static final String ANDROID = "ANDROID";
    private static final String BIOVOTION = "BIOVOTION";
    private static final String EMPATICA = "EMPATICA";

    @Test
    public void getDescriptiveStatisticTest() throws DateTimeParseException {
        assertEquals(AVERAGE, RadarConverter.getDescriptiveStatistic(Stat.avg));
        assertEquals(COUNT, RadarConverter.getDescriptiveStatistic(Stat.count));
        assertEquals(MAXIMUM, RadarConverter.getDescriptiveStatistic(Stat.max));
        assertEquals(MEDIAN, RadarConverter.getDescriptiveStatistic(Stat.median));
        assertEquals(MINIMUM, RadarConverter.getDescriptiveStatistic(Stat.min));
        assertEquals(INTERQUARTILE_RANGE, RadarConverter.getDescriptiveStatistic(Stat.iqr));
        assertEquals(SUM, RadarConverter.getDescriptiveStatistic(Stat.sum));
    }

    @Test
    public void getMongoStatTest() throws DateTimeParseException {
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
    public void roundDoubleTest() throws DateTimeParseException {
        double value = 1234.56789;
        double expected = 1234.57;
        assertEquals(expected, RadarConverter.roundDouble(value, 2), 0);
    }

    @Test
    public void getServerStatusTest() throws DateTimeParseException {
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
    public void isThresholdPassed() {
        Temporal hourAgo = Instant.now().minus(Duration.ofHours(1));
        Duration lessThanAnHour = Duration.ofHours(1).minus(Duration.ofMinutes(1));
        Duration moreThanAnHour = lessThanAnHour.plus(Duration.ofMinutes(2));
        assertTrue(RadarConverter.isThresholdPassed(hourAgo, lessThanAnHour));
        assertFalse(RadarConverter.isThresholdPassed(hourAgo, moreThanAnHour));
    }

    @Test
    public void testInstant() throws IOException {
        ObjectWriter writer = RadarConverter
                .writerFor(Instant.class);
        String epochString = writer.writeValueAsString(Instant.EPOCH);
        assertEquals("\"1970-01-01T00:00:00Z\"", epochString);

        ObjectReader reader = RadarConverter.readerFor(Instant.class);
        assertEquals(Instant.EPOCH, reader.readValue(epochString));
    }
}
