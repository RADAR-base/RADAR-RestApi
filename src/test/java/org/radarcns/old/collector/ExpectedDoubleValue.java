package org.radarcns.old.collector;

import java.util.HashMap;

/**
 * {@code ExpectedValue} represented as {@code Double}.
 *      @see {@link ExpectedValue}
 */
public class ExpectedDoubleValue extends ExpectedValue<DoubleValueCollector> {

    //private static final Logger logger = LoggerFactory.getLogger(ExpectedDoubleValue.class);

    /**
     * Constructor.
     **/
    public ExpectedDoubleValue() {
        series = new HashMap<>();

        lastTimestamp = 0L;
        lastValue = new DoubleValueCollector();
    }

    /**
     * It adds a new value the simulation taking into account if it belongs to an existing time
     *      window or not.
     * @param startTimeWindow timeZero for a time window that has this sample as initil value
     * @param timestamp time associated with the value
     * @param value sample value
     **/
    public void add(Long startTimeWindow, Long timestamp, double value) {
        if (timestamp < lastTimestamp + DURATION) {
            lastValue.add(value);
        } else {
            lastTimestamp = startTimeWindow;
            lastValue = new DoubleValueCollector();
            lastValue.add(value);
            series.put(startTimeWindow, lastValue);
        }
    }
}
