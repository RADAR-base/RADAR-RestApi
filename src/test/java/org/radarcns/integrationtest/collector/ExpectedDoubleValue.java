package org.radarcns.integrationtest.collector;

import java.util.HashMap;

/**
 * Created by francesco on 14/02/2017.
 */
public class ExpectedDoubleValue {

    //Timewindow length in milliseconds
    private long DURATION = 10000;

    private Long lastTimestamp;
    private DoubleValueCollector lastValue;
    private final HashMap<Long, DoubleValueCollector> series;

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

    public String toString(){
        String result = "";
        for ( Long interval : series.keySet() ){
            result += interval.toString() + "-" + (interval + DURATION) + '\t' +
                series.get(interval).toString() + "\n";
        }
        return result;
    }

}
