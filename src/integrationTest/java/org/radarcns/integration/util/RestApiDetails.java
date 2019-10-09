package org.radarcns.integration.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import net.minidev.json.annotate.JsonIgnore;
import org.radarbase.config.ServerConfig;
import org.radarbase.config.YamlConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestApiDetails {

    @JsonIgnore
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiClient.class);

    @JsonIgnore
    private static final String CONFIG_FILE_NAME = "restapi-details.yml";

    @JsonIgnore
    private static RestApiDetails instance = null;

    @JsonProperty("application")
    private ServerConfig applicationConfig;

    @JsonProperty("oauth_client_id")
    private String clientId;

    @JsonProperty("oauth_client_secret")
    private String clientSecret;

    @JsonProperty("oauth_client_scopes")
    private String clientScopes;

    @JsonProperty("management_portal_url")
    private URL managementPortalUrl;

    @JsonProperty("token_endpoint")
    private String tokenEndpoint;

    private RestApiDetails() {
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getClientScopes() {
        return clientScopes;
    }

    public URL getManagementPortalUrl() {
        return managementPortalUrl;
    }

    public String getTokenEndpoint() {
        return tokenEndpoint;
    }

    public ServerConfig getApplicationConfig() {
        return applicationConfig;
    }

    /**
     * Get rest-api configs.
     */
    public static RestApiDetails getRestApiClientDetails() {
        if (instance == null) {
            String path = Objects
                    .requireNonNull(RestApiDetails.class.getClassLoader().getResource(CONFIG_FILE_NAME))
                    .getFile();
            LOGGER.info("Loading RestAPI client Config file located at : {}", path);
            try {
                instance = new YamlConfigLoader().load(new File(path).toPath(), RestApiDetails.class);
            } catch (IOException e) {
                LOGGER.error("Cannot load rest-api client details, returning default configs");
                RestApiDetails restApiDetails = new RestApiDetails();
                restApiDetails.clientId = "radar_dashboard";
                restApiDetails.clientSecret = "secret";
                restApiDetails.clientScopes = "SUBJECT.READ PROJECT.READ SOURCE.READ "
                        + "SOURCETYPE.READ MEASUREMENT.READ";
                try {
                    restApiDetails.managementPortalUrl = new URL("http://localhost:8090/");
                } catch (MalformedURLException e1) {
                    throw new AssertionError("Failed to construct ManagementPortal url");
                }
                restApiDetails.tokenEndpoint = "oauth/token";
                return restApiDetails;

            }
        }
        return instance;
    }

}
