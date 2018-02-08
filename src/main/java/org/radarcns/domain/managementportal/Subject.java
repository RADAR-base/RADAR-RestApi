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

package org.radarcns.domain.managementportal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Java Class representing a RADAR Management Portal Subject.
 */
public class Subject {

    public static final String HUMAN_READABLE_IDENTIFIER_KEY = "Human-readable-identifier";

    @JsonProperty
    private String id;
    @JsonProperty
    private String externalId;
    @JsonProperty
    private URL externalLink;
    @JsonProperty
    private String email;
    @JsonProperty
    private String status;
    @JsonProperty
    private List<Source> sources;
    @JsonProperty
    private Map<String, String> attributes;
    @JsonProperty
    private Project project;

    @JsonSetter("login")
    public void setLogin(String login) {
        this.id = login;
    }

    public String getId() {
        return id;
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
     *
     * @return {@link String} stating the Human Readable Identifier associated with this subject or
     * {@code null} if not set.
     */
    @JsonIgnore
    public String getHumanReadableIdentifier() {
        return attributes.entrySet().stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase(HUMAN_READABLE_IDENTIFIER_KEY))
                .findAny()
                .map(Map.Entry::getValue)
                .orElse(null);
    }

    /**
     * Gets the project attribute (e.g. tag) associated with the given {@link String} key.
     *
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
                + "id=" + id + '\n'
                + "externalId='" + externalId + "'\n"
                + "externalLink='" + externalLink + "'\n"
                + "status='" + status + "'\n"
                + "project=" + project.getProjectName() + '}';
    }

}
