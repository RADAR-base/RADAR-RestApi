package org.radarcns.webapp.param;

import static java.time.temporal.ChronoUnit.HOURS;
import static org.junit.Assert.assertEquals;
import static org.radarcns.domain.restapi.TimeWindow.ONE_MIN;
import static org.radarcns.webapp.param.TimeScaleParser.DEFAULT_NUMBER_OF_WINDOWS;

import java.time.Duration;
import java.time.Instant;
import org.junit.Before;
import org.junit.Test;
import org.radarcns.domain.restapi.TimeWindow;
import org.radarcns.webapp.param.TimeScaleParser.TimeScale;

public class TimeScaleParserTest {
    private TimeScaleParser parser;

    @Before
    public void setUp() {
        parser = new TimeScaleParser();
    }

    @Test
    public void testDefaults() {
        TimeScale scale = parser.parse(null, null, null);
        assertEquals(Duration.ofDays(365), scale.getTimeFrame().getDuration());
        assertEquals(TimeWindow.ONE_WEEK, scale.getTimeWindow());
        assertEquals(52, scale.getNumberOfWindows());
    }

    @Test
    public void testDefaultTimeFrame() {
        TimeScale scale = parser.parse(null, null, ONE_MIN);
        assertEquals(Duration.ofMinutes(DEFAULT_NUMBER_OF_WINDOWS), scale.getTimeFrame().getDuration());
        assertEquals(ONE_MIN, scale.getTimeWindow());
        assertEquals(DEFAULT_NUMBER_OF_WINDOWS, scale.getNumberOfWindows());
    }

    @Test
    public void testFixedTimeScale() {
        Instant now = Instant.now();
        InstantParam start = new InstantParam(now.minus(1, HOURS).toString());
        InstantParam end = new InstantParam(now.toString());
        TimeScale scale = parser.parse(start, end, ONE_MIN);
        assertEquals(scale.getTimeFrame().getDuration(),
                Duration.ofHours(1));
        assertEquals(scale.getTimeWindow(), ONE_MIN);
        assertEquals(scale.getNumberOfWindows(), 60);
    }

    @Test
    public void testDefaultTimeWindow() {
        Instant now = Instant.now();
        InstantParam start = new InstantParam(now.minus(1, HOURS).toString());
        InstantParam end = new InstantParam(now.toString());

        TimeScale scale = parser.parse(start, end, null);
        assertEquals(scale.getTimeFrame().getDuration(),
                Duration.ofHours(1));
        assertEquals(scale.getTimeWindow(), ONE_MIN);
        assertEquals(scale.getNumberOfWindows(), 60);
    }
}