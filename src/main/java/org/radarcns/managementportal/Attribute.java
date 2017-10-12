package org.radarcns.managementportal;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Attribute {
    @JsonProperty("Human-readable-identifier")
    private final String humanRedableIdentifier;

    public Attribute(
            @JsonProperty("Human-readable-identifier") String identifier
    ){
        this.humanRedableIdentifier = identifier;
    }

    public String getHumanRedableIdentifier() {
        return humanRedableIdentifier;
    }
}
