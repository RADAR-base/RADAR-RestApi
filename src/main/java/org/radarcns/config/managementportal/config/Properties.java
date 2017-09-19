package org.radarcns.config.managementportal.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

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

/**
 * Singleton class to manage configuration files.
 */
public final class Properties {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(Properties.class);

    private static final String HTTPS = "https";

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
        Configuration config;

        String version = "0.1-alpha";
        String released = "2017-08-29";
        String oauthClientId = "radar_redcap_integrator";
        String oauthClientSecret = "my-secrect_token";
        URL managementPortalUrl = new URL("http://34.250.170.242:9000/");
        String tokenEndPoint = "oauth/token";
        String projectEndPoint = "api/projects/";
        String subjectEndPoint = "api/subjects";

        config = new Configuration(version, released, oauthClientId, oauthClientSecret, managementPortalUrl
                , tokenEndPoint, projectEndPoint, subjectEndPoint);


        return config;

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
     * @throws MalformedURLException in case the {@link URL} cannot be generated
     */
    public static URL getTokenEndPoint() throws MalformedURLException {
        return new URL(validateMpUrl(), CONFIG.getTokenEndpoint());
    }

    /**
     * Generates the token end point {@link URL} needed to manage subjects on Management Portal.
     * @return {@link URL} useful create and update subjects
     * @throws MalformedURLException in case the {@link URL} cannot be generated
     */
    public static URL getSubjectEndPoint() throws MalformedURLException {
        return new URL(validateMpUrl(), CONFIG.getSubjectEndpoint());
    }

    /**
     * Generates the token end point {@link URL} needed to reade projects on Management Portal.
     * @return {@link URL} useful to read project information
     * @throws MalformedURLException in case the {@link URL} cannot be generated
     */
    public static URL getProjectEndPoint() throws MalformedURLException {
        return new URL(CONFIG.getManagementPortalUrl().toString() + CONFIG.getProjectEndpoint());
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

    /**
     * Checks if the provided {@link URL} is using a secure connection and returns it.
     * @param url {@link URL} to has to be checked
     * @return the provided {@link URL}
     */
    public static URL validateRedcapUrl(URL url) {
        if (!isSecureConnection(url)) {
            LOGGER.warn("The provided REDCap instance is not using an encrypted connection.");
        }
        return url;
    }
}
