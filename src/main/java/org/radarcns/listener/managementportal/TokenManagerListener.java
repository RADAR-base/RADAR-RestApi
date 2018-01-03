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

package org.radarcns.listener.managementportal;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.radarcns.config.managementportal.Properties;
import org.radarcns.exception.TokenException;
import org.radarcns.oauth.OAuth2AccessTokenDetails;
import org.radarcns.oauth.OAuth2Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public static final String ACCESS_TOKEN = "TOKEN";

    private static final OAuth2Client client;

    private static OAuth2AccessTokenDetails token;

    static {
        try {
            client = new OAuth2Client()
                    .tokenEndpoint(new URL(Properties.validateMpUrl() , Properties.getTokenPath()))
                    .clientId(Properties.getOauthClientId())
                    .clientSecret(Properties.getOauthClientSecret());
            for(String scope : Properties.getOauthClientScopes().split(" ")) {
                client.addScope(scope);
            }
        } catch (MalformedURLException exc) {
            LOGGER.error("Properties cannot be loaded. Check the log for more information.", exc);
            throw new ExceptionInInitializerError(exc);
        }
        token = new OAuth2AccessTokenDetails();
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            // set the OkHttpClient created from local managed pool
            client.httpClient(HttpClientListener.getClient(sce.getServletContext()));
            if (token.isExpired()) {
                refresh(sce.getServletContext());
            }
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

        context.setAttribute(ACCESS_TOKEN, token);

        // we need to supply date in millis, token.getIssueDate() and getExpiresIn() are in seconds
        LOGGER.info("Refreshed token at {} valid till {}", getDate(Instant.now().toEpochMilli()),
                getDate((token.getIssueDate() + token.getExpiresIn()) * 1000));
    }

    private static String getDate(long time) {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS").format(new Date(time));
    }

    public synchronized static OAuth2AccessTokenDetails getToken(ServletContext context) {
        OAuth2AccessTokenDetails currentToken = (OAuth2AccessTokenDetails) context.getAttribute
                (ACCESS_TOKEN);
        if(((OAuth2AccessTokenDetails) context.getAttribute
                (ACCESS_TOKEN)).isExpired()) {
            refresh(context);
            return  (OAuth2AccessTokenDetails) context.getAttribute(ACCESS_TOKEN);
        }
        else {
           return currentToken;
        }
    }
}
