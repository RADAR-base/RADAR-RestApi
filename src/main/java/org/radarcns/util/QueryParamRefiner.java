package org.radarcns.util;

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

import org.radarcns.domain.restapi.TimeWindow;
import org.radarcns.domain.restapi.header.TimeFrame;


/**
 * This class refines query parameters and calculate new values when necessary.
 * <p>
 * If the tartTime is not provided startTime will be calculated based on default number of windows
 * and given timeWindow.
 * If no timeWindow is provided, a best fitting timeWindow will be calculated.
 * If none of the parameters are provided, API will return data for a period of 1 year with
 * ONE_WEEK of timeWindow (~52 records) from current timestamp.
 * </p>
 */
public class QueryParamRefiner {
    private static final int DEFAULT_NUMBER_OF_WINDOWS = 100;

    private static final List<Map.Entry<TimeWindow, Double>> TIME_WINDOW_LOG = Stream
            .of(TEN_SECOND, ONE_MIN, TEN_MIN, ONE_HOUR, ONE_DAY, ONE_WEEK)
            .map(w -> pair(w, Math.log(RadarConverter.getSecond(w))))
            .collect(Collectors.toList());

    private TimeFrame timeFrame;

    private TimeWindow timeWindow;

    /**
     * Default constructor.
     *
     * @param startTime startTime as Instant.
     * @param endTime endTime of the query as Instant.
     * @param timeWindow timeWindow of the query.
     */
    public QueryParamRefiner(Instant startTime, Instant endTime, TimeWindow
            timeWindow) {

        if (startTime != null && startTime.isAfter(endTime)) {
            throw new BadRequestException(String.format("startTime {} should not be after endTime"
                    + " {}", startTime, endTime));
        } else if (startTime == null && timeWindow == null) {
            // default settings, 1 year with 1 week intervals
            this.timeFrame = new TimeFrame(endTime.minus(365, ChronoUnit.DAYS), endTime);
            this.timeWindow = ONE_WEEK;
        } else if (startTime == null) {
            // use a fixed number of windows.
            this.timeFrame = new TimeFrame(endTime.minus(
                    RadarConverter.getSecond(timeWindow) * DEFAULT_NUMBER_OF_WINDOWS,
                    ChronoUnit.SECONDS), endTime);
            this.timeWindow = timeWindow;
        } else if (timeWindow == null) {
            // use the fixed time frame with a time window close to the default number of windows.
            this.timeFrame = new TimeFrame(startTime, endTime);
            this.timeWindow = getFittingTimeWindow(this.timeFrame, DEFAULT_NUMBER_OF_WINDOWS);

        } else {
            // all params are provided in request
            this.timeFrame = new TimeFrame(startTime, endTime);
            this.timeWindow = timeWindow;
        }
    }

    public TimeFrame getTimeFrame() {
        return timeFrame;
    }

    public TimeWindow getTimeWindow() {
        return timeWindow;
    }

    /**
     * Get the time window that closest matches given time frame.
     *
     * @param timeFrame time frame to compute time window for
     * @param numberOfWindows number of time windows that should ideally be returned.
     * @return closest match with given time frame.
     */
    private TimeWindow getFittingTimeWindow(TimeFrame timeFrame, int numberOfWindows) {
        double logSeconds = Math.log(timeFrame.getDuration().getSeconds() / numberOfWindows);
        return TIME_WINDOW_LOG.stream()
                .map(e -> pair(e.getKey(), Math.abs(logSeconds - e.getValue())))
                .reduce((e1, e2) -> e1.getValue() < e2.getValue() ? e1 : e2)
                .orElseThrow(() -> new AssertionError("No close time window found"))
                .getKey();
    }

    private static <K, V> Map.Entry<K, V> pair(K key, V value) {
        return new AbstractMap.SimpleImmutableEntry<>(key, value);
    }
}
