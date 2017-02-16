package org.radarcns.integrationtest.collector;

import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by francesco on 14/02/2017.
 */
public class ExpectedDoubleValue extends ExpectedValue<DoubleValueCollector>{

    private static final Logger logger = LoggerFactory.getLogger(ExpectedDoubleValue.class);

    public ExpectedDoubleValue(){
        series = new HashMap<>();

        lastTimestamp = 0L;
        lastValue = new DoubleValueCollector();
    }

    public void add(Long startTimeWindow, Long timestamp, double value){
        if ( timestamp < lastTimestamp + DURATION ) {
            lastValue.add(value);
        }
        else {
            lastTimestamp = timestamp;
            lastValue = new DoubleValueCollector();
            lastValue.add(value);
            series.put(startTimeWindow, lastValue);
        }
    }
}
