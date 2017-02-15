package org.radarcns.integrationtest.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
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

}
