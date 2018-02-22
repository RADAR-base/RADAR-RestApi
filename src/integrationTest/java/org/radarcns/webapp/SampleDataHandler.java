package org.radarcns.webapp;

public interface SampleDataHandler {

    String PROJECT = "radar";
    String SUBJECT = "sub-1";
    String SOURCE = "03d28e5c-e005-46d4-a9b3-279c27fbbc83";
    String SOURCE_TYPE = "empatica_e4_v1";
    String BATTERY_LEVEL_SOURCE_DATA_NAME = "EMPATICA_E4_v1_BATTERY";
    String ACCELEROMETER_SOURCE_DATA_NAME = "EMPATICA_E4_v1_ACCELEROMETER";
    int SAMPLES = 10;

    String BATTERY_LEVEL_COLLECTION_NAME = "android_empatica_e4_battery_level_output";
    String ACCELERATION_COLLECTION = "android_empatica_e4_acceleration_output";
    String ACCELERATION_COLLECTION_FOR_TEN_MINITES =
            "android_empatica_e4_acceleration_output_10min";
    String BATTERY_LEVEL_COLLECTION_FOR_TEN_MINUTES =
            "android_empatica_e4_battery_level_output_10min";

    String PRODUCER = "Empatica";
    String MODEL = "E4";
    String CATALOGUE_VERSION = "v1";
}
