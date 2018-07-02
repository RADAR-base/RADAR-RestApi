package org.radarcns.util;

import static org.junit.Assert.*;

import org.junit.Test;
import org.radarcns.domain.restapi.TimeWindow;

public class TimeScaleTest {
    @Test
    public void getSecond() {
        assertEquals(10, TimeScale.getSeconds(TimeWindow.TEN_SECOND), 0);
        assertEquals(60, TimeScale.getSeconds(TimeWindow.ONE_MIN), 0);
        assertEquals(600, TimeScale.getSeconds(TimeWindow.TEN_MIN), 0);
        assertEquals(3600, TimeScale.getSeconds(TimeWindow.ONE_HOUR), 0);
        assertEquals(3600 * 24, TimeScale.getSeconds(TimeWindow.ONE_DAY), 0);
        assertEquals(3600 * 24 * 7, TimeScale.getSeconds(TimeWindow.ONE_WEEK), 0);
    }
}