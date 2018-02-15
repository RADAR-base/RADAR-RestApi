package org.radarcns.listener;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import org.glassfish.jersey.internal.inject.DisposableSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpClientFactory implements DisposableSupplier<OkHttpClient> {
    private static final Logger logger = LoggerFactory.getLogger(HttpClientFactory.class);

    @Override
    public OkHttpClient get() {
        return new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public void dispose(OkHttpClient client) {
        client.connectionPool().evictAll();
        ExecutorService executorService = client.dispatcher().executorService();
        executorService.shutdown();
        try {
            executorService.awaitTermination(3, TimeUnit.MINUTES);
            logger.info("OkHttp ExecutorService closed.");
        } catch (InterruptedException e) {
            logger.warn("InterruptedException on destroy()", e);
        }
    }
}
