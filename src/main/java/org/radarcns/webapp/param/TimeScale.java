package org.radarcns.webapp.param;

import org.radarcns.domain.restapi.TimeWindow;
import org.radarcns.domain.restapi.header.TimeFrame;
import org.radarcns.util.RadarConverter;

/**
 * TimeScale containing temporal extent and resolution.
 */
public class TimeScale {
    private final TimeFrame timeFrame;
    private final TimeWindow timeWindow;

    public TimeScale(TimeFrame timeFrame, TimeWindow timeWindow) {
        this.timeFrame = timeFrame;
        this.timeWindow = timeWindow;
    }

    public TimeFrame getTimeFrame() {
        return timeFrame;
    }

    public TimeWindow getTimeWindow() {
        return timeWindow;
    }

    public long getNumberOfWindows() {
        return (long) Math.floor(timeFrame.getDuration().getSeconds()
                        / (double) RadarConverter.getSecond(timeWindow));
    }

    @Override
    public String toString() {
        return "TimeScale{" + "timeFrame=" + timeFrame
                + ", timeWindow=" + timeWindow
                + ", numberOfWindows=" + getNumberOfWindows()
                + '}';
    }
}
