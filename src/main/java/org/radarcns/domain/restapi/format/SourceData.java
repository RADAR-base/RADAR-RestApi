package org.radarcns.domain.restapi.format;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SourceData {

    @JsonProperty
    private String name;

    @JsonProperty
    private String type;

    public SourceData() {
    }

    public SourceData(String name) {
        this.name = name;
    }

    public SourceData(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
