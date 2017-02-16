package org.radarcns.integrationtest.collector;


import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by francesco on 14/02/2017.
 */
public class ExpectedArrayValue extends ExpectedValue<DoubleArrayCollector>{

    private static final Logger logger = LoggerFactory.getLogger(ExpectedArrayValue.class);

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

}
