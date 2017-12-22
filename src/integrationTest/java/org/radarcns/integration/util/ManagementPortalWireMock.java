package org.radarcns.integration.util;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import com.github.tomakehurst.wiremock.client.WireMock;
import java.time.Instant;
import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by yatharthranjan on 14/11/2017.
 */
public class ManagementPortalWireMock extends ExternalResource {
    private static Logger logger = LoggerFactory.getLogger(ManagementPortalWireMock.class);

    private static final String APPLICATION_JSON = "application/json; charset=utf-8";
    private static final String WIREMOCK_HOST = "localhost";
    private static final int WIREMOCK_PORT = 8089;
    private static final String SUBJECT_LOGIN1 = "UserID_0";
    private static final String SUBJECT_LOGIN2 = "UserID_01";
    private static final String SUBJECT_LOGIN_FALSE = "0";
    private static final String MOCK_SUBJECT = "{\n"
            + "    \"id\": 1151,\n"
            + "    \"login\": \"UserID_0\",\n"
            + "    \"externalLink\": null,\n"
            + "    \"externalId\": null,\n"
            + "    \"status\": \"ACTIVATED\",\n"
            + "    \"createdBy\": \"admin\",\n"
            + "    \"createdDate\": \"2017-11-09T16:23:28.416Z\",\n"
            + "    \"lastModifiedBy\": \"admin\",\n"
            + "    \"lastModifiedDate\": \"2017-11-12T13:24:57.533Z\",\n"
            + "    \"project\": {\n"
            + "        \"id\": 1051,\n"
            + "        \"projectName\": \"RADAR-Pilot-01\",\n"
            + "        \"description\": \"Testing and piloting\",\n"
            + "        \"organization\": \"RADAR\",\n"
            + "        \"location\": \"London\",\n"
            + "        \"startDate\": null,\n"
            + "        \"projectStatus\": \"PLANNING\",\n"
            + "        \"endDate\": null,\n"
            + "        \"projectAdmin\": null,\n"
            + "        \"deviceTypes\": [\n"
            + "            {\n"
            + "                \"id\": 1302,\n"
            + "                \"deviceProducer\": \"E4\",\n"
            + "                \"deviceModel\": \"Empatica\",\n"
            + "                \"catalogVersion\": \"v1\",\n"
            + "                \"sourceType\": \"PASSIVE\",\n"
            + "                \"canRegisterDynamically\": false,\n"
            + "                \"sensorData\": [],\n"
            + "                \"deviceTypeId\": null\n"
            + "            },\n"
            + "            {\n"
            + "                \"id\": 1303,\n"
            + "                \"deviceProducer\": \"App\",\n"
            + "                \"deviceModel\": \"aRMT-App\",\n"
            + "                \"catalogVersion\": \"v1\",\n"
            + "                \"sourceType\": \"ACTIVE\",\n"
            + "                \"canRegisterDynamically\": true,\n"
            + "                \"sensorData\": [],\n"
            + "                \"deviceTypeId\": null\n"
            + "            },\n"
            + "            {\n"
            + "                \"id\": 3001,\n"
            + "                \"deviceProducer\": \"THINC-IT App\",\n"
            + "                \"deviceModel\": \"App\",\n"
            + "                \"catalogVersion\": \"v1\",\n"
            + "                \"sourceType\": \"ACTIVE\",\n"
            + "                \"canRegisterDynamically\": true,\n"
            + "                \"sensorData\": [],\n"
            + "                \"deviceTypeId\": null\n"
            + "            },\n"
            + "            {\n"
            + "                \"id\": 3002,\n"
            + "                \"deviceProducer\": \"ANDROID\",\n"
            + "                \"deviceModel\": \"PHONE\",\n"
            + "                \"catalogVersion\": \"v1\",\n"
            + "                \"sourceType\": \"PASSIVE\",\n"
            + "                \"canRegisterDynamically\": true,\n"
            + "                \"sensorData\": [],\n"
            + "                \"deviceTypeId\": null\n"
            + "            }\n"
            + "        ],\n"
            + "        \"attributes\": [\n"
            + "            {\n"
            + "                \"key\": \"Work-package\",\n"
            + "                \"value\": \"TEST\"\n"
            + "            }\n"
            + "        ]\n"
            + "    },\n"
            + "    \"sources\": [\n"
            + "        {\n"
            + "            \"id\": 1686,\n"
            + "            \"deviceTypeId\": 1303,\n"
            + "            \"deviceTypeProducer\": \"App\",\n"
            + "            \"deviceTypeModel\": \"aRMT-App\",\n"
            + "            \"deviceTypeCatalogVersion\": \"v1\",\n"
            + "            \"expectedSourceName\": null,\n"
            + "            \"sourceId\": \"SourceID_0\",\n"
            + "            \"sourceName\": \"7c58b547\",\n"
            + "            \"assigned\": true,\n"
            + "            \"attributes\": {}\n"
            + "        }\n"
            + "    ],\n"
            + "    \"attributes\": {}\n"
            + "}";
    private static final String PUBLIC_KEY = "/oauth/token_key";
    private static final String TOKEN_END = "/oauth/token";
    private static final String SUBJECT_END1 = "/api/subjects/" + SUBJECT_LOGIN1;
    private static final String SUBJECT_END2 = "/api/subjects/" + SUBJECT_LOGIN2;
    private static final String SUBJECT_END_FALSE = "/api/subjects/" + SUBJECT_LOGIN_FALSE;

