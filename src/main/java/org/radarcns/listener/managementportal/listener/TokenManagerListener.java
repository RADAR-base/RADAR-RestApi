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

import org.radarcns.exception.TokenException;
import org.radarcns.oauth.OAuth2AccessTokenDetails;
import org.radarcns.oauth.OAuth2Client;
import org.radarcns.config.managementportal.config.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

//TODO check whether the token library is closing all the body request. Log reports
//WARNING [OkHttp ConnectionPool] okhttp3.internal.platform.Platform.log A connection to
// http://34.250.170.242:9000/ was leaked. Did you forget to close a response body? To see where
// this was allocated, set the OkHttpClient logger level to FINE:
// Logger.getLogger(OkHttpClient.class.getName()).setLevel(Level.FINE);

/**
 * Refreshes the OAuth2 token needed to authenticate against the Management Portal and adds it to
 *      the {@link javax.servlet.ServletContext} in this way multiple function can make reuse of it.
 */
@WebListener
public class TokenManagerListener implements ServletContextListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(TokenManagerListener.class);

    private static final String ACCESS_TOKEN = "TOKEN";

    private static final OAuth2Client client;
    private static OAuth2AccessTokenDetails token;

    static {
        try {
            client = new OAuth2Client()
                        .tokenEndpoint(Properties.getTokenEndPoint())
                        .clientId(Properties.getOauthClientId())
                        .clientSecret(Properties.getOauthClientSecret())
                        .addScope("read")
                        .addScope("write");
        } catch (MalformedURLException exc) {
            LOGGER.error("Properties cannot be loaded. Check the log for more information.", exc);
            throw new ExceptionInInitializerError(exc);
        }
        token = new OAuth2AccessTokenDetails();
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            client.setHttpClient(HttpClientListener.getClient(sce.getServletContext()));
            getToken(sce.getServletContext());
        } catch (TokenException exc) {
            LOGGER.warn("{} cannot be generated: {}", ACCESS_TOKEN, exc.getMessage());
        }

    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // clear connection pool
        client.getHttpClient().connectionPool().evictAll();
        // clear current token (set to invalid, expired token)
        token = new OAuth2AccessTokenDetails();
        // clear the token from the context
        sce.getServletContext().setAttribute(ACCESS_TOKEN, null);

        LOGGER.info("{} has been invalidated.", ACCESS_TOKEN);
    }

    /**
     * Returns the {@code Access Token} needed to interact with the Management Portal. If the token
     *      available in {@link ServletContext} is still valid, it will be returned. In case it has
     *      expired, the functional will automatically renew it.
     * @param context {@link ServletContext} where the last used {@code Access Token} has been
     *      stored
     * @return a valid {@code Access Token} to contact Management Portal
     * @throws TokenException In case the token was expired, and a new token could not be retrieved.
     */
    public static String getToken(ServletContext context) throws TokenException {
        if (token.isExpired()) {
            refresh(context);
        }
        return token.getAccessToken();
    }

    /**
     * Refresh the access token stored in the {@link ServletContext}
     * @param context {@link ServletContext} where the last used {@code Access Token} has been
     *      stored
     * @throws TokenException If the token could not be retrieved.
     */
    private static synchronized void refresh(ServletContext context) throws TokenException {
        // Multiple threads can be waiting to enter this method when the token is expired, we need
        // only the first one to request a new token, subsequent threads can safely exit immediately
        if (!token.isExpired()) {
            return;
        }

        token = client.getAccessToken();

        context.setAttribute(ACCESS_TOKEN, token.getAccessToken());

        // we need to supply date in millis, token.getIssueDate() and getExpiresIn() are in seconds
        LOGGER.info("Refreshed token at {} valid till {}", getDate(Instant.now().toEpochMilli()),
                getDate((token.getIssueDate() + token.getExpiresIn()) * 1000));
    }

    private static String getDate(long time) {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS").format(new Date(time));
    }
}
