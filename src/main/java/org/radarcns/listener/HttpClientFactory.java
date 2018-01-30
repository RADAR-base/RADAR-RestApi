package org.radarcns.listener;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import okhttp3.OkHttpClient;
import org.glassfish.jersey.server.CloseableService;
import org.glassfish.hk2.api.Factory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpClientFactory implements Factory<OkHttpClient> {
    private static final Logger logger = LoggerFactory.getLogger(HttpClientFactory.class);

    private final OkHttpClient client;

    /** Disposes the client after use. */
    @Inject
    @SuppressWarnings("PMD.UnusedPrivateField")
    private CloseableService closeableService;

    public HttpClientFactory() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public OkHttpClient provide() {
        return client;
    }

    @Override
    public void dispose(OkHttpClient client) {
        client.connectionPool().evictAll();
        ExecutorService executorService = client.dispatcher().executorService();
        executorService.shutdown();
        try {
            executorService.awaitTermination(3, TimeUnit.MINUTES);
            logger.info("OKHTTP ExecutorService closed.");
        } catch (InterruptedException e) {
            logger.warn("InterruptedException on destroy()", e);
        }
    }

}
