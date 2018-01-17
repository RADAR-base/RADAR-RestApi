package org.radarcns.managementportal;

import com.fasterxml.jackson.annotation.JsonProperty;

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
    @JsonProperty("sourceTypeId")
    private String sourceTypeId;
    @JsonProperty("sourceTypeProducer")
    private String sourceTypeProducer;
    @JsonProperty("sourceTypeModel")
    private String sourceTypeModel;
    @JsonProperty("sourceTypeCatalogVersion")
    private String sourceTypeCatalogVersion;
    @JsonProperty("sourceId")
    private String sourceId;
    @JsonProperty("sourceName")
    private String sourceName;
    @JsonProperty("assigned")
    private Boolean assigned;


    public Integer getId() {
        return id;
    }

    public String getSourceTypeId() {
        return sourceTypeId;
    }

    public String getSourceTypeProducer() {
        return sourceTypeProducer;
    }

    public String getSourceTypeModel() {
        return sourceTypeModel;
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

    public void setId(Integer id) {
        this.id = id;
    }

    public void setSourceTypeId(String sourceTypeId) {
        this.sourceTypeId = sourceTypeId;
    }

    public void setSourceTypeProducer(String sourceTypeProducer) {
        this.sourceTypeProducer = sourceTypeProducer;
    }

    public void setSourceTypeModel(String sourceTypeModel) {
        this.sourceTypeModel = sourceTypeModel;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getSourceTypeCatalogVersion() {
        return sourceTypeCatalogVersion;
    }

    public void setSourceTypeCatalogVersion(String sourceTypeCatalogVersion) {
        this.sourceTypeCatalogVersion = sourceTypeCatalogVersion;
    }

    public void setAssigned(Boolean assigned) {
        this.assigned = assigned;
    }
}
