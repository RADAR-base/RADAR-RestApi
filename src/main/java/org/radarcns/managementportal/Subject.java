/*
 * Copyright 2017 The Hyve
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

package org.radarcns.managementportal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.net.URL;
import java.util.List;
import java.util.Map;
import org.radarcns.managementportal.util.UrlDeseralizer;

/**
 * Java Class representing a RADAR Management Portal Subject.
 */
public class Subject {

    public static final String HUMAN_READABLE_IDENTIFIER_KEY = "Human-readable-identifier";

    private final String login;
    @JsonProperty("externalId")
    private final String externalId;
    @JsonDeserialize(using = UrlDeseralizer.class)
    @JsonProperty("externalLink")
    private final URL externalLink;
    @JsonProperty("email")
    private final String email;
    @JsonProperty("status")
    private final String status;
    @JsonProperty("sources")
    private final List<Source> sources;
    @JsonProperty("attributes")
    private final Map<String,String> attributes;
    @JsonProperty("project")
    private final Project project;

    /**
     * Constructor.
     * @param login {@link String} representing Management Portal Subject identifier
     * @param externalId {@link Integer} representing the REDCap Record identifier
     * @param externalLink {@link URL} pointing the REDCap integration form / instrument
     * @param attributes {@link Map} representing the value associated with
     * @param status {@link String} representing the status of the subject
     * @param sources {@link List} of {@link Tag} representing the sources of a subject
     * @param project {@link Project} representing the value associated with
     *      {@link #HUMAN_READABLE_IDENTIFIER_KEY}
     */
    public Subject(@JsonProperty("login") String login,
                   @JsonProperty("externalId") String externalId,
                   @JsonProperty("externalLink") URL externalLink,
                   @JsonProperty("attributes") Map<String,String> attributes,
                   @JsonProperty("status") String status,
                   @JsonProperty("sources") List<Source> sources,
                   @JsonProperty("email") String email,
                   @JsonProperty("project") Project project) {
        this.login = login;
        this.externalId = externalId;
        this.externalLink = externalLink;
        this.attributes = attributes;
        this.status = status;
        this.sources = sources;
        this.email = email;
        this.project = project;
    }

    @JsonProperty("id")
    public String getLogin() {
        return login;
    }

    public String getExternalId() {
        return externalId;
    }

    public URL getExternalLink() {
        return externalLink;
    }

    public String getEmail() {
        return email;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public String getStatus() {
        return status;
    }

    public List<Source> getSources() {
        return sources;
    }

    @JsonIgnore
    public Project getProject() {
        return project;
    }

    /**
     * Returns the Human Readable Identifier associated with this subject.
     * @return {@link String} stating the Human Readable Identifier associated with this subject
     */
    @JsonIgnore
    public String getHumanReadableIdentifier() {
        return getAttribute(HUMAN_READABLE_IDENTIFIER_KEY);
    }

    /**
     * Gets the project attribute (e.g. tag) associated with the given {@link String} key.
     * @param key {@link String} tag key
     * @return {@link String} value associated with the given key
     */
    @JsonIgnore
    public String getAttribute(String key) {
        return attributes.get(key);
    }

    @Override
    public String toString() {
        return "Subject{" + '\n'
                + "login=" + login + '\n'
                + "externalId='" + externalId + "'\n"
                + "externalLink='" + externalLink + "'\n"
                + "status='" + status + "'\n"
                + "project=" + project.getProjectName() + '}';
    }

}
