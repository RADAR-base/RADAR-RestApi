package org.radarcns.managementportal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

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
 * Java class defining a RADAR Management Portal Source.
 */
public class Source {

    @JsonProperty("id")
    private Integer id;
    @JsonProperty("deviceTypeId")
    private String deviceTypeId;
    @JsonProperty("deviceTypeProducer")
    private String deviceTypeProducer;
    @JsonProperty("deviceTypeModel")
    private String deviceTypeModel;
    @JsonProperty("sourceId")
    private String sourceId;
    @JsonProperty("sourceName")
    private String sourceName;
    @JsonProperty("assigned")
    private Boolean assigned;

    protected Source(){}

    protected Source(
            @JsonProperty("id") Integer id,
            @JsonProperty("deviceTypeId") String deviceTypeId,
            @JsonProperty("deviceTypeProducer") String deviceTypeProducer,
            @JsonProperty("deviceTypeModel") String deviceTypeModel,
            @JsonProperty("sourceId") String sourceId,
            @JsonProperty("sourceName") String sourceName,
            @JsonProperty("assigned")  boolean assigned
    ) {
        this.id = id;
        this.deviceTypeId = deviceTypeId;
        this.deviceTypeModel = deviceTypeModel;
        this.deviceTypeProducer = deviceTypeProducer;
        this.sourceId = sourceId;
        this.sourceName = sourceName;
        this.assigned = assigned;
    }


    public Integer getId() {
        return id;
    }

    public String getDeviceTypeId() {
        return deviceTypeId;
    }

    public String getDeviceTypeProducer() {
        return deviceTypeProducer;
    }

    public String getDeviceTypeModel() {
        return deviceTypeModel;
    }

    public String getSourceId() {
        return sourceId;
    }

    public String getSourceName() {
        return sourceName;
    }

    public Boolean getAssigned() {
        return assigned;
    }

    /**
     * Converts the {@link String} to a {@link Source} entity.
     * @param response {@link String} that has to be converted
     * @return {@link Source} stored in the {@link byte[]}
     * @throws IOException in case the conversion cannot be computed
     */
    @JsonIgnore
    public static Source getObject(String response) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true);
        mapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
        mapper.configure(DeserializationFeature.FAIL_ON_NULL_CREATOR_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper.readValue(response, Source.class);
    }
}
