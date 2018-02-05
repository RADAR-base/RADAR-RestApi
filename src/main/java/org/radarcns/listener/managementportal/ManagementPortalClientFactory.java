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
import java.time.Duration;
import javax.inject.Inject;
import okhttp3.OkHttpClient;
import org.glassfish.hk2.api.Factory;
import org.radarcns.config.ManagementPortalConfig;
import org.radarcns.config.Properties;
import org.radarcns.exception.TokenException;
import org.radarcns.oauth.OAuth2Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Refreshes the OAuth2 token needed to authenticate against the Management Portal and adds it to
 * the {@link javax.servlet.ServletContext} in this way multiple function can make reuse of it.
 */
public class ManagementPortalClientFactory implements Factory<ManagementPortalClient> {
    private static final Logger logger = LoggerFactory
            .getLogger(ManagementPortalClientFactory.class);

    private ManagementPortalClient mpClient;
    private OAuth2Client oauthClient;

    /**
     * Default constructor.
     */
    @Inject
    public ManagementPortalClientFactory(OkHttpClient httpClient) {
        mpClient = new ManagementPortalClient(httpClient);

        ManagementPortalConfig config = Properties.getApiConfig().getManagementPortalConfig();
        try {
            oauthClient = new OAuth2Client.Builder()
                    .endpoint(config.getManagementPortalUrl(), config.getTokenEndpoint())
                    .credentials(config.getOauthClientId(), config.getOauthClientSecret())
                    .httpClient(httpClient)
                    .build();
        } catch (MalformedURLException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public synchronized ManagementPortalClient provide() {
        try {
            mpClient.updateToken(oauthClient.getValidToken(Duration.ofSeconds(30)));
            return mpClient;
        } catch (TokenException e) {
            logger.error("Failed to retrieve token", e);
            return null;
        }
    }

    @Override
    public void dispose(ManagementPortalClient instance) {
        // no disposal needed
    }
}
