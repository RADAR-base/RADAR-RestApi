package org.radarcns.integration.util;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.ObjectReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.ws.rs.core.Response.Status;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.hamcrest.CoreMatchers;
import org.junit.rules.ExternalResource;
import org.radarcns.config.ServerConfig;
import org.radarcns.exception.TokenException;
import org.radarcns.oauth.OAuth2AccessTokenDetails;
import org.radarcns.oauth.OAuth2Client;
import org.radarcns.oauth.OAuth2Client.Builder;
import org.radarcns.producer.rest.ManagedConnectionPool;
import org.radarcns.producer.rest.RestClient;
import org.radarcns.util.RadarConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client to a REST API. Sets up the authorization against Management Portal
 */
public class ApiClient extends ExternalResource {

    private static final Logger logger = LoggerFactory.getLogger(ApiClient.class);
    private static final OAuth2Client oAuth2Client;
    private static final OAuth2AccessTokenDetails token;

    static {
        try {
            RestApiDetails restApiDetails = RestApiDetails.getRestApiClientDetails();
            oAuth2Client = new Builder()
                    .credentials(restApiDetails.getClientId(), restApiDetails.getClientSecret())
                    .endpoint(restApiDetails.getManagementPortalUrl(),
                            restApiDetails.getTokenEndpoint())
                    .scopes(restApiDetails.getClientScopes().split(" "))
                    .build();

            token = oAuth2Client.getValidToken();
        } catch (MalformedURLException e) {
            logger.error("Cannot create a valid url to access management portal", e);
            throw new AssertionError();
        } catch (TokenException e) {
            logger.error("Cannot get a valid access token", e);
            throw new AssertionError();
        }
    }

    private final ServerConfig config;
    private RestClient client;

    /**
     * Client to the REST API with given server and base path.
     *
     * @param config server configuration, including HTTPS safety, proxy and base path settings.
     */
    public ApiClient(ServerConfig config) {
        this.config = config;


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
    protected void before() {
        this.client = new RestClient(config, 120, new ManagedConnectionPool());
    }

    /**
     * Makes an HTTP get to given URL. For JSON requests, preferably use
     * {@link #getJson(String, Class, Status...)} or
     * {@link #getJsonList(String, Class, Status...)}. For plain text requests, use
     * {@link #getString(String, String, Status...)}. Close the response after use, for
     * example with a try-with-resources construct.
     *
     * @param relativePath path relative to the base URL, without starting slash.
     * @param accept Accept Header for content negotiation
     * @param expectedResponseCode response codes that are considered valid.
     * @return HTTP Response
     * @throws IOException if the get could not be executed
     */
    public Response get(String relativePath, String accept, Status... expectedResponseCode)
            throws IOException {
        Request request = this.client.requestBuilder(relativePath)
                .addHeader("User-Agent", "Mozilla/5.0")
                .addHeader("Accept", accept)
                .header("Authorization", "Bearer " + token.getAccessToken())
                .build();

        return request(request, expectedResponseCode);
    }

    private Response request(Request request, Status... expectedResponseCode) throws IOException {
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
     * Makes an HTTP POST request to given URL with JSON request and response types.
     *
     * @param relativePath path relative to the base URL, without starting slash.
     * @param body contents to include in the POST request as JSON.
     * @param responseClass JSON class to deserialize.
     * @param expectedResponseCodes response codes that are considered valid.
     * @param <T> response class type
     * @return Parsed JSON response
     * @throws IOException if the request could not be executed
     */
    public <T> T postJson(String relativePath, Object body, Class<T> responseClass,
            Status... expectedResponseCodes) throws IOException {
        RequestBody requestBody = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"),
                RadarConverter.writerFor(body.getClass()).writeValueAsBytes(body));

        Request request = this.client.requestBuilder(relativePath)
                .post(requestBody)
                .addHeader("User-Agent", "Mozilla/5.0")
                .addHeader("Accept", APPLICATION_JSON)
                .header("Authorization", "Bearer " + token.getAccessToken())
                .build();

        try (Response response = request(request, expectedResponseCodes)) {
            ResponseBody responseBody = response.body();
            assertNotNull(responseBody);
            return RadarConverter.readerFor(responseClass).readValue(responseBody.byteStream());
        }
    }

    /**
     * Request a string from the API, with given relative path.
     *
     * @param relativePath path relative to the base URL, without starting slash.
     * @param accept Accept Header for content negotiation
     * @param expectedResponse response codes that are considered valid.
     * @return HTTP Response body as a string
     * @throws IOException if the get could not be executed
     */
    @Nonnull
    public String getString(String relativePath, String accept, Status... expectedResponse)
            throws IOException {
        try (Response response = get(relativePath, accept, expectedResponse)) {
            ResponseBody body = response.body();
            assertNotNull(body);
            return body.string();
        }
    }

    /**
     * Request a JSON object using given object reader. Preferably use shorthand
     * {@link #getJson(String, Class, Status...)} or
     * {@link #getJsonList(String, Class, Status...)}. This sets the Accept
     * header to {@code application/json}.
     *
     * @param relativePath path relative to the base URL, without starting slash.
     * @param reader object reader to deserialize with.
     * @param expectedResponse response codes that are considered valid.
     * @param <K> type of the JSON response
     * @return HTTP Response body as an object
     * @throws IOException if the get could not be executed
     */
    public <K> K getJson(String relativePath, ObjectReader reader, Status... expectedResponse)
            throws IOException {
        try (Response response = get(relativePath, APPLICATION_JSON, expectedResponse)) {
            ResponseBody body = response.body();
            assertNotNull(body);
            return reader.readValue(body.byteStream());
        }
    }

    /**
     * Request an JSON object from the API, with given relative path. This sets the Accept
     * header to {@code application/json}.
     *
     * @param relativePath path relative to the base URL, without starting slash.
     * @param jsonClass JSON class to deserialize.
     * @param expectedResponse response codes that are considered valid.
     * @param <K> type of the JSON response
     * @return HTTP Response body as an object
     * @throws IOException if the get could not be executed
     */
    @Nonnull
    public <K> K getJson(String relativePath, Class<K> jsonClass, Status... expectedResponse)
            throws IOException {
        return getJson(relativePath, RadarConverter.readerFor(jsonClass), expectedResponse);
    }

    /**
     * Request a list of JSON objects from the API, with given relative path. This sets the Accept
     * header to {@code application/json}.
     *
     * @param relativePath path relative to the base URL, without starting slash.
     * @param jsonClass Avro SpecificRecord class to deserialize.
     * @param expectedResponse response codes that are considered valid.
     * @param <K> type of the JSON response
     * @return HTTP Response body as an object
     * @throws IOException if the get could not be executed
     */
    @Nonnull
    public <K> List<K> getJsonList(String relativePath, Class<K> jsonClass,
            Status... expectedResponse) throws IOException {
        return getJson(relativePath,
                RadarConverter.readerForCollection(List.class, jsonClass), expectedResponse);
    }

    @Override
    protected void after() {
        this.client.close();
    }
}
