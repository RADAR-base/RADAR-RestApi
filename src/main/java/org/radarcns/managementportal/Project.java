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

package org.radarcns.managementportal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;

/**
 * Java class defining a RADAR Management Portal Project.
 */
public class Project {

    @JsonProperty("id")
    private Integer id;
    @JsonProperty("projectName")
    private String projectName;
    @JsonProperty("organization")
    private String organization;
    @JsonProperty("location")
    private String location;
    @JsonProperty("attributes")
    private List<Tag> attributes;
    @JsonProperty("projectStatus")
    private String projectStatus;
    @JsonProperty("sourceTypes")
    private List<SourceType> sourceTypes;

    public void setId(Integer id) {
        this.id = id;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setAttributes(List<Tag> attributes) {
        this.attributes = attributes;
    }

    public void setProjectStatus(String projectStatus) {
        this.projectStatus = projectStatus;
    }

    public void setSourceTypes(List<SourceType> sourceTypes) {
        this.sourceTypes = sourceTypes;
    }

    public Integer getId() {
        return id;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getOrganization() {
        return organization;
    }

    public String getLocation() {
        return location;
    }

    public List<Tag> getAttributes() {
        return attributes;
    }

    public String getProjectStatus() {
        return projectStatus;
    }

    public List<SourceType> getSourceTypes() {
        return sourceTypes;
    }

    /**
     * Gets the project attribute (e.g. tag) associated with the given {@link String} key.
     *
     * @param key {@link String} tag key
     * @return {@link String} value associated with the given key
     */
    @JsonIgnore
    public String getAttribute(String key) {
        Optional<Tag> tag = attributes.stream()
                .filter(item -> item.getKey().equals(key)).findFirst();

        return tag.isPresent() ? tag.get().getValue() : null;
    }

    @Override
    public String toString() {
        return "Project{" + '\n'
                + "id=" + id + '\n'
                + "projectName='" + projectName + "'\n"
                + "organization='" + organization + "'\n"
                + "location='" + location + "'\n"
                + "attributes=" + attributes + '}';
    }


}
