package org.radarcns.unit.config;

import java.io.File;
import java.io.IOException;
import org.junit.Test;
import org.radarcns.config.catalog.DeviceCatalog;

/*
 *  Copyright 2016 King's College London and The Hyve
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
public class TestCatalog {

    @Test
    public void readCatalogOk() throws IOException {
        DeviceCatalog.load(new File(
                TestCatalog.class.getClassLoader()
                .getResource("device-catalog_ok.yml").getFile()
        ));
    }

    @Test(expected = com.fasterxml.jackson.databind.exc.InvalidFormatException.class)
    public void readCatalogKoUnit() throws IOException {
        DeviceCatalog.load(new File(
                TestCatalog.class.getClassLoader()
                .getResource("device-catalog_ko_unit.yml").getFile()
        ));
    }

    @Test(expected = com.fasterxml.jackson.databind.exc.InvalidFormatException.class)
    public void readCatalogKoDevice() throws IOException {
        DeviceCatalog.load(new File(
                    TestCatalog.class.getClassLoader()
                    .getResource("device-catalog_ko_device.yml").getFile()
        ));
    }

    @Test(expected = com.fasterxml.jackson.databind.exc.InvalidFormatException.class)
    public void readCatalogKoSensor() throws IOException {
        DeviceCatalog.load(new File(
                TestCatalog.class.getClassLoader()
                .getResource("device-catalog_ko_sensor.yml").getFile()
        ));
    }

}
