package org.radarcns;

import org.junit.Test;

import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.Assert.assertEquals;

/**
 * Created by Francesco Nobilia on 10/01/2017.
 */
public class ApiTest {

    private final boolean TEST = false;

    private final String USER_AGENT = "Mozilla/5.0";
    private final String SERVER = "http://localhost:8080/";
    private final String PATH = "radar/api/";

    @Test
    public void callTest204() throws Exception {
        if( TEST ) {
            assertEquals(204, request(SERVER + PATH + "Acc/count/radarTestTheHyveEmpaticaDevice0/00:07:80:12:F0:42"));
        }
    }

    @Test
    public void callTest200() throws Exception {
        if( TEST ) {
            assertEquals(200, request(SERVER + PATH + "Acc/count/radarTestTheHyveEmpaticaDevice0/00:07:80:12:F0:41"));
        }
    }

    private int request(String url) throws Exception {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", USER_AGENT);

        return con.getResponseCode();
    }

}
