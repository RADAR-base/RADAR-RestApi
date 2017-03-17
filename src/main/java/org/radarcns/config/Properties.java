package org.radarcns.config;

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

import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidParameterException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

/**
 * Properties handler.
 */
public final class Properties {

    /** Logger. **/
    private static final Logger LOGGER = LoggerFactory.getLogger(Properties.class);

    // Useful for AWS deploy
    /** Path to the configuration file for AWS deploy. **/
    private static final String PATH_FILE = "/usr/share/tomcat8/conf/";
    /** Config file name. **/
    public static final String NAME_FILE = "radar.yml";

    /** Interface to access properties. **/
    private static RadarConfig config;

    /** Baypass singleto . **/
    private static boolean bypass;

    /** Singleton. **/
    private static final Properties INSTANCE = new Properties(PATH_FILE + NAME_FILE);

    /** Test instance. **/
    private static Properties instanceTest;

    /**
     * Gives access to the singleton properties.
     * @return Properties
     */
    public static Properties getInstance() {
        if (bypass) {
            return instanceTest;
        }

        return INSTANCE;
    }

    /**
     * Gives access to the singleton properties. ONLY FOR TEST PURPOSES.
     * @return Properties
     */
    //TODO review
    public static synchronized Properties getInstanceTest(String path) {
        if (instanceTest == null) {
            bypass = true;
            instanceTest = new Properties(path);
        }

        return instanceTest;
    }

    private Properties(String path) {
        initDockerConfig();

        if (config == null) {
            initYamlConfig(path);
        }
    }

    private void initDockerConfig() {
        try {
            config = new DockerConfig();

            LOGGER.info("Properties fetched from env variables");
        } catch (InvalidParameterException ex) {
            LOGGER.warn("Impossible load Docker properties", ex);
        }
    }

    private void initYamlConfig(String path) {
        try {
            final Yaml yaml = new Yaml();
            final InputStream input = Files.newInputStream(Paths.get(path));
            config = yaml.loadAs(input, YamlConfig.class);

            LOGGER.info("Properties fetched from .yml file");
        } catch (IOException ex) {
            LOGGER.warn("Impossible load Yaml properties", ex);
        }
    }

    /**
     * Returns the list of all known MongoDB instances.
     * @return MongoDB instances as List
     */
    public List<ServerAddress> getMongoHosts() {
        return config.getMongoDbHosts();
    }

    /**
     * Returns the list of all known MongoDB credentials.
     * @return MongoDB credentials as List
     */
    public List<MongoCredential> getMongoDbCredential() {
        return config.getMongoDbCredentials();
    }

    /**
     * Returns a String representing the MongoDB database name.
     * @return MongoDB database name as String
     */
    public String getMongoDbName() {
        return config.getMongoDbName();
    }
}
