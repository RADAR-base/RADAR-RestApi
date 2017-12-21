package org.radarcns.listener.managementportal.listener;

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

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates a {@link OkHttpClient} and adds it to the {@link javax.servlet.ServletContext} in this
 *      way multiple function can relay on a single {@link OkHttpClient} instance.
 */
@WebListener
public class HttpClientListener implements ServletContextListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientListener.class);

    private static final String HTTP_CLIENT = "HTTP_CLIENT";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        sce.getServletContext().setAttribute(HTTP_CLIENT, client);

        LOGGER.info("Created general HTTP Client.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        OkHttpClient client = (OkHttpClient) sce.getServletContext().getAttribute(HTTP_CLIENT);

        if (Objects.nonNull(client)) {
            client.connectionPool().evictAll();
            sce.getServletContext().setAttribute(HTTP_CLIENT, null);

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
        OkHttpClient client = (OkHttpClient) context.getAttribute(HTTP_CLIENT);

        if (Objects.isNull(client)) {
            throw new IllegalStateException("HTTP Client is null.");
        }

        return client;
    }
}
