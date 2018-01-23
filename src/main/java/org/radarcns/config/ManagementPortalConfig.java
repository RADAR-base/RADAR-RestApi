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

package org.radarcns.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URL;
import java.util.Objects;
import javax.annotation.Nonnull;

/**
 * <p>Java class that defines the configuration required by the web app to handle authentication and
 * authorisation against Management Portal and REDCap instances.</p>
 *
 * <p>Current implementation support a single Management Portal instance since the current
 * RADAR-CNS Platform Architecture is designed with a centralised Management Portal. In order
 * to support multiple Management Portal instances, the following variables<ul>
 *      <li>{@code oauthClientId}</li>
 *      <li>{@code oauthClientSecret}</li>
 *      <li>{@code managementPortalUrl}</li>
 *      <li>{@code tokenEndpoint}</li>
 *      <li>{@code projectEndpoint}</li>
 *      <li>{@code subjectEndpoint}</li>
 * </ul>
 */
public class ManagementPortalConfig {
    /** OAuth2 client identifier. */
    @JsonProperty("oauth_client_id")
    private String oauthClientId;

    /** OAuth2 client secret. */
    @JsonProperty("oauth_client_secret")
    private String oauthClientSecret;

    /** OAuth2 client scopes. */
    @JsonProperty("oauth_client_scopes")
    private String oauthClientScopes;

    /** URL pointing a Management Portal instance. */
    @JsonProperty("management_portal_url")
    private URL managementPortalUrl;

    /** Web root of Management Portal token end point. It is required to refresh Access Token. */
    @JsonProperty("token_endpoint")
    private String tokenEndpoint;

    /**
     * Web root of Management Portal project end point. It is required to get a Management Portal
     *      Project.
     */
    @JsonProperty("project_endpoint")
    private String projectEndpoint;

    /**
     * Web root of Management Portal subject end point. It is required to create and get Managemen
     *      Portal Subjects.
     */
    @JsonProperty("subject_endpoint")
    private String subjectEndpoint;

    /**
     * Web root of Management Portal source-type end point. It is required to create and get
     * ManagementPortal SourceTypes.
     */
    @JsonProperty("source_data_endpoint")
    private String sourceDataEndpoint;

    /**
     * Web root of Management Portal source-type end point. It is required to create and get
     * Managemen
     *      Portal Subjects.
     */
    @JsonProperty("source_type_endpoint")
    private String sourceTypeEndpoint;

    /** Time until subject and project caches are invalidated. */
    @JsonProperty("cache_invalidate_duration")
    private String cacheInvalidateDuration;

    /** Time until subject and project caches can be retried on failed requests. */
    @JsonProperty("cache_retry_duration")
    private String cacheRetryDuration;

    public String getOauthClientId() {
        return oauthClientId;
    }

    public String getOauthClientSecret() {
        return oauthClientSecret;
    }

    public String getOauthClientScopes() {
        return oauthClientScopes;
    }

    public URL getManagementPortalUrl() {
        return managementPortalUrl;
    }

    public String getTokenEndpoint() {
        return tokenEndpoint;
    }

    public String getProjectEndpoint() {
        return projectEndpoint;
    }

    public String getSubjectEndpoint() {
        return subjectEndpoint;
    }

    public String getSourceTypeEndpoint() {
        return sourceTypeEndpoint;
    }

    public String getSourceDataEndpoint() {
        return sourceDataEndpoint;
    }

    /**
     * Ensures that the resultant string represents a relative directory.
     * It modifies the string if needed to start with a non-slash and ends with a slash.
     * If referencing the current directory, this returns an empty string.
     */
    public static String ensureRelativeDirectory(@Nonnull String str) {
        Objects.requireNonNull(str);
        String result = str;
        while (!result.isEmpty() && result.charAt(0) == '/') {
            result = result.substring(1);
        }
        if (result.isEmpty()) {
            return "";
        }
        return result.charAt(result.length() - 1) == '/' ? result : result + '/';
    }

    public String getCacheInvalidateDuration() {
        return cacheInvalidateDuration;
    }

    public String getCacheRetryDuration() {
        return cacheRetryDuration;
    }

    @Override
    public String toString() {
        return "Configuration {" + "\n"
                + "oauthClientId = '" + oauthClientId + "'\n"
                + "oauthClientSecret = '" + oauthClientSecret + "'\n"
                + "oauthClientScopes = '" + oauthClientScopes + "'\n"
                + "managementPortalUrl = " + managementPortalUrl + "\n"
                + "tokenEndpoint = '" + tokenEndpoint + "'\n"
                + "projectEndpoint = '" + projectEndpoint + "'\n"
                + "subjectEndpoint = '" + subjectEndpoint + "'\n"
                + "sourceTypeEndpoint = '" + sourceTypeEndpoint + "'\n"
                + "sourceDataEndpoint = '" + sourceDataEndpoint + "'\n"
                + "cacheRetryDuration = '" + cacheRetryDuration + "'\n"
                + "cacheInvalidateDuration = '" + cacheInvalidateDuration + "'\n"
                + '}';
    }
}