package org.radarcns.old.util;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.avro.specific.SpecificRecord;
import org.radarcns.avro.restapi.dataset.Dataset;
import org.radarcns.util.AvroConverter;

/**
 * HTTP client to consume AVRO Rest Api.
 */
public class HttpClient {

    //private static final Logger logger = LoggerFactory.getLogger(HttpClient.class);

    @SuppressWarnings({"checkstyle:AbbreviationAsWordInName","checkstyle:MemberName"})
    private final String USER_AGENT = "Mozilla/5.0";

    //private final String SERVER = "http://52.210.59.174:8080/";
    //private final String PATH = "radar/api/";

    @SuppressWarnings({"checkstyle:AbbreviationAsWordInName","checkstyle:MemberName"})
    private final long CONNECTION_TIMEOUT = 30;

    private final OkHttpClient httpClient;

    /**
     * Constructor, it instantiates the client and set the timeouts.
     **/
    public HttpClient() {
        httpClient = new OkHttpClient.Builder()
            .connectTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
            .build();
    }

    /**
     * It contact the given URL and return either a {@code SpecificRecord} representing a Dataset
     *      or null in case the response code is different from 200
     * @param url URL used to buid the http request.
     * @return {@code SpecificRecord} representing a Dataset or null
     * @see {@link org.radarcns.avro.restapi.dataset.Dataset}
     **/
    public SpecificRecord doGetRequest(String url) throws IOException {
        Request request = new Request.Builder().header("User-Agent", USER_AGENT).url(url).build();
        Response response = httpClient.newCall(request).execute();

        if (response.code() == 200) {
            return AvroConverter.avroByteToAvro(response.body().bytes(), Dataset.getClassSchema());
        }

        return null;
    }

}
