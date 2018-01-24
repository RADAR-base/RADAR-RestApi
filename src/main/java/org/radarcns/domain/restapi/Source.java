package org.radarcns.domain.restapi;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Source {

    @JsonProperty
    private String sourceId;

    @JsonProperty
    private String sourceName;

    @JsonProperty
    private String sourceTypeProducer;

    @JsonProperty
    private String sourceTypeModel;

    @JsonProperty
    private String sourceTypeCatalogVersion;

    @JsonProperty
    private Boolean assigned;

    @JsonProperty
    private String status;

    @JsonProperty
    private EffectiveTimeFrame effectiveTimeFrame;

    public Source sourceId(String sourceId) {
        this.sourceId = sourceId;
        return this;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public Source sourceName(String sourceName) {
        this.sourceName = sourceName;
        return this;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public Source sourceTypeProducer(String sourceTypeProducer) {
        this.sourceTypeProducer = sourceTypeProducer;
        return this;
    }

    public String getSourceTypeProducer() {
        return sourceTypeProducer;
    }

    public void setSourceTypeProducer(String sourceTypeProducer) {
        this.sourceTypeProducer = sourceTypeProducer;
    }

    public Source sourceTypeModel(String sourceTypeModel) {
        this.sourceTypeModel = sourceTypeModel;
        return this;
    }

    public String getSourceTypeModel() {
        return sourceTypeModel;
    }

    public void setSourceTypeModel(String sourceTypeModel) {
        this.sourceTypeModel = sourceTypeModel;
    }

    public Source sourceTypeCatalogVersion(String sourceTypeCatalogVersion) {
        this.sourceTypeCatalogVersion = sourceTypeCatalogVersion;
        return this;
    }

    public String getSourceTypeCatalogVersion() {
        return sourceTypeCatalogVersion;
    }

    public void setSourceTypeCatalogVersion(String sourceTypeCatalogVersion) {
        this.sourceTypeCatalogVersion = sourceTypeCatalogVersion;
    }

    public Source assigned(Boolean assigned) {
        this.assigned = assigned;
        return this;
    }

    public Boolean getAssigned() {
        return assigned;
    }

    public void setAssigned(Boolean assigned) {
        this.assigned = assigned;
    }

    public Source status(String status) {
        this.status = status;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Source effectiveTimeFrame(EffectiveTimeFrame effectiveTimeFrame) {
        this.effectiveTimeFrame = effectiveTimeFrame;
        return this;
    }

    public EffectiveTimeFrame getEffectiveTimeFrame() {
        return effectiveTimeFrame;
    }

    public void setEffectiveTimeFrame(EffectiveTimeFrame effectiveTimeFrame) {
        this.effectiveTimeFrame = effectiveTimeFrame;
    }
}
