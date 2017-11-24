package org.radarcns.managementportal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.radarcns.managementportal.util.UrlDeseralizer;
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

    private static final ObjectMapper mapper;

    static {
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

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

    /**
     * Generates the {@link JsonNode} representation of the current instance.
     * @return {@link JsonNode} serialising this object
     * @throws IOException in case the serialisation cannot be complete
     */
    @JsonIgnore
    public JsonNode getJson() throws IOException {
        return mapper.readTree(getJsonString());
    }

    /**
     * Generates the JSON {@link String} representation of the current instance.
     * @return {@link String} serialising this object
     * @throws IOException in case the serialisation cannot be complete
     */
    @JsonIgnore
    public String getJsonString() throws IOException {
        return mapper.writeValueAsString(this);
    }

    /**
     * Converts the {@link String} to a {@link Subject} entity.
     * @param response {@link String} that has to be converted
     * @return {@link Subject} stored in the {@link String}
     * @throws IOException in case the conversion cannot be computed
     */
    @JsonIgnore
    public static Subject getObject(String response) throws IOException {
        return mapper.readValue(response, Subject.class);
    }

    /**
     * Converts the JSON {@link String} to a {@link ArrayList} of {@link Subject} entity.
     * @param jsonString {@link String} that has to be converted
     * @return {@link ArrayList} of {@link Subject} stored in the JSON {@link String}
     * @throws IOException in case the conversion cannot be computed
     */
    @JsonIgnore
    public static ArrayList<Subject> getAllSubjectsFromJson(String jsonString) throws
            IOException {

        ArrayList<Subject> allSubjects = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        JsonFactory jsonFactory = objectMapper.getFactory();
        JsonParser jp = jsonFactory.createParser(jsonString);

        JsonNode root = objectMapper.readTree(jp);

        Iterator<JsonNode> elements = root.elements();

        while (elements.hasNext()) {
            JsonNode currentSubject = elements.next();
            Subject subject = getObject(currentSubject.toString());
            allSubjects.add(subject);
        }

        return allSubjects;
    }
}
