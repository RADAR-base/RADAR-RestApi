///*
// * Copyright 2016 King's College London and The Hyve
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package org.radarcns.monitor;
//
//import static org.junit.Assert.assertEquals;
//
//import java.util.HashMap;
//import java.util.LinkedList;
//import java.util.List;
//import org.junit.Test;
//import org.radarcns.catalog.SourceDefinition;
//import org.radarcns.catalogue.ProcessingState;
//import org.radarcns.catalogue.Unit;
//import org.radarcns.config.catalog.DeviceItem;
//import org.radarcns.config.catalog.SensorCatalog;
//import org.radarcns.dao.mongo.data.sensor.DataFormat;
//import org.radarcns.restapi.source.States;
//
//public class SourceMonitorTest {
//
//    @Test
//    public void getSourceTest() {
//        List<SensorCatalog> sensors = new LinkedList<>();
//
//        sensors.add(new SensorCatalog("ACCELEROMETER", 32.0, Unit.G, ProcessingState.RAW,
//                DataFormat.ACCELERATION_FORMAT,
//                getCollections("android_empatica_e4_acceleration_output")));
//
//        sensors.add(new SensorCatalog("BATTERY", 1.0, Unit.PERCENTAGE,
//                ProcessingState.RAW, DataFormat.DOUBLE_FORMAT,
//                getCollections("android_empatica_e4_battery_level_output")));
//
//        sensors.add(new SensorCatalog("BLOOD_VOLUME_PULSE", 64.0, Unit.NANO_WATT,
//                ProcessingState.RAW, DataFormat.DOUBLE_FORMAT,
//                getCollections("android_empatica_e4_blood_volume_pulse_output")));
//
//        sensors.add(new SensorCatalog("ELECTRODERMAL_ACTIVITY", 4.0,
//                Unit.MICRO_SIEMENS, ProcessingState.RAW, DataFormat.DOUBLE_FORMAT,
//                getCollections("android_empatica_e4_electrodermal_activity_output")));
//
//        sensors.add(new SensorCatalog("HEART_RATE", 1.0, Unit.BEATS_PER_MIN,
//                ProcessingState.RADAR, DataFormat.DOUBLE_FORMAT,
//                getCollections("android_empatica_e4_heartrate")));
//
//        sensors.add(new SensorCatalog("INTER_BEAT_INTERVAL", 1.0,
//                Unit.BEATS_PER_MIN, ProcessingState.RAW, DataFormat.DOUBLE_FORMAT,
//                getCollections("android_empatica_e4_inter_beat_interval_output")));
//
//        sensors.add(new SensorCatalog("THERMOMETER", 4.0, Unit.CELSIUS,
//                ProcessingState.RAW, DataFormat.DOUBLE_FORMAT,
//                getCollections("android_empatica_e4_inter_beat_interval_output")));
//
//
//        SourceDefinition empatica = new SourceDefinition("EMPATICA",
//                new DeviceItem(sensors));
//
//        SourceMonitor monitor = new SourceMonitor(empatica);
//
//        assertEquals(empatica, monitor.getSource());
//    }
//
//    private HashMap<String, String> getCollections(String prefix) {
//        HashMap<String, String> collections = new HashMap<>();
//
//        collections.put("10sec", prefix);
//        collections.put("1min", prefix + "_1min");
//        collections.put("10min", prefix + "_10min");
//        collections.put("1h", prefix + "_1h");
//        collections.put("1d", prefix + "_1d");
//        collections.put("1w", prefix + "_1w");
//
//        return collections;
//    }
//
//    @Test
//    public void getStatusTest() {
//        assertEquals(States.FINE, SourceMonitor.getStatus(1d));
//        assertEquals(States.FINE, SourceMonitor.getStatus(0.951));
//
//        assertEquals(States.OK, SourceMonitor.getStatus(0.95));
//        assertEquals(States.OK, SourceMonitor.getStatus(0.90));
//        assertEquals(States.OK, SourceMonitor.getStatus(0.801));
//
//        assertEquals(States.WARNING, SourceMonitor.getStatus(0.80));
//        assertEquals(States.WARNING, SourceMonitor.getStatus(0.40));
//        assertEquals(States.WARNING, SourceMonitor.getStatus(0.01));
//
//        assertEquals(States.DISCONNECTED, SourceMonitor.getStatus(0d));
//
//        assertEquals(States.UNKNOWN, SourceMonitor.getStatus(-1d));
//    }
//
//    @Test
//    public void getPercentageTest() {
//        assertEquals(1d, SourceMonitor.getPercentage(100d, 100d),
//                0d);
//        assertEquals(0.95d, SourceMonitor.getPercentage(95d, 100d),
//                0d);
//        assertEquals(0.50d, SourceMonitor.getPercentage(50d, 100d),
//                0d);
//        assertEquals(0.1d, SourceMonitor.getPercentage(10d, 100d),
//                0d);
//        assertEquals(0d, SourceMonitor.getPercentage(0d, 100d),
//                0d);
//    }
//
//}
