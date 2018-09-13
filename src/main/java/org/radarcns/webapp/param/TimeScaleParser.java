package org.radarcns.webapp.param;

import static org.radarcns.domain.restapi.TimeWindow.ONE_DAY;
import static org.radarcns.domain.restapi.TimeWindow.ONE_HOUR;
import static org.radarcns.domain.restapi.TimeWindow.ONE_MIN;
import static org.radarcns.domain.restapi.TimeWindow.ONE_WEEK;
import static org.radarcns.domain.restapi.TimeWindow.TEN_MIN;
import static org.radarcns.domain.restapi.TimeWindow.TEN_SECOND;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.BadRequestException;
import org.radarcns.domain.restapi.TimeWindow;
import org.radarcns.domain.restapi.header.TimeFrame;
import org.radarcns.util.RadarConverter;
import org.radarcns.util.TimeScale;

/**
 * Parser for temporal query parameters, calculating new values when necessary.
 * <p>
 * If the startTime is not provided startTime will be calculated based on default number of windows
 * and given timeWindow.
 * If no timeWindow is provided, a best fitting timeWindow will be calculated.
 * If none of the parameters are provided, API will return data for a period of 1 year with
 * ONE_WEEK of timeWindow (~52 records) from current timestamp.
 * </p>
 */
public class TimeScaleParser {
    public static final int DEFAULT_NUMBER_OF_WINDOWS = 100;
    public static final int MAX_NUMBER_OF_WINDOWS = 1000;

    private static final List<Map.Entry<TimeWindow, Double>> TIME_WINDOW_LOG = Stream
            .of(TEN_SECOND, ONE_MIN, TEN_MIN, ONE_HOUR, ONE_DAY, ONE_WEEK)
            .map(w -> RadarConverter.pair(w, Math.log(TimeScale.getSeconds(w))))
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

        TimeScale timeScale = parseWithDefaults(startTime, endTime, timeWindow);

        if (timeScale.getNumberOfWindows() > maxNumberOfWindows) {
            throw new BadRequestException("Cannot request more than " + maxNumberOfWindows
                    + " time windows using " + timeScale + ".");
        }

        return timeScale;
    }

    private TimeScale parseWithDefaults(@Nullable Instant startTime, @Nonnull Instant endTime,
            @Nullable TimeWindow timeWindow) {

        TimeFrame timeFrame;
        if (startTime != null) {
            timeFrame = new TimeFrame(startTime, endTime);
        } else if (timeWindow != null) {
            long totalSeconds = TimeScale.getSeconds(timeWindow) * defaultNumberOfWindows;
            timeFrame = new TimeFrame(endTime.minus(totalSeconds, ChronoUnit.SECONDS), endTime);
        } else {
            timeFrame = new TimeFrame(endTime.minus(365, ChronoUnit.DAYS), endTime);
        }

        return new TimeScale(timeFrame,
                timeWindow != null ? timeWindow : getFittingTimeWindow(timeFrame));
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
                .map(e -> RadarConverter.pair(e.getKey(), Math.abs(logSeconds - e.getValue())))
                .min(Comparator.comparing(Entry::getValue))
                .orElseThrow(() -> new AssertionError("No close time window found"))
                .getKey();
    }
}
