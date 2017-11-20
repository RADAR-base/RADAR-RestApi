package org.radarcns.integration.util;

//import com.github.tomakehurst.wiremock.client.WireMock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * Created by yatharthranjan on 14/11/2017.
 */
public class WiremockUtils {

    public static final String WIREMOCK_HOST = "localhost";
    public static final int WIREMOCK_PORT = 8089;
    public static int wiremockInitialized = 0;
    public static final String SUBJECT_LOGIN1 = "UserID_0";
    public static final String SUBJECT_LOGIN2 = "UserID_01";
    public static final String SUBJECT_LOGIN_FALSE = "0";
    public static final String MOCK_SUBJECT = "{\n" +
            "    \"id\": 1151,\n" +
            "    \"login\": \"UserID_0\",\n" +
            "    \"externalLink\": null,\n" +
            "    \"externalId\": null,\n" +
            "    \"status\": \"ACTIVATED\",\n" +
            "    \"createdBy\": \"admin\",\n" +
            "    \"createdDate\": \"2017-11-09T16:23:28.416Z\",\n" +
            "    \"lastModifiedBy\": \"admin\",\n" +
            "    \"lastModifiedDate\": \"2017-11-12T13:24:57.533Z\",\n" +
            "    \"project\": {\n" +
            "        \"id\": 1051,\n" +
            "        \"projectName\": \"RADAR-Pilot-01\",\n" +
            "        \"description\": \"Testing and piloting\",\n" +
            "        \"organization\": \"RADAR\",\n" +
            "        \"location\": \"London\",\n" +
            "        \"startDate\": null,\n" +
            "        \"projectStatus\": \"PLANNING\",\n" +
            "        \"endDate\": null,\n" +
            "        \"projectAdmin\": null,\n" +
            "        \"deviceTypes\": [\n" +
            "            {\n" +
            "                \"id\": 1302,\n" +
            "                \"deviceProducer\": \"E4\",\n" +
            "                \"deviceModel\": \"Empatica\",\n" +
            "                \"catalogVersion\": \"v1\",\n" +
            "                \"sourceType\": \"PASSIVE\",\n" +
            "                \"canRegisterDynamically\": false,\n" +
            "                \"sensorData\": [],\n" +
            "                \"deviceTypeId\": null\n" +
            "            },\n" +
            "            {\n" +
            "                \"id\": 1303,\n" +
            "                \"deviceProducer\": \"App\",\n" +
            "                \"deviceModel\": \"aRMT-App\",\n" +
            "                \"catalogVersion\": \"v1\",\n" +
            "                \"sourceType\": \"ACTIVE\",\n" +
            "                \"canRegisterDynamically\": true,\n" +
            "                \"sensorData\": [],\n" +
            "                \"deviceTypeId\": null\n" +
            "            },\n" +
            "            {\n" +
            "                \"id\": 3001,\n" +
            "                \"deviceProducer\": \"THINC-IT App\",\n" +
            "                \"deviceModel\": \"App\",\n" +
            "                \"catalogVersion\": \"v1\",\n" +
            "                \"sourceType\": \"ACTIVE\",\n" +
            "                \"canRegisterDynamically\": true,\n" +
            "                \"sensorData\": [],\n" +
            "                \"deviceTypeId\": null\n" +
            "            },\n" +
            "            {\n" +
            "                \"id\": 3002,\n" +
            "                \"deviceProducer\": \"ANDROID\",\n" +
            "                \"deviceModel\": \"PHONE\",\n" +
            "                \"catalogVersion\": \"v1\",\n" +
            "                \"sourceType\": \"PASSIVE\",\n" +
            "                \"canRegisterDynamically\": true,\n" +
            "                \"sensorData\": [],\n" +
            "                \"deviceTypeId\": null\n" +
            "            }\n" +
            "        ],\n" +
            "        \"attributes\": [\n" +
            "            {\n" +
            "                \"key\": \"Work-package\",\n" +
            "                \"value\": \"TEST\"\n" +
            "            }\n" +
            "        ]\n" +
            "    },\n" +
            "    \"sources\": [\n" +
            "        {\n" +
            "            \"id\": 1686,\n" +
            "            \"deviceTypeId\": 1303,\n" +
            "            \"deviceTypeProducer\": \"App\",\n" +
            "            \"deviceTypeModel\": \"aRMT-App\",\n" +
            "            \"deviceTypeCatalogVersion\": \"v1\",\n" +
            "            \"expectedSourceName\": null,\n" +
            "            \"sourceId\": \"SourceID_0\",\n" +
            "            \"sourceName\": \"7c58b547\",\n" +
            "            \"assigned\": true,\n" +
            "            \"attributes\": {}\n" +
            "        }\n" +
            "    ],\n" +
            "    \"attributes\": {}\n" +
            "}";
    public static final String PUBLIC_KEY = "/oauth/token_key";
    public static final String TOKEN_END = "/oauth/token";
    public static final String SUBJECT_END1 = "/api/subjects/" + SUBJECT_LOGIN1;
    public static final String SUBJECT_END2 = "/api/subjects/" + SUBJECT_LOGIN2;
    public static final String SUBJECT_END_FALSE = "/api/subjects/" + SUBJECT_LOGIN_FALSE;
    public static String TOKEN_RESPONSE = "";

    private static Logger logger = LoggerFactory.getLogger(WiremockUtils.class);

    public static void initializeWiremock() throws Exception {
        TokenTestUtils.setUp();
        setupTokenResponse();
        setupStubs();
        wiremockInitialized = 1;
    }

    public static void setupTokenResponse() {
        TOKEN_RESPONSE = "{\"access_token\":\"" + TokenTestUtils.VALID_TOKEN + "\","+
                "\"token_type\":\"bearer\"," +
                "\"expires_in\":1799," +
                "\"scope\":\"SUBJECT.READ PROJECT.READ SOURCE.READ DEVICETYPE.READ\"," +
                "\"sub\":\"radar_restapi\"," +
                "\"sources\":[]," +
                "\"iss\":\"ManagementPortal\"," +
                "\"iat\":"+ Instant.now().getEpochSecond()+"," +
                "\"jti\":\"c9b29b53-2bf8-4a1b-aed5-1732e0dbce57\"}";
        logger.info("Mock token response set up successful!");
    }

    public static void setupStubs() {
        //WireMock wireMock1 = new WireMock(WIREMOCK_HOST, WIREMOCK_PORT);
        configureFor(WIREMOCK_HOST,WIREMOCK_PORT);
        stubFor(get(urlEqualTo(PUBLIC_KEY))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-type", TokenTestUtils.APPLICATION_JSON)
                        .withBody(TokenTestUtils.PUBLIC_KEY_BODY)));

        stubFor(post(urlEqualTo(TOKEN_END))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-type", TokenTestUtils.APPLICATION_JSON)
                        .withBody(TOKEN_RESPONSE)));

        stubFor(get(urlEqualTo(SUBJECT_END1))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-type", TokenTestUtils.APPLICATION_JSON)
                        .withBody(MOCK_SUBJECT)));

        stubFor(get(urlEqualTo(SUBJECT_END2))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-type", TokenTestUtils.APPLICATION_JSON)
                        .withBody(MOCK_SUBJECT)));
        stubFor(get(urlEqualTo(SUBJECT_END_FALSE))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-type", TokenTestUtils.APPLICATION_JSON)
                        .withBody(MOCK_SUBJECT)));
        logger.info("Mock MP set up successfully");
    }
}
