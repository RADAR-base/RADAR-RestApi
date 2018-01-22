package org.radarcns.managementportal;

import org.radarcns.catalogue.ProcessingState;
import org.radarcns.catalogue.Unit;

public class SourceData {

    private Long id;

    private String sourceDataType;

    private String sourceDataName;

    private Double frequency;

    private Unit unit;

    private ProcessingState processingState;

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

    public Unit getUnit() {
        return unit;
    }

    public ProcessingState getProcessingState() {
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