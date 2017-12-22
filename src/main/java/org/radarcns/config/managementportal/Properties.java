/*
 * Copyright 2017 King's College London
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

package org.radarcns.config.managementportal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Objects;
import org.radarcns.config.YamlConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton class to manage configuration files.
 */
public final class Properties {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(Properties.class);

    private static final String HTTPS = "https";


    /** Path to the configuration file for AWS deploy. */
    private static final String PATH_FILE_AWS = "/usr/share/tomcat8/conf/";

    /** Path to the configuration file for Docker image. */
    private static final String PATH_FILE_DOCKER = "/usr/local/tomcat/conf/radar/";

    /** Placeholder alternative path for the config folder. */
    private static final String CONFIG_FOLDER = "CONFIG_FOLDER";

    /** API Config file name. */
    public static final String NAME_CONFIG_FILE = "mp_info.yml";

    /** Path where the config file is located. */
    //private static String validPath;

    private static final Configuration CONFIG;

    static {
        try {
            CONFIG = loadApiConfig();
        } catch (IOException exec) {
            LOGGER.error(exec.getMessage(), exec);
            throw new ExceptionInInitializerError(exec);
        }
    }

    private Properties() {
        //Nothing to do
    }

    /**
     * Loads the API configuration file. First of all, the {@code CONFIG_FOLDER} env variable is
     *      checked to verify if points a valid config file. If not, the default location for AWS
     *      and Docker image deployment are checked. In the last instance, the config file is
     *      searched inside the default projects resources folder.
     */
    private static Configuration loadApiConfig() throws IOException {
        String[] paths = new String[]{
                System.getenv(CONFIG_FOLDER),
                PATH_FILE_AWS,
                PATH_FILE_DOCKER
        };

        Configuration config;
        for (int i = 0; i < paths.length; i++) {
            config = loadApiConfig(paths[i]);
            if (config != null) {
                return config;
            }
        }

        try {
            String path = Properties.class.getClassLoader().getResource(NAME_CONFIG_FILE).getFile();
            //validPath = new File(path).getParent() + "/";

            LOGGER.info("Loading Config file located at : {}", path);

            return new YamlConfigLoader().load(new File(path), Configuration.class);
        } catch (NullPointerException exc) {
            String[] folders = Arrays.copyOfRange(paths,
                    Objects.isNull(System.getenv(CONFIG_FOLDER)) ? 1 : 0, paths.length);
            LOGGER.error("Config file {} cannot be found at {} or in the WAR resources"
                    + "folder.", NAME_CONFIG_FILE, folders, CONFIG_FOLDER);
            throw new FileNotFoundException(NAME_CONFIG_FILE + " cannot be found.");
        }
    }


    private static Configuration loadApiConfig(String path) throws IOException {
        //validPath = path;
        String filePath = path + NAME_CONFIG_FILE;

        if (checkFileExist(filePath)) {
            LOGGER.info("Loading Config file located at : {}", path);
            return new YamlConfigLoader().load(new File(filePath), Configuration.class);
        }

        //validPath = null;
        return null;
    }



    /**
     * Checks whether the give path points a file.
     *
     * @param path that should point a file
     * @return true if {@code path} points a file, false otherwise
     */
    private static boolean checkFileExist(String path) {
        return path != null && new File(path).exists();
    }


    /**
     * Loads all configurations and converts them to {@link String}. If the conversion
     *      fails, it means that the config files are wrong.
     * @return a {@link String} representing the loaded configurations
     */
    public static String validate() {
        return CONFIG.toString();
    }


    /**
     * Get the OAuth2 client id to access ManagementPortal.
     * @return the client id
     */
    public static String getOauthClientId() {
        return CONFIG.getOauthClientId();
    }

    /**
     * Get the OAuth2 client secret to access ManagementPortal.
     * @return the client secret
     */
    public static String getOauthClientSecret() {
        return CONFIG.getOauthClientSecret();
    }

    /**
     * Generates the token end point {@link URL} needed to refresh tokens against Management Portal.
     * @return {@link URL} useful to refresh tokens
     */
    public static String getTokenPath() {
        return CONFIG.getTokenEndpoint();
    }

    /**
     * Generates the token end point {@link URL} needed to manage subjects on Management Portal.
     * @return {@link URL} useful create and update subjects
     */
    public static String getSubjectPath() {
        return CONFIG.getSubjectEndpoint();
    }

    /**
     * Generates the token end point {@link URL} needed to reade projects on Management Portal.
     * @return {@link URL} useful to read project information
     */
    public static String getProjectPath() {
        return CONFIG.getProjectEndpoint();
    }

    /**
     * Checks if the provided {@link URL} is using a secure connection or not.
     * @param url {@link URL} to check
     * @return {@code true} if the protocol is {@code HTTPS}, {@code false} otherwise
     */
    private static boolean isSecureConnection(URL url) {
        return url.getProtocol().equals(HTTPS);
    }

    /**
     * Returns a {@link URL} pointing a Management Portal instance and Checks if it is using a
     *      secure connection.
     * @return {@link URL} pointing the Management Portal instance specified on the config file
     */
    public static URL validateMpUrl() {
        if (!isSecureConnection(CONFIG.getManagementPortalUrl())) {
            LOGGER.warn("The provided Management Portal instance is not using an encrypted"
                    + " connection.");
        }
        return CONFIG.getManagementPortalUrl();
    }

}
