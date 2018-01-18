package org.radarcns.managementportal;

import com.fasterxml.jackson.annotation.JsonProperty;



/**
 * Java class defining a RADAR Management Portal Device Type.
 */

public class SourceType {
    @JsonProperty
    private Integer id;

    @JsonProperty
    private String producer;

    @JsonProperty
    private String model;

    @JsonProperty
    private String catalogVersion;

    @JsonProperty
    private String sourceTypeScope;

    @JsonProperty
    private Boolean canRegisterDynamically;

    public Integer getId() {
        return id;
    }

    public String getProducer() {
        return producer;
    }

    public String getModel() {
        return model;
    }

    public String getCatalogVersion() {
        return catalogVersion;
    }

    public String getSourceTypeScope() {
        return sourceTypeScope;
    }

    public Boolean getCanRegisterDynamically() {
        return canRegisterDynamically;
    }
}
