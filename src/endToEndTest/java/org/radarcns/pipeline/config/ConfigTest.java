package org.radarcns.pipeline.config;

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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import org.junit.Test;
import org.radarcns.config.YamlConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigTest {

    //private static final Logger LOGGER = LoggerFactory.getLogger(ConfigTest.class);

    private static PipelineConfig config = null;

    public static final String PIPELINE_CONFIG = "pipeline.yml";

    @Test
    public void configTest() throws Exception {
        getPipelineConfig();

        assertEquals("http://localhost/api/radar/api", config.getRestApiInstance());
        assertEquals("http://localhost/kafka", config.getRestProxyInstance());
    }

    private static PipelineConfig getPipelineConfig() {
        if (config == null) {

            try {
                config = new YamlConfigLoader().load(
                        new File(
                                ConfigTest.class.getClassLoader()
                                        .getResource(PIPELINE_CONFIG).getFile()
                        ), PipelineConfig.class);
            } catch (IOException exec) {
                exec.printStackTrace();
            }
        }
        return config;
    }
}
