package org.radarcns.config;

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

import java.io.File;
import java.io.IOException;
import org.radarcns.config.api.ApiConfig;
import org.radarcns.config.catalog.DeviceCatalog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Properties handler.
 */
public final class Properties {

    /** Logger. **/
    private static final Logger LOGGER = LoggerFactory.getLogger(Properties.class);

    /** Path to the configuration file for AWS deploy. **/
    private static final String PATH_FILE_AWS = "/usr/share/tomcat8/conf/";

    /** Path to the configuration file for Docker image. **/
    private static final String PATH_FILE_DOCKER = "/usr/local/tomcat/conf/radar/";

    /** Placeholder alternative path for the config folder. **/
    private static final String CONFIG_FOLDER = "CONFIG_FOLDER";

    /** API Config file name. **/
    public static final String NAME_CONFIG_FILE = "radar.yml";

    /** Device Catalog file name. **/
    public static final String NAME_DEV_CATALOG_FILE = "device-catalog.yml";

    /** Path where the config file is located. **/
    private static String validPath;

    /** Singleton. The default folder for config files is {@code PATH_FILE}. It can be
     *      override setting the environment variable {@code CONFIG_FOLDER}. In case, no one of
     *      those contain the expected file, the {@code ClassLoader} is used to load file from the
     *      resources folder.
     **/
    private static final ApiConfig API_CONFIG_INSTANCE;
    private static final DeviceCatalog DEVICE_CATALOG_INSTANCE;

    static {
        try {
            API_CONFIG_INSTANCE = loadApiConfig();
            DEVICE_CATALOG_INSTANCE = loadDeviceCatalog();
        } catch (IOException exec) {
            LOGGER.error(exec.getMessage(), exec);
            throw new ExceptionInInitializerError(exec);
        }
    }

    /**
     * Gives access to the singleton API properties.
     * @return Properties
     */
    public static ApiConfig getApiConfig() {
        return API_CONFIG_INSTANCE;
    }

    /**
     * Gives access to the singleton Device Catalog.
     * @return Properties
     */
    public static DeviceCatalog getDeviceCatalog() {
        return DEVICE_CATALOG_INSTANCE;
    }

    /**
     * Loads the API configuration file. First of all, the {@code CONFIG_FOLDER} env variable is
     *      checked to verify if points a valid config file. If not, the default location for AWS
     *      and Docker image deployment are checked. In the last instance, the config file is
     *      searched inside the default projects resources folder.
     */
    private static ApiConfig loadApiConfig() throws IOException {
        String[] paths = new String[]{
                System.getenv(CONFIG_FOLDER),
                PATH_FILE_AWS,
                PATH_FILE_DOCKER
        };

        ApiConfig config;
        for (int i = 0; i < paths.length; i++) {
            config = loadApiConfig(paths[i]);
            if (config != null) {
                return config;
            }
        }

        String path = Properties.class.getClassLoader().getResource(NAME_CONFIG_FILE).getFile();
        validPath = new File(path).getParent() + "/";

        LOGGER.info("Loading Config file located at : {}", path);

        return new YamlConfigLoader().load(new File(path), ApiConfig.class);
    }

    private static ApiConfig loadApiConfig(String path) throws IOException {
        validPath = path;
        String filePath = path + NAME_CONFIG_FILE;

        if (checkFileExist(filePath)) {
            LOGGER.info("Loading Config file located at : {}", path);
            return new YamlConfigLoader().load(new File(filePath), ApiConfig.class);
        }

        validPath = null;
        return null;
    }

    /**
     * Loads the Device Catalog configuration file.
     */
    private static DeviceCatalog loadDeviceCatalog() throws IOException {
        String path = API_CONFIG_INSTANCE.getDeviceCatalog();

        if (! (new File(path).isAbsolute())) {
            path = validPath + path;
        }

        if (!checkFileExist(path)) {
            path = Properties.class.getClassLoader().getResource(NAME_DEV_CATALOG_FILE).getFile();
        }

        LOGGER.info("Loading Device Catalog file located at : {}", path);

        return DeviceCatalog.load(new File(path));
    }

    /**
     * Checks whether the give path points a file.
     *
     * @param path that should point a file
     * @return true if {@code path} points a file, false otherwise
     */
    private static boolean checkFileExist(String path) {
        return path == null ? false : new File(path).exists();
    }
}
