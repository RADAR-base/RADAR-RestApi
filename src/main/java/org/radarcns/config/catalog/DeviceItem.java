package org.radarcns.config.catalog;

import java.util.List;

/*
 * Copyright 2016 King's College London and The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class DeviceItem {

    private List<SensorCatalog> sensors;

    public DeviceItem() {
        // POJO initializer
    }

    public DeviceItem(List<SensorCatalog> sensors) {
        this.sensors = sensors;
    }

    public List<SensorCatalog> getSensors() {
        return sensors;
    }

    public void setSensors(List<SensorCatalog> sensors) {
        this.sensors = sensors;
    }

    @Override
    public String toString() {
        return "DeviceItem{" + "sensors=" + sensors + '}';
    }
}
