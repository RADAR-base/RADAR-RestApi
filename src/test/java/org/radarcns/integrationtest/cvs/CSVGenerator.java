package org.radarcns.integrationtest.cvs;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import org.radarcns.integrationtest.config.MockDataConfig;

/**
 * It generates a CVS file that can be used by both MockDevice and MockAggregator to stream data and
 *      to compute the expected results.
 * @see {@link org.radarcns.integrationtest.util.MockAggregator}
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public class CSVGenerator {

    //private static final Logger logger = LoggerFactory.getLogger(CSVGenerator.class);

    /**
     * It generates a CSV file simulating an accelerometer sensor.
     * @param samples the number of samples that have to be generated
     * @param path where the file has to be created
     **/
    public static void acceleration(int samples, String path) throws IOException {
        acceleration(null, null, null, samples, path);
    }

    /**
     * It generates a CSV file simulating an accelerometer sensor.
     * @param user user identifier
     * @param source source identifier
     * @param timeZero initial instant used to compute all needed instants
     * @param samples the number of samples that have to be generated
     * @param path where the file has to be created
     **/
    public static void acceleration(String user, String source, Long timeZero, int samples,
            String path) throws IOException {
        CSVSensor accelerator = new CSVSensor( new ArrayList<String>() {
            {
                add("userId");
                add("sourceId");
                add("time");
                add("timeReceived");
                add("x");
                add("y");
                add("z");
            }
        }, user, source, timeZero) {
            @Override
            public String nextValue() {
                String sample = getUser() + "," + getSource() + ","
                            + getRandomRoundTripTime(getCurrentTime()).toString() + ","
                            + getCurrentTime().toString()
                            + "," + getRandomDouble(0.0, 2.0) + ","
                            + getRandomDouble(0.0, 2.0) + "," + getRandomDouble(0.0, 2.0) + "\n";

                incCurrentTime();

                return sample;
            }
        };

        writeFile(accelerator, samples, path);
    }

    /**
     * It generates a CSV file simulating battery life decay.
     * @param samples the number of samples that have to be generated
     * @param path where the file has to be created
     **/
    public static void battery(int samples, String path) throws IOException {
        battery(null, null, null, samples, path);
    }

    /**
     * It generates a CSV file simulating battery life decay.
     * @param user user identifier
     * @param source source identifier
     * @param timeZero initial instant used to compute all needed instants
     * @param samples the number of samples that have to be generated
     * @param path where the file has to be created
     **/
    public static void battery(String user, String source, Long timeZero, int samples,
            String path) throws IOException {
        CSVSensor battery = new CSVSensor( new ArrayList<String>() {
            {
                add("userId");
                add("sourceId");
                add("time");
                add("timeReceived");
                add("batteryLevel");
            }
        }, user, source, timeZero) {

            private double batteryDecayFactor = 0.1f * getRandomDouble();
            private double count = 1;
            @Override
            public String nextValue() {
                double batteryLevel = 1d - (batteryDecayFactor * count % 1);
                count ++;

                String sample = getUser() + "," + getSource() + ","
                        + getRandomRoundTripTime(getCurrentTime()).toString() + ","
                        + getCurrentTime().toString()
                        + "," + batteryLevel + "\n";

                incCurrentTime();

                return sample;
            }
        };

        writeFile(battery, samples, path);
    }

    /**
     * It generates a CSV file simulating Photoplethysmograph data.
     * @param samples the number of samples that have to be generated
     * @param path where the file has to be created
     **/
    public static void bloodVolumePulse(int samples, String path) throws IOException {
        bloodVolumePulse(null, null, null, samples, path);
    }

    /**
     * It generates a CSV file simulating Photoplethysmograph data.
     * @param user user identifier
     * @param source source identifier
     * @param timeZero initial instant used to compute all needed instants
     * @param samples the number of samples that have to be generated
     * @param path where the file has to be created
     **/
    public static void bloodVolumePulse(String user, String source, Long timeZero, int samples,
            String path) throws IOException {
        CSVSensor bloodVolumePulse = new CSVSensor( new ArrayList<String>() {
            {
                add("userId");
                add("sourceId");
                add("time");
                add("timeReceived");
                add("bloodVolumePulse");
            }
        }, user, source, timeZero) {
            @Override
            public String nextValue() {
                String sample = getUser() + "," + getSource() + ","
                        + getRandomRoundTripTime(getCurrentTime()).toString() + ","
                        + getCurrentTime().toString() + "," + getRandomDouble(60.0, 90.0) + "\n";

                incCurrentTime();

                return sample;
            }
        };

        writeFile(bloodVolumePulse, samples, path);
    }

    /**
     * It generates a CSV file simulatin ggalvanic skin response sensor data.
     * @param samples the number of samples that have to be generated
     * @param path where the file has to be created
     **/
    public static void electrodermalActivty(int samples, String path) throws IOException {
        electrodermalActivty(null, null, null, samples, path);
    }

    /**
     * It generates a CSV file simulatin ggalvanic skin response sensor data.
     * @param user user identifier
     * @param source source identifier
     * @param timeZero initial instant used to compute all needed instants
     * @param samples the number of samples that have to be generated
     * @param path where the file has to be created
     **/
    public static void electrodermalActivty(String user, String source, Long timeZero, int samples,
            String path) throws IOException {
        CSVSensor electrodermalActivty = new CSVSensor( new ArrayList<String>() {
            {
                add("userId");
                add("sourceId");
                add("time");
                add("timeReceived");
                add("electroDermalActivity");
            }
        }, user, source, timeZero) {
            @Override
            public String nextValue() {
                String sample = getUser() + "," + getSource() + ","
                        + getRandomRoundTripTime(getCurrentTime()).toString() + ","
                        + getCurrentTime().toString() + "," + getRandomDouble(0.01, 0.05) + "\n";

                incCurrentTime();

                return sample;
            }
        };

        writeFile(electrodermalActivty, samples, path);
    }

    /**
     * It generates a CSV file simulating inter beat interval data.
     * @param samples the number of samples that have to be generated
     * @param path where the file has to be created
     **/
    public static void interBeatInterval(int samples, String path) throws IOException {
        interBeatInterval(null, null, null, samples, path);
    }

    /**
     * It generates a CSV file simulating inter beat interval data.
     * @param user user identifier
     * @param source source identifier
     * @param timeZero initial instant used to compute all needed instants
     * @param samples the number of samples that have to be generated
     * @param path where the file has to be created
     **/
    public static void interBeatInterval(String user, String source, Long timeZero, int samples,
            String path) throws IOException {
        CSVSensor interBeatInterval = new CSVSensor( new ArrayList<String>() {
            {
                add("userId");
                add("sourceId");
                add("time");
                add("timeReceived");
                add("interBeatInterval");
            }
        }, user, source, timeZero) {
            @Override
            public String nextValue() {
                String sample = getUser() + "," + getSource() + ","
                        + getRandomRoundTripTime(getCurrentTime()).toString() + ","
                        + getCurrentTime().toString() + "," + getRandomDouble(55.0, 120.0) + "\n";

                incCurrentTime();

                return sample;
            }
        };

        writeFile(interBeatInterval, samples, path);
    }

    /**
     * It generates a CSV file simulating a thermometer sensor.
     * @param samples the number of samples that have to be generated
     * @param path where the file has to be created
     **/
    public static void temperature(int samples, String path) throws IOException {
        temperature(null, null, null, samples, path);
    }

    /**
     * It generates a CSV file simulating a thermometer sensor.
     * @param user user identifier
     * @param source source identifier
     * @param timeZero initial instant used to compute all needed instants
     * @param samples the number of samples that have to be generated
     * @param path where the file has to be created
     **/
    public static void temperature(String user, String source, Long timeZero, int samples,
            String path) throws IOException {
        CSVSensor temperature = new CSVSensor( new ArrayList<String>() {
            {
                add("userId");
                add("sourceId");
                add("time");
                add("timeReceived");
                add("temperature");
            }
        }, user, source, timeZero) {
            @Override
            public String nextValue() {
                String sample = getUser() + "," + getSource() + ","
                        + getRandomRoundTripTime(getCurrentTime()).toString() + ","
                        + getCurrentTime().toString() + "," + getRandomDouble(36.5, 37.0) + "\n";

                incCurrentTime();

                return sample;
            }
        };

        writeFile(temperature, samples, path);
    }

    /**
     * It writes a CSV file.
     * @param generator cvs sample
     * @param samples the number of samples that have to be generated
     * @param path where the file has to be created
     **/
    public static void writeFile(CSVSensor generator, int samples, String path)
        throws IOException {

        try (FileWriter writer = new FileWriter(path, false)) {
            writer.write(generator.getHeaders());

            for (int i = 0 ; i < samples; i++) {
                writer.write(generator.nextValue());
            }

            writer.flush();
            writer.close();
        }
    }

    /**
     * According to the REST function has to be tested, this function invokes the correct CSVSensor
     *      @see {@link org.radarcns.integrationtest.cvs.CSVSensor}.
     * @param config configuration information
     * @param samples the number of samples that have to be generated
     **/
    public static void generate(MockDataConfig config, int samples) throws IOException {
        if (config.getRestCall().contains("/Acc/")) {
            acceleration(samples, config.getDataFile());
        } else if (config.getRestCall().contains("/B/")) {
            battery(samples, config.getDataFile());
        } else if (config.getRestCall().contains("/BVP/")) {
            bloodVolumePulse(samples, config.getDataFile());
        } else if (config.getRestCall().contains("/EDA/")) {
            electrodermalActivty(samples, config.getDataFile());
        } else if (config.getRestCall().contains("/HR/")) {
            throw new IllegalArgumentException(config.getRestCall()
                + " is not a supported test case");
        } else if (config.getRestCall().contains("/IBI/")) {
            interBeatInterval(samples, config.getDataFile());
        } else if (config.getRestCall().contains("/T/")) {
            temperature(samples, config.getDataFile());
        } else {
            throw new IllegalArgumentException(config.getRestCall()
                + " is not a supported test case");
        }
    }

}
