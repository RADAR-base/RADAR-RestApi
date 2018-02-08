package org.radarcns.config;

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

import java.io.File;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Properties handler.
 */
public final class Properties {

    /**
     * Logger.
     **/
    private static final Logger LOGGER = LoggerFactory.getLogger(Properties.class);

    /**
     * Path to the configuration file for AWS deploy.
     **/
    private static final String PATH_FILE_AWS = "/usr/share/tomcat8/conf/";

    /**
     * Path to the configuration file for Docker image.
     **/
    private static final String PATH_FILE_DOCKER = "/usr/local/tomcat/conf/radar/";

    /**
     * Placeholder alternative path for the config folder.
     **/
    private static final String CONFIG_FOLDER = "CONFIG_FOLDER";

    /**
     * API Config file name.
     **/
    public static final String NAME_CONFIG_FILE = "radar.yml";

    /**
     * Singleton. The default folder for config files is {@code PATH_FILE}. It can be override
     * setting the environment variable {@code CONFIG_FOLDER}. In case, no one of those contain the
     * expected file, the {@code ClassLoader} is used to load file from the resources folder.
     **/
    private static final ApplicationConfig API_CONFIG_INSTANCE;

    static {
        try {
            API_CONFIG_INSTANCE = loadApiConfig();
        } catch (IOException exec) {
            LOGGER.error(exec.getMessage(), exec);
            throw new ExceptionInInitializerError(exec);
        }
    }

    /**
     * Gives access to the singleton API properties.
     *
     * @return Properties
     */
    public static ApplicationConfig getApiConfig() {
        return API_CONFIG_INSTANCE;
    }

    /**
     * Loads the API configuration file. First of all, the {@code CONFIG_FOLDER} env variable is
     * checked to verify if points a valid config file. If not, the default location for AWS and
     * Docker image deployment are checked. In the last instance, the config file is searched inside
     * the default projects resources folder.
     */
    private static ApplicationConfig loadApiConfig() throws IOException {
        String[] paths = new String[]{
                System.getenv(CONFIG_FOLDER),
                PATH_FILE_AWS,
                PATH_FILE_DOCKER
        };

        ApplicationConfig config;
        for (String path1 : paths) {
            config = loadApiConfig(path1);
            if (config != null) {
                return config;
            }
        }

        String path = Properties.class.getClassLoader().getResource(NAME_CONFIG_FILE).getFile();

        LOGGER.info("Loading Config file located at : {}", path);

        return new YamlConfigLoader().load(new File(path), ApplicationConfig.class);
    }

    private static ApplicationConfig loadApiConfig(String path) throws IOException {
        String filePath = path + NAME_CONFIG_FILE;

        if (fileExists(filePath)) {
            LOGGER.info("Loading Config file located at : {}", path);
            return new YamlConfigLoader().load(new File(filePath), ApplicationConfig.class);
        } else {
            return null;
        }
    }


    /**
     * Checks whether the give path points a file.
     *
     * @param path that should point a file
     * @return true if {@code path} points a file, false otherwise
     */
    private static boolean fileExists(String path) {
        return path != null && new File(path).exists();
    }
}
