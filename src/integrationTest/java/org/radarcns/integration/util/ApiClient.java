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
import org.radarcns.producer.rest.RestClient;
import org.radarcns.util.AvroConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiClient extends ExternalResource {
    private static final Logger logger = LoggerFactory.getLogger(ApiClient.class);

    private final ServerConfig config;
    private final ManagementPortalOAuth2 oauth2;
    private final ManagementPortalWireMock wireMock;
    private RestClient client;

    public ApiClient(ServerConfig config) {
        this.config = config;
        try {
            this.oauth2 = new ManagementPortalOAuth2();
        } catch (GeneralSecurityException | IOException e) {
            throw new AssertionError("Token Utils cannot be initialized", e);
        }
        this.wireMock = new ManagementPortalWireMock();
    }

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
        this.client = new RestClient(config);
        this.wireMock.before();
    }

    /**
     * Makes an HTTP request to given URL.
     *
     * @param path path
     * @param accept Accept Header for content negotiation
     *
     * @return HTTP Response
     * @throws IOException if the request could not be executed
     */
    public Response request(String path, String accept, Status... expectedResponse)
            throws IOException {
        Request request = this.client.requestBuilder(path)
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

    @Nonnull
    public String requestString(String path, String accept, Status... expectedResponse)
            throws IOException {
        try (Response response = request(path, accept, expectedResponse)) {
            ResponseBody body = response.body();
            assertNotNull(body);
            return body.string();
        }
    }

    @Nonnull
    public <K extends SpecificRecord> K requestAvro(String path, Class<K> recordClass,
            Status... expectedResponse) throws IOException, ReflectiveOperationException {
        try (Response response = request(path, AVRO_BINARY, expectedResponse)) {
            ResponseBody body = response.body();
            assertNotNull(body);
            @SuppressWarnings("JavaReflectionMemberAccess")
            Schema schema = (Schema) recordClass.getMethod("getClassSchema").invoke(null);
            return AvroConverter.avroByteToAvro(body.bytes(), schema);
        }
    }

    @Override
    protected void after() {
        this.wireMock.after();
        this.client.close();
    }
}
