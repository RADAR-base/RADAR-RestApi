package org.radarcns.integration.testcase.config;

/*
 * Copyright 2017 King's College London and The Hyve
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

import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import okhttp3.Response;
import org.junit.Test;
import org.radarcns.config.Properties;
import org.radarcns.integration.util.Utility;

/**
 * Checks if the config file for the Front-End ecosystem is where expected, and checks the
 *      validity of the swagger documentation.
 */
public class ExposedConfigTest {

    private static final String PROTOCOL = "http";
    private static final String SERVER = "localhost";
    private static final int PORT = 8080;

    public static final String CONFIG_JSON = "config.json";
    private static final String SWAGGER_JSON = "swagger.json";

    @Test
    public void checkFrontEndConfig() throws IOException {
        URL url = new URL(PROTOCOL, SERVER, PORT, "/radar/frontend/");

        String actual = checkFrontEndConfig(url);

        String expected = Utility.fileToString(
                ExposedConfigTest.class.getClassLoader().getResource(CONFIG_JSON).getFile());

        assertEquals(expected, actual);
    }

    /** Retrieves the exposed Frontedn config file. **/
    public static String checkFrontEndConfig(URL url) throws IOException {
        Response response = Utility.makeRequest(new URL(url, CONFIG_JSON).toString());

        assertEquals(200, response.code());

        return response.body().string();
    }

    @Test
    public void checkSwaggerDoc() throws MalformedURLException {
        URL url = new URL(PROTOCOL, SERVER, PORT, "/radar/api/");
        assertEquals(Properties.getApiConfig().getApiBasePath(), getSwaggerBasePath(url));
    }

    /** Retrieves the exposed Swagger documentation. **/
    public static String getSwaggerBasePath(URL url) throws MalformedURLException {
        Swagger swagger = new SwaggerParser().read(
                new URL(url, SWAGGER_JSON).toString().toString());

        return swagger.getBasePath();
    }

}
