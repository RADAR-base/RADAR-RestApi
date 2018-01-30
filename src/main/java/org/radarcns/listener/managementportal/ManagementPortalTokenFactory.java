package org.radarcns.listener.managementportal;

import java.net.MalformedURLException;
import java.net.URL;
import javax.inject.Inject;
import okhttp3.OkHttpClient;
import org.glassfish.hk2.api.Factory;
import org.radarcns.config.ManagementPortalConfig;
import org.radarcns.config.Properties;
import org.radarcns.exception.TokenException;
import org.radarcns.oauth.OAuth2AccessTokenDetails;
import org.radarcns.oauth.OAuth2Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManagementPortalTokenFactory implements Factory<OAuth2AccessTokenDetails> {
    private static final Logger logger = LoggerFactory
            .getLogger(ManagementPortalTokenFactory.class);

    private final URL url;
    private final String clientSecret;
    private final String clientId;

    @Inject
    private OkHttpClient httpClient;
    private OAuth2Client oauthClient;

    public ManagementPortalTokenFactory() throws MalformedURLException {
        ManagementPortalConfig config = Properties.getApiConfig().getManagementPortalConfig();
        url = new URL(config.getManagementPortalUrl(), config.getTokenEndpoint());
        clientId = config.getOauthClientId();
        clientSecret = config.getOauthClientSecret();
    }

    @Override
    public OAuth2AccessTokenDetails provide() {
        synchronized (this) {
            if (oauthClient == null) {
                oauthClient = new OAuth2Client()
                        .tokenEndpoint(url)
                        .clientId(clientId)
                        .clientSecret(clientSecret)
                        .httpClient(httpClient);
            }
        }
        try {
            return oauthClient.getAccessToken();
        } catch (TokenException e) {
            logger.error("Failed to retrieve token", e);
            throw new IllegalStateException(
                    "Failed to retrieve access token for management portal.");
        }
    }

    @Override
    public void dispose(OAuth2AccessTokenDetails instance) {
        // TODO: reuse token
    }
}
