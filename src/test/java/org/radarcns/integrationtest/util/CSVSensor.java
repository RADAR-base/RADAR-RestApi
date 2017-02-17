package org.radarcns.integrationtest.util;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.radarcns.security.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by francesco on 17/02/2017.
 */
public abstract class CSVSensor {
    private List<String> headers;
    private String userID;
    private String sourceID;
    private Double currentTime;

    private static final Logger logger = LoggerFactory.getLogger(CSVSensor.class);

    public CSVSensor(List<String> headers) {
        this(headers, null, null,
            null);
    }

    public CSVSensor(List<String> headers, String userID, String sourceID, Long timeZero) {
        this.headers = headers;
        this.userID = userID;
        this.sourceID = sourceID;

        if ( timeZero == null ) {
            this.currentTime = ( System.currentTimeMillis() -
                TimeUnit.DAYS.toMillis(2) ) / 1000d;
        } else {
            this.currentTime = timeZero / 1000d;
        }

        if ( Param.isNullOrEmpty(this.userID) ) {
            this.userID = "UserID_0";
        }

        if ( Param.isNullOrEmpty(this.sourceID) ) {
            this.sourceID = "SourceID_0";
        }
    }

    public String getUserID() {
        return userID;
    }

    public String getSourceID() {
        return sourceID;
    }

    public Double getCurrentTime() {
        return currentTime;
    }

    public void incCurrentTime() {
        this.currentTime = getRandomIncTimestamp(currentTime);
    }

    public String getHeaders(){
        return headers.stream().collect(
            Collectors.joining(",")) + "\n";
    }

    abstract String nextValue();

    public static Double getRandomDouble(){
        return ThreadLocalRandom.current().nextDouble();
    }

    public static Double getRandomDouble(double min, double max){
        return ThreadLocalRandom.current().nextDouble(min , max);
    }

    public Double getRandomIncTimestamp(Double currentTime){
        return currentTime + ( ThreadLocalRandom.current().nextDouble(10.0 ,
            TimeUnit.MINUTES.toMillis(1)) / 1000d );
    }

    public Double getRandomRTT(Double timeReceived){
        Double value = timeReceived - ( ThreadLocalRandom.current().nextDouble(1.0 ,
            TimeUnit.SECONDS.toMillis(1)) / 1000d );

        if (value < 0.0) {
            return timeReceived;
        }

        return value;
    }
}
