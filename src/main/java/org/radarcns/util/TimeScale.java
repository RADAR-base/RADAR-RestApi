package org.radarcns.util;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.radarcns.domain.restapi.TimeWindow;
import org.radarcns.domain.restapi.header.TimeFrame;

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
                / (double) getWindowSeconds());
    }

    public Stream<TimeFrame> streamIntervals() {
        TemporalAmount window = getWindowDuration();
        return Stream.iterate(
                windowTimeFrame(timeFrame.getStartDateTime(), window),
                t -> windowTimeFrame(t.getEndDateTime(), window))
                .limit(getNumberOfWindows());
    }

    private static TimeFrame windowTimeFrame(Instant start, TemporalAmount duration) {
        return new TimeFrame(start, start.plus(duration));
    }

    public long getWindowSeconds() {
        return getSeconds(timeWindow);
    }

    public TemporalAmount getWindowDuration() {
        return getDuration(timeWindow);
    }

    @Override
    public String toString() {
        return "TimeScale{" + "timeFrame=" + timeFrame
                + ", timeWindow=" + timeWindow
                + ", numberOfWindows=" + getNumberOfWindows()
                + '}';
    }

    /**
     * Converts a time window to seconds.
     *
     * @param timeWindow time window that has to be converted in seconds
     * @return a {@link Long} representing the amount of seconds
     */
    public static long getSeconds(TimeWindow timeWindow) {
        switch (timeWindow) {
            case TEN_SECOND:
                return TimeUnit.SECONDS.toSeconds(10);
            case ONE_MIN:
                return TimeUnit.MINUTES.toSeconds(1);
            case TEN_MIN:
                return TimeUnit.MINUTES.toSeconds(10);
            case ONE_HOUR:
                return TimeUnit.HOURS.toSeconds(1);
            case ONE_DAY:
                return TimeUnit.DAYS.toSeconds(1);
            case ONE_WEEK:
                return TimeUnit.DAYS.toSeconds(7);
            default:
                throw new IllegalArgumentException(timeWindow + " is not yet supported");
        }
    }

    /**
     * Converts a time window to seconds.
     *
     * @param timeWindow time window that has to be converted in seconds
     * @return a {@link Long} representing the amount of seconds
     */
    public static TemporalAmount getDuration(TimeWindow timeWindow) {
        switch (timeWindow) {
            case TEN_SECOND:
                return Duration.ofSeconds(10);
            case ONE_MIN:
                return Duration.ofMinutes(1);
            case TEN_MIN:
                return Duration.ofMinutes(10);
            case ONE_HOUR:
                return Duration.ofHours(1);
            case ONE_DAY:
                return Duration.ofDays(1);
            case ONE_WEEK:
                return Duration.ofDays(7);
            default:
                throw new IllegalArgumentException(timeWindow + " is not yet supported");
        }
    }
}