    private ManagementPortalOAuth2 oauth;
    private WireMock wireMock;

    @Override
    protected void before() throws Throwable {
        this.wireMock = new WireMock(WIREMOCK_HOST, WIREMOCK_PORT);
        this.oauth = new ManagementPortalOAuth2();
        setupStubs();
    }

    @Override
    protected void after() {
        wireMock.removeMappings();
    }

    private String getTokenBody() {
        return "{\"access_token\":\"" + oauth.getAccessToken() + "\","
                + "\"token_type\":\"bearer\","
                + "\"expires_in\":3600,"
                + "\"scope\":\"SUBJECT.READ PROJECT.READ SOURCE.READ DEVICETYPE.READ\","
                + "\"sub\":\"radar_restapi\","
                + "\"sources\":[],"
                + "\"iss\":\"ManagementPortal\","
                + "\"iat\":"+ (Instant.now().getEpochSecond() - 30L) + ","
                + "\"jti\":\"c9b29b53-2bf8-4a1b-aed5-1732e0dbce57\"}";
    }

    private String getPublicKeyBody() {
        return "{\n"
                + "  \"alg\" : \"SHA256withRSA\",\n"
                + "  \"value\" : \"-----BEGIN PUBLIC KEY-----\\n" + oauth.getPublicKey()
                + "\\n-----END PUBLIC "
                + "KEY-----\"\n"
                + "}";
    }

    private void setupStubs() {
        wireMock.register(get(urlEqualTo(PUBLIC_KEY))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-type", APPLICATION_JSON)
                        .withBody(getPublicKeyBody())));

        wireMock.register(post(urlEqualTo(TOKEN_END))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-type", APPLICATION_JSON)
                        .withBody(getTokenBody())));

        wireMock.register(get(urlEqualTo(SUBJECT_END1))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-type", APPLICATION_JSON)
                        .withBody(MOCK_SUBJECT)));

        wireMock.register(get(urlEqualTo(SUBJECT_END2))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-type", APPLICATION_JSON)
                        .withBody(MOCK_SUBJECT)));
        wireMock.register(get(urlEqualTo(SUBJECT_END_FALSE))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-type", APPLICATION_JSON)
                        .withBody(MOCK_SUBJECT)));
        logger.info("Mock MP set up successfully");
    }
}
