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

import com.fasterxml.jackson.annotation.JsonCreator;
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

    private static final String HTTPS = "https";
    /** OAuth2 client identifier. */
    private final String oauthClientId;

    /** OAuth2 client secret. */
    private final String oauthClientSecret;

    /** OAuth2 client scopes. */
    private final String oauthClientScopes;

    /** URL pointing a Management Portal instance. */
    private final URL managementPortalUrl;

    /** Web root of Management Portal token end point. It is required to refresh Access Token. */
    private final String tokenEndpoint;

    /**
     * Web root of Management Portal project end point. It is required to get a Management Portal
     *      Project.
     */
    private final String projectEndpoint;

    /**
     * Web root of Management Portal subject end point. It is required to create and get Managemen
     *      Portal Subjects.
     */
    private final String subjectEndpoint;


    /**
     * Constructor.
     * @param oauthClientId {@link String} representing OAuth2 client identifier
     * @param oauthClientSecret {@link String} representing OAuth2 client identifier
     * @param oauthClientScopes {@link String} representing OAuth2 client scopes
     * @param managementPortalUrl {@link URL} pointing a Management Portal instane
     * @param tokenEndpoint {@link String} representing Management Portal web root to renew tokens
     * @param projectEndpoint {@link String} representing Management Portal web root to access
     *      project data
     * @param subjectEndpoint {@link String} representing Management Portal web root to manage
     *      subject
     */
    @JsonCreator
    protected ManagementPortalConfig(
            @JsonProperty("oauth_client_id") String oauthClientId,
            @JsonProperty("oauth_client_secret") String oauthClientSecret,
            @JsonProperty("oauth_client_scopes") String oauthClientScopes,
            @JsonProperty("management_portal_url") URL managementPortalUrl,
            @JsonProperty("token_endpoint") String tokenEndpoint,
            @JsonProperty("project_endpoint") String projectEndpoint,
            @JsonProperty("subject_endpoint") String subjectEndpoint) {
        this.oauthClientId = oauthClientId;
        this.oauthClientSecret = oauthClientSecret;
        this.oauthClientScopes = oauthClientScopes;
        this.managementPortalUrl = managementPortalUrl;
        this.tokenEndpoint = tokenEndpoint;
        this.projectEndpoint = projectEndpoint;
        this.subjectEndpoint = subjectEndpoint;
    }

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

    /**
     * Checks if the provided {@link URL} is using a secure connection or not.
     * @param url {@link URL} to check
     * @return {@code true} if the protocol is {@code HTTPS}, {@code false} otherwise
     */
    public static boolean isSecureConnection(URL url) {
        return url.getProtocol().equals(HTTPS);
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
            + '}';
    }
}
