package org.radarcns.integrationtest.cvs;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import org.radarcns.avro.restapi.dataset.Dataset;
import org.radarcns.avro.restapi.header.DescriptiveStatistic;
import org.radarcns.avro.restapi.header.Unit;
import org.radarcns.integrationtest.config.MockDataConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by francesco on 17/02/2017.
 */
public class CSVGenerator {

    //private static final Logger logger = LoggerFactory.getLogger(CSVGenerator.class);

    public static void acceleration(int samples, String path) throws IOException {
        acceleration(null, null, null, samples, path);
    }

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
        }, user, source, timeZero){
            @Override
            public String nextValue() {
                String sample = getUserID() + "," + getSourceID()+ "," +
                    getRandomRTT(getCurrentTime()).toString() + "," + getCurrentTime().toString()
                    + "," + getRandomDouble(0.0, 2.0) + "," + getRandomDouble(0.0, 2.0) + ","
                    + getRandomDouble(0.0, 2.0) + "\n";

                incCurrentTime();

                return sample;
            }
        };

        writeFile(accelerator, samples, path);
    }

    public static void battery(int samples, String path) throws IOException {
        battery(null, null, null, samples, path);
    }

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
        }, user, source, timeZero){

            private double batteryDecayFactor = 0.1f * getRandomDouble();
            private double count = 1;
            @Override
            public String nextValue() {
                double batteryLevel = 1d - (batteryDecayFactor * count % 1);
                count ++;

                String sample = getUserID() + "," + getSourceID()+ "," +
                    getRandomRTT(getCurrentTime()).toString() + "," + getCurrentTime().toString()
                    + "," + batteryLevel + "\n";

                incCurrentTime();

                return sample;
            }
        };

        writeFile(battery, samples, path);
    }

    public static void bloodVolumePulse(int samples, String path) throws IOException {
        bloodVolumePulse(null, null, null, samples, path);
    }

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
        }, user, source, timeZero){
            @Override
            public String nextValue() {
                String sample = getUserID() + "," + getSourceID()+ "," +
                    getRandomRTT(getCurrentTime()).toString() + "," + getCurrentTime().toString()
                    + "," + getRandomDouble(60.0, 90.0) + "\n";

                incCurrentTime();

                return sample;
            }
        };

        writeFile(bloodVolumePulse, samples, path);
    }

    public static void electrodermalActivty (int samples, String path) throws IOException {
        electrodermalActivty(null, null, null, samples, path);
    }

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
        }, user, source, timeZero){
            @Override
            public String nextValue() {
                String sample = getUserID() + "," + getSourceID()+ "," +
                    getRandomRTT(getCurrentTime()).toString() + "," + getCurrentTime().toString()
                    + "," + getRandomDouble(0.01, 0.05) + "\n";

                incCurrentTime();

                return sample;
            }
        };

        writeFile(electrodermalActivty, samples, path);
    }

    public static void interBeatInterval (int samples, String path) throws IOException {
        interBeatInterval(null, null, null, samples, path);
    }

    public static void interBeatInterval (String user, String source, Long timeZero, int samples,
        String path) throws IOException {
        CSVSensor interBeatInterval = new CSVSensor( new ArrayList<String>() {
            {
                add("userId");
                add("sourceId");
                add("time");
                add("timeReceived");
                add("interBeatInterval");
            }
        }, user, source, timeZero){
            @Override
            public String nextValue() {
                String sample = getUserID() + "," + getSourceID()+ "," +
                    getRandomRTT(getCurrentTime()).toString() + "," + getCurrentTime().toString()
                    + "," + getRandomDouble(55.0, 120.0) + "\n";

                incCurrentTime();

                return sample;
            }
        };

        writeFile(interBeatInterval, samples, path);
    }

    public static void temperature (int samples, String path) throws IOException {
        temperature(null, null, null, samples, path);
    }

    public static void temperature (String user, String source, Long timeZero, int samples,
        String path) throws IOException {
        CSVSensor temperature = new CSVSensor( new ArrayList<String>() {
            {
                add("userId");
                add("sourceId");
                add("time");
                add("timeReceived");
                add("temperature");
            }
        }, user, source, timeZero){
            @Override
            public String nextValue() {
                String sample = getUserID() + "," + getSourceID()+ "," +
                    getRandomRTT(getCurrentTime()).toString() + "," + getCurrentTime().toString()
                    + "," + getRandomDouble(36.5, 37.0) + "\n";

                incCurrentTime();

                return sample;
            }
        };

        writeFile(temperature, samples, path);
    }

    public static void writeFile(CSVSensor generator, int samples, String path)
        throws IOException {

        try (FileWriter writer = new FileWriter(path, false)) {
            writer.write(generator.getHeaders());

            for ( int i=0 ; i<samples; i++ ) {
                writer.write(generator.nextValue());
            }

            writer.flush();
            writer.close();
        }
    }

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
            //
        } else if (config.getRestCall().contains("/IBI/")) {
            interBeatInterval(samples, config.getDataFile());
        } else if (config.getRestCall().contains("/T/")) {
            temperature(samples, config.getDataFile());
        } else {
            throw new IllegalArgumentException(config.getRestCall() +
                " is not a supported test case");
        }
    }

}
