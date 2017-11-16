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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import java.io.IOException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import okhttp3.Request;
import okhttp3.Response;
import org.junit.BeforeClass;
import org.junit.Test;
import org.radarcns.config.Properties;
import org.radarcns.config.ServerConfig;
import org.radarcns.integration.util.TokenTestUtils;
import org.radarcns.integration.util.Utility;
import org.radarcns.integration.util.WiremockUtils;
import org.radarcns.producer.rest.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final String WEB_ROOT = "radar";
    private static final String BASE_PATH = "api";
    public static final String FRONTEND = "frontend";

    private static Logger logger = LoggerFactory.getLogger(ExposedConfigTest.class);

    @BeforeClass
    public static void loadWiremock() throws Exception {
        WiremockUtils.initializeWiremock();
        logger.info("Wiremock set up successfully");
    }


    @Test
    public void checkFrontEndConfig()
            throws IOException, NoSuchAlgorithmException, KeyManagementException {
        URL url = new URL(PROTOCOL, SERVER, PORT, "/" + WEB_ROOT + "/" + FRONTEND + "/");

        try (Response response = Utility.makeRequest(new URL(url, CONFIG_JSON).toString())) {
            assertEquals(200, response.code());

            String expected = Utility.readAll(
                    ExposedConfigTest.class.getClassLoader().getResourceAsStream(CONFIG_JSON));

            assertEquals(expected, response.body().string());
        }
    }

    @Test
    public void checkSwaggerDoc()
            throws IOException, NoSuchAlgorithmException, KeyManagementException {
        ServerConfig config = new ServerConfig();
        config.setProtocol(PROTOCOL);
        config.setHost(SERVER);
        config.setPort(PORT);
        config.setPath("/" + WEB_ROOT + "/" + BASE_PATH + "/");
        config.setUnsafe(false);
        assertEquals(Properties.getApiConfig().getApiBasePath(), getSwaggerBasePath(config));
    }

    /** Retrieves the exposed Swagger documentation. **/
    public static String getSwaggerBasePath(ServerConfig config)
            throws IOException, NoSuchAlgorithmException, KeyManagementException {


        try (RestClient client = new RestClient(config);
                Response response = client.request(new Request.Builder().
                        header("Authorization","Bearer "
                                + TokenTestUtils.VALID_TOKEN)
                        .url(client.getRelativeUrl(SWAGGER_JSON)).build())) {

            logger.info("Requested {}", client.getRelativeUrl(SWAGGER_JSON));

            ObjectMapper mapper = new ObjectMapper();
            JsonNode swaggerDocumentation = mapper.readTree(response.body().string());

            Swagger swagger = new SwaggerParser().read(swaggerDocumentation);

            return swagger.getBasePath();
        }
    }
}
