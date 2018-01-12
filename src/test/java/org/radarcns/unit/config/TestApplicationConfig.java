package org.radarcns.unit.config;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import org.junit.Test;
import org.radarcns.config.ApplicationConfig;
import org.radarcns.config.Properties;
import org.radarcns.config.YamlConfigLoader;

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
public class TestApplicationConfig {

    @Test
    public void loadApiConfigOk() throws IOException {
        assertEquals("device-catalog.yml", Properties.getApiConfig().getDeviceCatalog());
    }


    @Test(expected = com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException.class)
    public void readCatalogKoUnit() throws IOException {
        new YamlConfigLoader().load(new File(
                        TestApplicationConfig.class.getClassLoader()
                                .getResource("radar_dev_catalog_ko.yml").getFile()),
                ApplicationConfig.class);
    }

}
