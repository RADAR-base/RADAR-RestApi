package org.radarcns.integration.util;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.radarcns.webapp.util.BasePath.AVRO_BINARY;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.ws.rs.core.Response.Status;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.avro.Schema;
import org.apache.avro.specific.SpecificRecord;
import org.hamcrest.CoreMatchers;
import org.junit.rules.ExternalResource;
import org.radarcns.config.ServerConfig;
import org.radarcns.exception.TokenException;
import org.radarcns.oauth.OAuth2AccessTokenDetails;
import org.radarcns.oauth.OAuth2Client;
import org.radarcns.producer.rest.ManagedConnectionPool;
import org.radarcns.producer.rest.RestClient;
import org.radarcns.util.AvroConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client to a REST API. Sets up the authorization against Management Portal
 */
public class ApiClient extends ExternalResource {
    private static final Logger logger = LoggerFactory.getLogger(ApiClient.class);

    private final ServerConfig config;

    private final OAuth2Client oAuth2Client;

    private final OAuth2AccessTokenDetails token ;

    private RestClient client;

    /**
     * Client to the REST API with given server and base path.
     *
     * @param config server configuration, including HTTPS safety, proxy and base path settings.
     */
    public ApiClient(ServerConfig config) {
        this.config = config;

        try {
            RestApiDetails restApiDetails = RestApiDetails
                    .getRestApiClientDetails();
            this.oAuth2Client = new OAuth2Client()
                    .clientId(restApiDetails.getClientId())
                    .clientSecret(restApiDetails.getClientSecret())
                    .tokenEndpoint(
                            new URL(new URL(restApiDetails.getManagementPortalUrl()),
                                    restApiDetails.getTokenEndpoint()));

            for (String scope : restApiDetails.getClientScopes().split(" ")) {
                oAuth2Client.addScope(scope);
            }

            this.token = this.oAuth2Client.getAccessToken();

        } catch (MalformedURLException e) {
            throw new AssertionError("Cannot create a valid url to access management portal", e);
        } catch (TokenException e) {
            throw new AssertionError("Cannot get a valid access token", e);
        }
    }

    /**
     * Client to the REST API with given server and base path.
     *
     * @param url API server root path, with HTTPS safety set to true and no proxies.
     */
    public ApiClient(String url) {
        this(parseUrl(url));
    }

    private static ServerConfig parseUrl(String url) {
        try {
            return new ServerConfig(url);
        } catch (MalformedURLException ex) {
            throw new AssertionError("Cannot parse URL", ex);
        }
    }

    @Override
    protected void before() throws TokenException {

        this.client = new RestClient(config, 120, new ManagedConnectionPool());
    }

    /**
     * Makes an HTTP request to given URL.
     *
     * @param relativePath path relative to the base URL, without starting slash.
     * @param accept Accept Header for content negotiation
     * @param expectedResponseCode response codes that are considered valid. If none are given, any
     *                         success response code is considered valid.
     *
     * @return HTTP Response
     * @throws IOException if the request could not be executed
     * @throws AssertionError if the response code does not match one of expectedResponse or
     *                        if no expectedResponse is provided if the response code does not
     *                        indicate success.
     */
    public Response request(String relativePath, String accept, Status... expectedResponseCode)
            throws IOException {
        Request request = this.client.requestBuilder(relativePath)
                .addHeader("User-Agent", "Mozilla/5.0")
                .addHeader("Accept", accept)
                .header("Authorization", "Bearer " + token.getAccessToken())
                .build();

        logger.info("Requesting {} expecting code {}", request.url(), expectedResponseCode);

        Response response = client.request(request);
        if (expectedResponseCode.length == 0) {
            assertTrue(response.isSuccessful());
        } else {
            assertThat(Status.fromStatusCode(response.code()),
                    anyOf(Arrays.stream(expectedResponseCode)
                            .map(CoreMatchers::equalTo)
                            .collect(Collectors.toList())));
        }
        return response;
    }

    /**
     * Request a string from the API, with given relative path.
     *
     * @param relativePath path relative to the base URL, without starting slash.
     * @param accept Accept Header for content negotiation
     * @param expectedResponse response codes that are considered valid. If none are given, any
     *                         success response code is considered valid.
     *
     * @return HTTP Response body as a string
     * @throws IOException if the request could not be executed
     * @throws AssertionError if the response code does not match one of expectedResponse or
     *                        if no expectedResponse is provided if the response code does not
     *                        indicate success.
     */
    @Nonnull
    public String requestString(String relativePath, String accept, Status... expectedResponse)
            throws IOException {
        try (Response response = request(relativePath, accept, expectedResponse)) {
            ResponseBody body = response.body();
            assertNotNull(body);
            return body.string();
        }
    }

    /**
     * Request an Avro SpecificRecord from the API, with given relative path. This sets the
     * Accept header to {@code avro/binary}.
     *
     * @param relativePath path relative to the base URL, without starting slash.
     * @param avroClass Avro SpecificRecord class to deserialize.
     * @param expectedResponse response codes that are considered valid. If none are given, any
     *                         success response code is considered valid.
     *
     * @return HTTP Response body as a string
     * @throws IOException if the request could not be executed
     * @throws ReflectiveOperationException if the provided class does not have a static
     *                                      {@code getClassSchema()} method.
     * @throws AssertionError if the response code does not match one of expectedResponse or
     *                        if no expectedResponse is provided if the response code does not
     *                        indicate success.
     */
    @Nonnull
    public <K extends SpecificRecord> K requestAvro(String relativePath, Class<K> avroClass,
            Status... expectedResponse) throws IOException, ReflectiveOperationException {
        try (Response response = request(relativePath, AVRO_BINARY, expectedResponse)) {
            ResponseBody body = response.body();
            assertNotNull(body);
            @SuppressWarnings("JavaReflectionMemberAccess")
            Schema schema = (Schema) avroClass.getMethod("getClassSchema").invoke(null);
            return AvroConverter.avroByteToAvro(body.bytes(), schema);
        }
    }

    @Override
    protected void after() {
        this.client.close();
        this.oAuth2Client.getHttpClient().connectionPool().evictAll();
    }
}
