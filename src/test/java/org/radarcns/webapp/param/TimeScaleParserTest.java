package org.radarcns.webapp.param;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.MINUTES;
import static org.junit.Assert.assertEquals;
import static org.radarcns.domain.restapi.TimeWindow.ONE_MIN;
import static org.radarcns.domain.restapi.TimeWindow.TEN_SECOND;
import static org.radarcns.webapp.param.TimeScaleParser.DEFAULT_NUMBER_OF_WINDOWS;

import java.time.Duration;
import java.time.Instant;
import javax.ws.rs.BadRequestException;
import org.junit.Before;
import org.junit.Test;
import org.radarcns.domain.restapi.TimeWindow;

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

    @Test(expected = BadRequestException.class)
    public void testTooLarge() {
        Instant now = Instant.now();
        InstantParam start = new InstantParam(now.minus(365, DAYS).toString());
        InstantParam end = new InstantParam(now.toString());
        parser.parse(start, end, TEN_SECOND);
    }

    @Test(expected = BadRequestException.class)
    public void testWrongStartTime() {
        Instant now = Instant.now();
        InstantParam start = new InstantParam(now.plus(1, MINUTES).toString());
        InstantParam end = new InstantParam(now.toString());
        parser.parse(start, end, TEN_SECOND);
    }
}
