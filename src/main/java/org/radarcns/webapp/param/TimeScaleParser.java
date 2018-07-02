package org.radarcns.webapp.param;

import static org.radarcns.domain.restapi.TimeWindow.ONE_DAY;
import static org.radarcns.domain.restapi.TimeWindow.ONE_HOUR;
import static org.radarcns.domain.restapi.TimeWindow.ONE_MIN;
import static org.radarcns.domain.restapi.TimeWindow.ONE_WEEK;
import static org.radarcns.domain.restapi.TimeWindow.TEN_MIN;
import static org.radarcns.domain.restapi.TimeWindow.TEN_SECOND;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.ws.rs.BadRequestException;

import javax.ws.rs.ext.Provider;
import org.radarcns.domain.restapi.TimeWindow;
import org.radarcns.domain.restapi.header.TimeFrame;
import org.radarcns.util.RadarConverter;
import org.radarcns.webapp.param.InstantParam;

/**
 * This class refines temporal query parameters and calculate new values when necessary.
 * <p>
 * If the tartTime is not provided startTime will be calculated based on default number of windows
 * and given timeWindow.
 * If no timeWindow is provided, a best fitting timeWindow will be calculated.
 * If none of the parameters are provided, API will return data for a period of 1 year with
 * ONE_WEEK of timeWindow (~52 records) from current timestamp.
 * </p>
 */
public class TimeScaleParser {
    private static final int DEFAULT_NUMBER_OF_WINDOWS = 100;
    public static final int MAX_NUMBER_OF_WINDOWS = 5000;

    private static final List<Map.Entry<TimeWindow, Double>> TIME_WINDOW_LOG = Stream
            .of(TEN_SECOND, ONE_MIN, TEN_MIN, ONE_HOUR, ONE_DAY, ONE_WEEK)
            .map(w -> pair(w, Math.log(RadarConverter.getSecond(w))))
            .collect(Collectors.toList());

    private final int defaultNumberOfWindows;
    private final int maxNumberOfWindows;

    public TimeScaleParser() {
        this(DEFAULT_NUMBER_OF_WINDOWS, MAX_NUMBER_OF_WINDOWS);
    }

    public TimeScaleParser(int defaultNumberOfWindows, int maxNumberOfWindows) {
        this.defaultNumberOfWindows = defaultNumberOfWindows;
        this.maxNumberOfWindows = maxNumberOfWindows;
    }

    /**
     * Parse a set of start and end time and a time window. Use default values where necessary.
     * The end time defaults to the current moment. If not provided, the other parameters will be
     * deduced to get a useful set of time windows back. If no values are provided, the default time
     * frame is one year of one week intervals.
     *
     * @param startTimeParam parameter possibly containing startTime.
     * @param endTimeParam parameter possibly containing endTime.
     * @param timeWindow timeWindow of the query.
     * @throws BadRequestException if the start time is after the end time or if the number of
     *                             windows requested exceeds the maximum number of windows.
     */
    public TimeScale parse(InstantParam startTimeParam, InstantParam endTimeParam,
            final TimeWindow timeWindow) {

        Instant startTime = startTimeParam != null ? startTimeParam.getValue() : null;
        Instant endTime = endTimeParam != null ? endTimeParam.getValue() : Instant.now();

        if (startTime != null && startTime.isAfter(endTime)) {
            throw new BadRequestException("startTime " + startTime
                    + " should not be after endTime " + endTime);
        }

        TimeFrame timeFrame;
        TimeWindow newTimeWindow = timeWindow;

        if (startTime == null && timeWindow == null) {
            // default settings, 1 year with 1 week intervals
            timeFrame = new TimeFrame(endTime.minus(1, ChronoUnit.YEARS), endTime);
            newTimeWindow = ONE_WEEK;
        } else if (startTime == null) {
            // use a fixed number of windows.
            long totalSeconds = RadarConverter.getSecond(timeWindow) * defaultNumberOfWindows;
            timeFrame = new TimeFrame(endTime.minus(totalSeconds, ChronoUnit.SECONDS), endTime);
        } else if (timeWindow == null) {
            // use the fixed time frame with a time window close to the default number of windows.
            timeFrame = new TimeFrame(startTime, endTime);
            newTimeWindow = getFittingTimeWindow(timeFrame);
        } else {
            // all params are provided in request
            timeFrame = new TimeFrame(startTime, endTime);
        }

        long numberOfWindowsRequested = (long) Math.floor(timeFrame.getDuration().getSeconds()
                / (double) RadarConverter.getSecond(newTimeWindow));

        if (numberOfWindowsRequested > MAX_NUMBER_OF_WINDOWS) {
            throw new BadRequestException("Cannot request more than " + maxNumberOfWindows
                    + " time windows using time frame " + timeFrame + " and time window "
                    + newTimeWindow + ". Queried " + numberOfWindowsRequested + " instead.");
        }

        return new TimeScale(timeFrame, newTimeWindow);
    }

    /**
     * Get the time window that closest matches given time frame.
     *
     * @param timeFrame time frame to compute time window for
     * @return closest match with given time frame.
     */
    private TimeWindow getFittingTimeWindow(TimeFrame timeFrame) {
        double logSeconds = Math.log(timeFrame.getDuration().getSeconds() / defaultNumberOfWindows);
        return TIME_WINDOW_LOG.stream()
                .map(e -> pair(e.getKey(), Math.abs(logSeconds - e.getValue())))
                .reduce((e1, e2) -> e1.getValue() < e2.getValue() ? e1 : e2)
                .orElseThrow(() -> new AssertionError("No close time window found"))
                .getKey();
    }

    private static <K, V> Map.Entry<K, V> pair(K key, V value) {
        return new AbstractMap.SimpleImmutableEntry<>(key, value);
    }

    public static class TimeScale {
        private final TimeFrame timeFrame;
        private final TimeWindow timeWindow;

        private TimeScale(TimeFrame timeFrame, TimeWindow timeWindow) {
            this.timeFrame = timeFrame;
            this.timeWindow = timeWindow;
        }

        public TimeFrame getTimeFrame() {
            return timeFrame;
        }

        public TimeWindow getTimeWindow() {
            return timeWindow;
        }
    }
}
