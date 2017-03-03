package org.radarcns.old.cvs;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import org.radarcns.security.Param;


/**
 * Generic CSV sensor definition.
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public abstract class CSVSensor {
    private List<String> headers;
    private String user;
    private String source;
    private Double currentTime;

    //private static final Logger logger = LoggerFactory.getLogger(CSVSensor.class);

    /**
     * Constructor.
     * @param headers {@code List<String>} containing the fields name that has to be generated
     */
    public CSVSensor(List<String> headers) {
        this(headers, null, null,
            null);
    }

    /**
     * Constructor.
     * @param headers {@code List<String>} containing the fields name that has to be generated
     * @param user user identifier
     * @param source source identifier
     * @param timeZero initial instant used to compute all needed instants
     */
    public CSVSensor(List<String> headers, String user, String source, Long timeZero) {
        this.headers = headers;
        this.user = user;
        this.source = source;

        if (timeZero == null) {
            this.currentTime = ( System.currentTimeMillis()
                - TimeUnit.DAYS.toMillis(2) ) / 1000d;
        } else {
            this.currentTime = timeZero / 1000d;
        }

        if (Param.isNullOrEmpty(this.user)) {
            this.user = "UserID_0";
        }

        if (Param.isNullOrEmpty(this.source)) {
            this.source = "SourceID_0";
        }
    }

    public String getUser() {
        return user;
    }

    public String getSource() {
        return source;
    }

    public Double getCurrentTime() {
        return currentTime;
    }

    /**
     * Increment the current time.
     **/
    public void incCurrentTime() {
        this.currentTime = getRandomIncTimestamp(currentTime);
    }

    /**
     * @return a comma separated {@code String} containing all header names.
     **/
    public String getHeaders() {
        String result = "";
        for (String header : headers) {
            result += header + ",";
        }

        return result.substring(0, result.length() - 1) + '\n';
    }

    public abstract String nextValue();

    /**
     * @return random {@code Double} using {@code ThreadLocalRandom}.
     * @see {@link java.util.concurrent.ThreadLocalRandom}
     **/
    public static Double getRandomDouble() {
        return ThreadLocalRandom.current().nextDouble();
    }

    /**
     * It returns a random {@code Double} between min and max.
     * @param min range lower bound
     * @param max range upper bound
     * @return random {@code Double} using {@code ThreadLocalRandom}
     * @see {@link java.util.concurrent.ThreadLocalRandom}
     **/
    public static Double getRandomDouble(double min, double max) {
        return ThreadLocalRandom.current().nextDouble(min , max);
    }

    /**
     * It returns the next random timestamp incrementing the input time.
     * @param currentTime time zero
     * @return random {@code Double} representig the next timestamp using {@code ThreadLocalRandom}
     * @see {@link java.util.concurrent.ThreadLocalRandom}
     **/
    public Double getRandomIncTimestamp(Double currentTime) {
        return currentTime + ( ThreadLocalRandom.current().nextDouble(10.0 ,
            TimeUnit.MINUTES.toMillis(1)) / 1000d );
    }

    /**
     * It returns the a random Round Trip Time for the given in input time.
     * @param timeReceived time at which the message has been received
     * @return random {@code Double} representig the Round Trip Time for the given timestamp
     *      using {@code ThreadLocalRandom}
     * @see {@link java.util.concurrent.ThreadLocalRandom}
     **/
    public Double getRandomRoundTripTime(Double timeReceived) {
        Double value = timeReceived - ( ThreadLocalRandom.current().nextDouble(1.0 ,
                TimeUnit.SECONDS.toMillis(1)) / 1000d );

        if (value < 0.0) {
            return timeReceived;
        }

        return value;
    }
}
