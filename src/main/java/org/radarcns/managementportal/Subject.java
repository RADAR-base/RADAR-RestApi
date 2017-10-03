package org.radarcns.managementportal;

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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.logging.Logger;

import okhttp3.Response;

public class Subject {

    public static final String HUMAN_READABLE_IDENTIFIER_KEY = "Human-readable-identifier";

    @JsonProperty("subjectId")
    private final String subjectId;
    @JsonProperty("externalId")
    private final Integer externalId;
    @JsonProperty("externalLink")
    private final URL externalLink;
    @JsonProperty("email")
    private final String email;
    @JsonProperty("status")
    private final String status;
    @JsonProperty("projectId")
    private final Integer projectId;
    @JsonProperty("projectName")
    private final String projectName;
    @JsonProperty("sources")
    private final List<Source> sources;
    @JsonProperty("attributes")
    private final List<Tag> attributes;


    /**
     * Constructor.
     * @param subjectId {@link String} representing Management Portal Subject identifier
     * @param externalId {@link Integer} representing the REDCap Record identifier
     * @param externalLink {@link URL} pointing the REDCap integration form / instrument
     * @param attributes {@link List<Tag>} representing the value associated with
     *      {@link #HUMAN_READABLE_IDENTIFIER_KEY}
     */
    public Subject(String subjectId, Integer externalId, URL externalLink,
            List<Tag> attributes, String status, Integer projectId, String projectName, List<Source> sources, String email) {
        this.subjectId = subjectId;
        this.externalId = externalId;
        this.externalLink = externalLink;
        this.attributes = attributes;
        this.status = status;
        this.projectId = projectId;
        this.projectName = projectName;
        this.sources = sources;
        this.email = email;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public Integer getExternalId() {
        return externalId;
    }

    public URL getExternalLink() {
        return externalLink;
    }

    public String getEmail() {
        return email;
    }

    public List<Tag> getAttributes() {
        return attributes;
    }

    public String getStatus() {
        return status;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public List<Source> getSources() {
        return sources;
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
        Optional<Tag> tag = attributes.stream()
                .filter(item -> item.getKey().equals(key)).findFirst();

        return tag.isPresent() ? tag.get().getValue() : null;
    }

    /**
     * Generates the {@link JsonNode} representation of the current instance.
     * @return {@link JsonNode} serialising this object
     * @throws IOException in case the serialisation cannot be complete
     */
    @JsonIgnore
    public JsonNode getJson() throws IOException {
        return new ObjectMapper().readTree(getJsonString());
    }

    /**
     * Generates the JSON {@link String} representation of the current instance.
     * @return {@link String} serialising this object
     * @throws IOException in case the serialisation cannot be complete
     */
    @JsonIgnore
    public String getJsonString() throws IOException {
        return new ObjectMapper().writeValueAsString(this);
    }

    /**
     * Converts the {@link Response#body()} to a {@link Subject} entity.
     * @param response {@link Response} that has to be converted
     * @return {@link Subject} stored in the {@link Response#body()}
     * @throws IOException in case the conversion cannot be computed
     */
    @JsonIgnore
    public static Subject getObject(Response response) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        byte[] body = response.body().bytes();
        response.close();
        return mapper.readValue(body, Subject.class);
    }


    /**
     * Converts the JSON {@link String} to a {@link Subject} entity.
     * @param jsonString {@link String} that has to be converted
     * @return {@link Subject} stored in the JSON {@link String}
     * @throws IOException in case the conversion cannot be computed
     */
    @JsonIgnore
    public static Subject createSubjectFromJson(String jsonString) throws IOException {

        // TODO Use Jackson for Serialization and Deserialization

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        JsonFactory jsonFactory = objectMapper.getFactory();
        JsonParser jp = jsonFactory.createParser(jsonString);

        JsonNode root = objectMapper.readTree(jp);

        // Integer id = new Integer(root.path("id").asInt());
        String subjectId = root.path("login").asText();
        Integer externalId = new Integer(root.path("externalId").asInt());
        URL externalLink = null;
        try {
            externalLink = new URL(root.path("externalLink").asText());
        } catch (MalformedURLException exc) {
            Logger.getGlobal().warning("External Link is Null");
        }
        String status = root.path("status").asText();
        String email = root.path("email").asText();
        List<Tag> attributesList = new ArrayList<>();
        JsonNode attributes = root.path("attributes");
        if(attributes.has(0)) {
            attributesList = Collections.singletonList(new Tag(attributes.path(0).findValue("key")
                    .asText(HUMAN_READABLE_IDENTIFIER_KEY),
                    attributes.path(0).findValue("value").asText("NULL")));
        }

        JsonNode project = root.path("project");
        Integer projectId = new Integer(project.path("id").asInt());
        String projectName = project.path("projectName").asText();

        JsonNode sources = root.path("sources");
        Iterator<JsonNode> elements = sources.elements();

        ArrayList<Source> sourceList = new ArrayList<>();

        while(elements.hasNext()){
                JsonNode currentSource = elements.next();
                Source source = Source.getObject(currentSource.toString());
                // TODO Update this when more source info is available
                sourceList.add(source);
        }
        return new Subject(subjectId, externalId, externalLink, attributesList, status, projectId,
                projectName, sourceList, email);
    }

    /**
     * Converts the JSON {@link String} to a {@link ArrayList<Subject>} entity.
     * @param jsonString {@link String} that has to be converted
     * @return {@link ArrayList<Subject>} stored in the JSON {@link String}
     * @throws IOException in case the conversion cannot be computed
     */
    @JsonIgnore
    public static ArrayList<Subject> getAllSubjectsFromJson(String jsonString) throws IOException{

        ArrayList<Subject> allSubjects = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        JsonFactory jsonFactory = objectMapper.getFactory();
        JsonParser jp = jsonFactory.createParser(jsonString);

        JsonNode root = objectMapper.readTree(jp);

        Iterator<JsonNode> elements = root.elements();

        while (elements.hasNext()) {
            JsonNode currentSubject = elements.next();
            Subject subject = createSubjectFromJson(currentSubject.toString());
            allSubjects.add(subject);
        }

        return allSubjects;
    }
}
