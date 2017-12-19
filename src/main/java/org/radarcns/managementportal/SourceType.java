package org.radarcns.managementportal;

import com.fasterxml.jackson.annotation.JsonProperty;



/**
 * Java class defining a RADAR Management Portal Device Type.
 */

public class SourceType {

    @JsonProperty("id")
    private final Integer id;
    @JsonProperty("producer")
    private final String producer;
    @JsonProperty("model")
    private final String model;
    @JsonProperty("catalogVersion")
    private final String catalogVersion;
    @JsonProperty("sourceTypeScope")
    private final String sourceTypeScope;
    @JsonProperty("canRegisterDynamically")
    private final Boolean canRegisterDynamically;

    protected SourceType(
            @JsonProperty("id") Integer id,
            @JsonProperty("producer") String producer,
            @JsonProperty("model") String model,
            @JsonProperty("catalogVersion") String catalogVersion,
            @JsonProperty("sourceTypeScope") String sourceTypeScope,
            @JsonProperty("canRegisterDynamically") Boolean canRegisterDynamically) {
        this.id = id;
        this.producer = producer;
        this.model = model;
        this.catalogVersion = catalogVersion;
        this.sourceTypeScope = sourceTypeScope;
        this.canRegisterDynamically = canRegisterDynamically;
    }

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
