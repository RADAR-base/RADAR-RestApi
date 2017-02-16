package org.radarcns.integrationtest.util;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.avro.specific.SpecificRecord;
import org.radarcns.avro.restapi.avro.Message;
import org.radarcns.avro.restapi.dataset.Dataset;
import org.radarcns.util.AvroConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Francesco Nobilia on 10/01/2017.
 */
public class HttpClient {

    private static final Logger logger = LoggerFactory.getLogger(HttpClient.class);

    private final String USER_AGENT = "Mozilla/5.0";
    private final String SERVER = "http://52.210.59.174:8080/";
    private final String PATH = "radar/api/";

    private final long CONNECTION_TIMEOUT = 30;

    private final OkHttpClient httpClient;

    public HttpClient(){
        httpClient = new OkHttpClient.Builder()
            .connectTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
            .build();
    }

    public SpecificRecord doGetRequest(String url) throws IOException {
        Request request = new Request.Builder().header("User-Agent", USER_AGENT).url(url).build();
        Response response = httpClient.newCall(request).execute();

        if ( response.code() == 200 ){
            return AvroConverter.avroByteToAvro(response.body().bytes(), Dataset.getClassSchema());
        }

        return AvroConverter.avroByteToAvro(response.body().bytes(), Message.getClassSchema());
    }

}
