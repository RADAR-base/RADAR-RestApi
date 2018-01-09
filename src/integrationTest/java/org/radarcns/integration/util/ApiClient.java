package org.radarcns.integration.util;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.radarcns.webapp.util.BasePath.AVRO_BINARY;

import java.io.IOException;
import java.net.MalformedURLException;
import java.security.GeneralSecurityException;
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
import org.radarcns.producer.rest.ManagedConnectionPool;
import org.radarcns.producer.rest.RestClient;
import org.radarcns.util.AvroConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client to a REST API. Sets up the Management Portal WireMock and authentication.
 */
public class ApiClient extends ExternalResource {
    private static final Logger logger = LoggerFactory.getLogger(ApiClient.class);

    private final ServerConfig config;
    private final ManagementPortalOAuth2 oauth2;
    private final ManagementPortalWireMock wireMock;
    private RestClient client;

    /**
     * Client to the REST API with given server and base path.
     *
     * @param config server configuration, including HTTPS safety, proxy and base path settings.
     */
    public ApiClient(ServerConfig config) {
        this.config = config;
        try {
            this.oauth2 = new ManagementPortalOAuth2();
        } catch (GeneralSecurityException | IOException e) {
            throw new AssertionError("Token Utils cannot be initialized", e);
        }
        this.wireMock = new ManagementPortalWireMock();
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
    protected void before() throws Throwable {
        this.client = new RestClient(config, 120, new ManagedConnectionPool());
        this.wireMock.before();
    }

    /**
     * Makes an HTTP request to given URL.
     *
     * @param relativePath path relative to the base URL, without starting slash.
     * @param accept Accept Header for content negotiation
     * @param expectedResponse response codes that are considered valid. If none are given, any
     *                         success response code is considered valid.
     *
     * @return HTTP Response
     * @throws IOException if the request could not be executed
     * @throws AssertionError if the response code does not match one of expectedResponse or
     *                        if no expectedResponse is provided if the response code does not
     *                        indicate success.
     */
    public Response request(String relativePath, String accept, Status... expectedResponse)
            throws IOException {
        Request request = this.client.requestBuilder(relativePath)
                .addHeader("User-Agent", "Mozilla/5.0")
                .addHeader("Accept", accept)
                .header("Authorization", "Bearer " + oauth2.getAccessToken())
                .build();

        logger.info("Requesting {} expecting code {}", request.url(), expectedResponse);

        Response response = client.request(request);
        if (expectedResponse.length == 0) {
            assertTrue(response.isSuccessful());
        } else {
            assertThat(Status.fromStatusCode(response.code()),
                    anyOf(Arrays.stream(expectedResponse)
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
        this.wireMock.after();
        this.client.close();
    }
}
