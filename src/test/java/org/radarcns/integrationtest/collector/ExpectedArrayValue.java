package org.radarcns.integrationtest.collector;

import java.util.HashMap;

/**
 * Created by francesco on 14/02/2017.
 */
public class ExpectedArrayValue {

    //Timewindow length in milliseconds
    private long DURATION = 10000;

    private Long lastTimestamp;
    private DoubleArrayCollector lastValue;
    private final HashMap<Long, DoubleArrayCollector> series;

    public ExpectedArrayValue(){
        series = new HashMap<>();

        lastTimestamp = 0L;
        lastValue = new DoubleArrayCollector();
    }

    public void add(Long startTimeWindow, Long timestamp, Double[] array){
        double[] temp = new double[array.length];

        for (int i=0; i<array.length; i++){
            temp[i] = array[i];
        }

        add(startTimeWindow, timestamp, temp);
    }

    public void add(Long startTimeWindow, Long timestamp, double[] array){
        if ( timestamp < lastTimestamp + DURATION ) {
            lastValue.add(array);
        }
        else {
            lastTimestamp = timestamp;
            lastValue = new DoubleArrayCollector();
            lastValue.add(array);
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
