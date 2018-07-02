package org.radarcns.webapp.param;

import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.stream.Stream;
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

    public Stream<TimeFrame> streamIntervals() {
        TemporalAmount window = RadarConverter.getDuration(timeWindow);
        return Stream.iterate(
                windowTimeFrame(timeFrame.getStartDateTime(), window),
                t -> windowTimeFrame(t.getEndDateTime(), window))
                .limit(getNumberOfWindows());
    }

    private static TimeFrame windowTimeFrame(Instant start, TemporalAmount duration) {
        return new TimeFrame(start, start.plus(duration));
    }

    @Override
    public String toString() {
        return "TimeScale{" + "timeFrame=" + timeFrame
                + ", timeWindow=" + timeWindow
                + ", numberOfWindows=" + getNumberOfWindows()
                + '}';
    }
}
