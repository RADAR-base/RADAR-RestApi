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
import java.util.Objects;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.radarcns.config.ManagementPortalConfig;
import org.radarcns.config.Properties;
import org.radarcns.exception.TokenException;
import org.radarcns.oauth.OAuth2AccessTokenDetails;
import org.radarcns.oauth.OAuth2Client;
import org.radarcns.util.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//TODO check whether the token library is closing all the body request. Log reports
//WARNING [OkHttp ConnectionPool] okhttp3.internal.platform.Platform.log A connection to
// http://34.250.170.242:9000/ was leaked. Did you forget to close a response body? To see where
// this was allocated, set the OkHttpClient logger level to FINE:
// Logger.getLogger(OkHttpClient.class.getName()).setLevel(Level.FINE);

/**
 * Refreshes the OAuth2 token needed to authenticate against the Management Portal and adds it to
 * the {@link javax.servlet.ServletContext} in this way multiple function can make reuse of it.
 */
@WebListener
public class ManagementPortalClientManager implements ServletContextListener {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(ManagementPortalClientManager.class);

    private static final String ACCESS_TOKEN = "TOKEN";
    private static final String MP_CLIENT = "MP_CLIENT";
    private static final String OAUTH2_CLIENT = "OAUTH2_CLIENT";



    @Override
    public void contextInitialized(ServletContextEvent sce) {
        getOAuth2Client(sce.getServletContext());
        try {
            getToken(sce.getServletContext());
            getManagementPortalClient(sce.getServletContext());
        } catch (TokenException e) {
            LOGGER.warn("Cannot initialize ManagementPortal Client due to Token exception ", e);
        }

    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // clear connection pool
        OAuth2Client authClient = (OAuth2Client) sce.getServletContext()
                .getAttribute(OAUTH2_CLIENT);
        if (Objects.nonNull(authClient)) {
            authClient.getHttpClient().connectionPool().evictAll();
        }
        // clear current token (set to invalid, expired token)
        sce.getServletContext().setAttribute(ACCESS_TOKEN, null);
        LOGGER.info("{} has been invalidated.", ACCESS_TOKEN);
        sce.getServletContext().setAttribute(OAUTH2_CLIENT, null);
        sce.getServletContext().setAttribute(MP_CLIENT, null);


    }

    /**
     * Refresh the access token stored in the {@link ServletContext}.
     *
     * @param context {@link ServletContext} where the {@code Access Token} has been stored
     * @throws TokenException If the token could not be retrieved.
     */
    private static synchronized void refresh(ServletContext context) throws TokenException {
        // Multiple threads can be waiting to enter this method when the token is expired, we need
        // only the first one to request a new token, subsequent threads can safely exit immediately
        OAuth2AccessTokenDetails currentToken = (OAuth2AccessTokenDetails) context
                .getAttribute(ACCESS_TOKEN);
        if (Objects.nonNull(currentToken) && !currentToken.isExpired()) {
            return;
        }

        do {
            try {
                Thread.sleep(30000L);
            } catch (InterruptedException e) {
                LOGGER.error("Thread interrupted while waiting for MP");
            }
        } while (!HttpUtil.isReachable(
                Properties.getApiConfig().getManagementPortalConfig().getManagementPortalUrl()));


        currentToken = getOAuth2Client(context).getAccessToken();

        context.setAttribute(ACCESS_TOKEN, currentToken);

        // we need to supply date in millis, token.getIssueDate() and getExpiresIn() are in seconds
        LOGGER.info("Refreshed token at {} valid till {}", getDate(Instant.now().toEpochMilli()),
                getDate((currentToken.getIssueDate() + currentToken.getExpiresIn()) * 1000));
    }

    private static String getDate(long time) {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS").format(new Date(time));
    }

    private static synchronized OAuth2AccessTokenDetails getToken(ServletContext context)
            throws TokenException {
        OAuth2AccessTokenDetails currentToken = (OAuth2AccessTokenDetails) context
                .getAttribute(ACCESS_TOKEN);
        if (Objects.isNull(currentToken) || currentToken.isExpired()) {
            refresh(context);
            return (OAuth2AccessTokenDetails) context.getAttribute(ACCESS_TOKEN);
        } else {
            return currentToken;
        }
    }

    /**
     * Gets the instance of {@link ManagementPortalClient} from given context, if not available
     * creates one and stores in the context.
     *
     * @param context current servlet context.
     * @return instance of {@link ManagementPortalClient}
     * @throws TokenException if cannot get a valid token while initializing the client.
     */
    public static ManagementPortalClient getManagementPortalClient(ServletContext context)
            throws TokenException {
        ManagementPortalClient managementPortalClient = (ManagementPortalClient) context
                .getAttribute(MP_CLIENT);
        if (managementPortalClient == null) {
            managementPortalClient = new ManagementPortalClient(
                    HttpClientListener.getClient(context));
            context.setAttribute(MP_CLIENT, managementPortalClient);
        }
        managementPortalClient.updateToken(getToken(context));
        return managementPortalClient;
    }

    private static OAuth2Client getOAuth2Client(ServletContext context) {
        OAuth2Client authClient = (OAuth2Client) context.getAttribute(OAUTH2_CLIENT);
        if (authClient == null) {
            try {
                ManagementPortalConfig config = Properties.getApiConfig()
                        .getManagementPortalConfig();
                authClient = new OAuth2Client()
                        .tokenEndpoint(
                                new URL(config.getManagementPortalUrl(), config.getTokenEndpoint()))
                        .clientId(config.getOauthClientId())
                        .clientSecret(config.getOauthClientSecret());

                authClient.httpClient(HttpClientListener.getClient(context));

                for (String scope : config.getOauthClientScopes().split(" ")) {
                    authClient.addScope(scope);
                }
            } catch (MalformedURLException exc) {
                LOGGER.error("Properties cannot be loaded. Check the log for more information.",
                        exc);
                throw new ExceptionInInitializerError(exc);
            }
        }
        context.setAttribute(OAUTH2_CLIENT, authClient);
        return authClient;
    }


}
