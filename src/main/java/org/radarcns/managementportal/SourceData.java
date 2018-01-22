package org.radarcns.managementportal;

public class SourceData {

    private Long id;

    private String sourceDataType;

    private String sourceDataName;

    private Double frequency;

    private String unit;

    private String processingState;

    private String keySchema;

    private String valueSchema;

    private String topic;

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