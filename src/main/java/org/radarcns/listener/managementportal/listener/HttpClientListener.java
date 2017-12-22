/*
 * Copyright 2017 King's College London
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.radarcns.listener.managementportal.listener;

import java.net.MalformedURLException;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.radarcns.config.ServerConfig;
import org.radarcns.producer.rest.ManagedConnectionPool;
import org.radarcns.producer.rest.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates a {@link OkHttpClient} and adds it to the {@link javax.servlet.ServletContext} in this
 *      way multiple function can rely on a single {@link OkHttpClient} instance.
 */
@WebListener
public class HttpClientListener implements ServletContextListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientListener.class);

    private static final String HTTP_CONNECTION_POOL = "org.radarcns.HTTP_CONNECTION_POOL";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        sce.getServletContext().setAttribute(HTTP_CONNECTION_POOL,
                new TrivialManagedConnectionPool());

        LOGGER.info("Created general HTTP Client.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        TrivialManagedConnectionPool pool = (TrivialManagedConnectionPool) sce.getServletContext()
                .getAttribute(HTTP_CONNECTION_POOL);

        if (pool != null) {
            pool.acquire().evictAll();
            sce.getServletContext().removeAttribute(HTTP_CONNECTION_POOL);

            LOGGER.info("Destroyed HTTP Client.");
        }
    }

    /**
     * Extracts the {@link OkHttpClient} from the {@link ServletContext}.
     * @param context {@link ServletContext} used to share variables across requests
     * @return a general instance of {@link OkHttpClient}
     * @throws IllegalStateException in case the client is not available
     */
    public static OkHttpClient getClient(ServletContext context) {
        ManagedConnectionPool pool = getPool(context);

        return new OkHttpClient.Builder()
                .connectionPool(pool.acquire())
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    public static RestClient getRestClient(ServletContext context, String url)
            throws MalformedURLException {
        ManagedConnectionPool pool = getPool(context);
        ServerConfig server = new ServerConfig(url);
        return new RestClient(server, 30, pool);
    }

    private static ManagedConnectionPool getPool(ServletContext context) {
        return (TrivialManagedConnectionPool) context.getAttribute(HTTP_CONNECTION_POOL);
    }

    /** Managed Connection Pool that holds a single connection pool during its lifetime. */
    private static class TrivialManagedConnectionPool extends ManagedConnectionPool {
        private final ConnectionPool pool;

        private TrivialManagedConnectionPool() {
            this.pool = new ConnectionPool();
        }

        @Override
        public ConnectionPool acquire() {
            return pool;
        }

        @Override
        public void release() {
            // noop
        }
    }
}
