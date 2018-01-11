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

package org.radarcns.config;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import javax.ws.rs.core.Response.Status;
import org.junit.Rule;
import org.junit.Test;
import org.radarcns.integration.util.ApiClient;
import org.radarcns.integration.util.Utility;

/**
 * Checks if the config file for the Front-End ecosystem is where expected, and checks the
 *      validity of the swagger documentation.
 */
public class ExposedConfigTest {
    public static final String CONFIG_JSON = "config.json";
    public static final String OPENAPI_JSON = "openapi.json";

    private static final String BASE_PATH = "api";
    private static final String FRONTEND = "frontend";

    @Rule
    public ApiClient apiClient = new ApiClient("http://localhost:8080/radar/");

    @Test
    public void checkFrontEndConfig()
            throws IOException, NoSuchAlgorithmException, KeyManagementException {
        String actual = apiClient.requestString(FRONTEND + '/' + CONFIG_JSON, "*/*", Status.OK);
        String expected = Utility.readAll(
                ExposedConfigTest.class.getClassLoader().getResourceAsStream(CONFIG_JSON));

        assertEquals(expected, actual);
    }

    @Test
    public void checkSwaggerDoc()
            throws IOException, GeneralSecurityException {
        String response = apiClient.requestString(BASE_PATH + '/' + OPENAPI_JSON,
                APPLICATION_JSON, Status.OK);
        JsonNode node = new ObjectMapper().readTree(response);
        assertTrue(node.has("servers"));
        String serverUrl = node.get("servers").elements().next().get("url").asText();
        assertEquals(Properties.getApiConfig().getApplicationConfig().getUrlString(), serverUrl);
    }
}
