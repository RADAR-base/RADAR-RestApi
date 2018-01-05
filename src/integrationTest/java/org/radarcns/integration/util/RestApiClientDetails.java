package org.radarcns.integration.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import net.minidev.json.annotate.JsonIgnore;
import org.radarcns.config.Properties;
import org.radarcns.config.YamlConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestApiClientDetails {

    @JsonIgnore
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiClient.class);

    @JsonIgnore
    private static final String CONFIG_FILE_NAME = "restapi-client-details.yml";

    @JsonIgnore
    private static RestApiClientDetails instance = null;

    @JsonProperty("oauth_client_id")
    private String clientId;

    @JsonProperty("oauth_client_secret")
    private String clientSecret;

    @JsonProperty("oauth_client_scopes")
    private String clientScopes;

    @JsonProperty("management_portal_url")
    private String managementPortalUrl;

    @JsonProperty("token_endpoint")
    private String tokenEndpoint;

    private RestApiClientDetails () {}

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getClientScopes() {
        return clientScopes;
    }

    public String getManagementPortalUrl() {
        return managementPortalUrl;
    }

    public String getTokenEndpoint() {
        return tokenEndpoint;
    }

    public static RestApiClientDetails getRestApiClientDetails() {
        if(Objects.isNull(instance)) {
            String path = Properties.class.getClassLoader().getResource(CONFIG_FILE_NAME).getFile();
            LOGGER.info("Loading RestAPI client Config file located at : {}", path);
            try {
                instance =new YamlConfigLoader().load(new File(path), RestApiClientDetails.class);
            } catch (IOException e) {
                LOGGER.error("Cannot load rest-api client details, returning default configs");
                RestApiClientDetails restApiClientDetails = new RestApiClientDetails();
                restApiClientDetails.clientId = "radar_dashboard";
                restApiClientDetails.clientSecret = "secret";
                restApiClientDetails.clientScopes = "SUBJECT.READ PROJECT.READ SOURCE.READ "
                        + "SOURCETYPE.READ MEASUREMENT.READ";
                restApiClientDetails.managementPortalUrl = "http://localhost:8090/";
                restApiClientDetails.tokenEndpoint = "oauth/token";
                return restApiClientDetails;

            }
        }
        return instance;
    }

}
