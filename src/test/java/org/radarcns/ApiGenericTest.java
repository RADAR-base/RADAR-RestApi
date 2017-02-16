package org.radarcns;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.Test;
import org.radarcns.avro.restapi.avro.Message;
import org.radarcns.avro.restapi.dataset.Dataset;
import org.radarcns.avro.restapi.dataset.Item;
import org.radarcns.avro.restapi.header.DescriptiveStatistic;
import org.radarcns.avro.restapi.header.EffectiveTimeFrame;
import org.radarcns.avro.restapi.header.Header;
import org.radarcns.avro.restapi.header.Unit;
import org.radarcns.avro.restapi.sensor.HeartRate;
import org.radarcns.integrationtest.util.HttpClient;
import org.radarcns.util.AvroConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Francesco Nobilia on 10/01/2017.
 */
public class ApiGenericTest {

    private static final Logger logger = LoggerFactory.getLogger(ApiGenericTest.class);

    private final boolean TEST = true;

    private final String USER_AGENT = "Mozilla/5.0";
    private final String SERVER = "http://52.210.59.174:8080/";
    private final String PATH = "radar/api/";

    @Test
    public void callTest() throws Exception {
        if( TEST ) {
            assertEquals(200, requestAvro("http://52.210.59.174:8080/radar/api/Acc/AVRO/count/a/b"));
            //assertEquals(200, request(SERVER + PATH + "User/GetAllPatients"));
            //assertEquals(200, request(SERVER + PATH + "User/GetAllSources/UserID_0"));
            //assertEquals(200, request(SERVER + PATH + "Android/Status/UserID_0/SourceID_0"));
            //assertEquals(200, request(SERVER + PATH + "Device/Status/UserID_0/SourceID_0"));
            //assertEquals(200, request(SERVER + PATH + "Acc/RT/count/UserID_0/SourceID_0"));
        }
    }

    private int request(String url) throws Exception {

        logger.info(url);

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", USER_AGENT);
        con.setRequestProperty("content-type", "application/json");

        BufferedReader br = null;
        if (200 <= con.getResponseCode() && con.getResponseCode() <= 299) {
            br = new BufferedReader(new InputStreamReader((con.getInputStream())));
        } else {
            br = new BufferedReader(new InputStreamReader((con.getErrorStream())));
        }

        StringBuilder result = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            result.append(line);
        }
        br.close();
        logger.info(result.toString());

        return con.getResponseCode();
    }

    private int requestAvro(String url) throws Exception {

        logger.info(url);

        OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

        Request request = new Request.Builder().header("User-Agent", USER_AGENT).url(url).build();
        Response response = client.newCall(request).execute();

        byte[] array = response.body().bytes();
        logger.info("Received {} bytes", array.length);

//        if ( response.code() == 200 ){
//            logger.info(AvroConverter.avroByteToAvro(array, Dataset.getClassSchema()).toString());
//        }

        logger.info(AvroConverter.avroByteToAvro(array, Message.getClassSchema()).toString());

        return response.code();
    }

}
