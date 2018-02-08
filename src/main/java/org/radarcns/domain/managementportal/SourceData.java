package org.radarcns.domain.managementportal;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SourceData {

    @JsonProperty
    private Long id;

    @JsonProperty
    private String sourceDataType;

    @JsonProperty
    private String sourceDataName;

    @JsonProperty
    private Double frequency;

    @JsonProperty
    private String unit;

    @JsonProperty
    private String processingState;

    @JsonProperty
    private String keySchema;

    @JsonProperty
    private String valueSchema;

    @JsonProperty
    private String topic;

    @JsonProperty
    private Boolean enabled;

    public Long getId() {
        return id;
    }

    public String getSourceDataType() {
        return sourceDataType;
    }

    public String getSourceDataName() {
        return sourceDataName;
    }

    public Double getFrequency() {
        return frequency;
    }

    public String getUnit() {
        return unit;
    }

    public String getProcessingState() {
        return processingState;
    }

    public String getKeySchema() {
        return keySchema;
    }

    public String getValueSchema() {
        return valueSchema;
    }

    public String getTopic() {
        return topic;
    }

    public Boolean getEnabled() {
        return enabled;
    }
}