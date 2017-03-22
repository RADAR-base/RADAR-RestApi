package org.radarcns.pipeline.config;

/*
 *  Copyright 2016 Kings College London and The Hyve
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

import java.io.File;
import java.io.IOException;
import org.radarcns.pipeline.mock.config.MockConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Config {

    private static final Logger logger = LoggerFactory.getLogger(Config.class);

    public static final String PIPELINE_CONFIG = "pipeline.yml";
    public static final String MOCK_CONFIG = "mock_file.yml";

    private static class BaseFile {
        private static final File file = new File(
                Config.class.getClassLoader().getResource(PIPELINE_CONFIG).getFile());
    }

    private static class PipelineHolder {
        private static final PipelineConfig instance = load();

        private static PipelineConfig load() {
            PipelineConfig config = null;

            try {
                config =  new ConfigLoader().load(BaseFile.file, PipelineConfig.class);
            } catch (IOException exec) {
                logger.error("PipelineConfig cannot be created. ", exec);
            }

            return config;
        }
    }

    private static class MockHolder {
        private static final MockConfig instance = load();

        private static MockConfig load() {
            MockConfig config = null;

            try {
                config =  new ConfigLoader().load(
                    new File(
                        Config.class.getClassLoader().getResource(MOCK_CONFIG).getFile()
                    ), MockConfig.class);
            } catch (IOException exec) {
                logger.error("MockConfig cannot be created. ", exec);
            }

            return config;
        }
    }

    public static PipelineConfig getPipelineConfig() {
        return PipelineHolder.instance;
    }

    public static MockConfig getMockConfig() {
        return MockHolder.instance;
    }

    public static File getBaseFile() {
        return BaseFile.file;
    }
}
