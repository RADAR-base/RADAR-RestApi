package org.radarcns.domain.managementportal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Objects;


/**
 * Java class defining a RADAR Management Portal Device Type.
 */

public class SourceType {

    @JsonIgnore
    private static final String SOURCE_STATISTICS_MONITOR = "source_statistics";
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

    @JsonProperty
    private List<SourceData> sourceData;

    @JsonProperty
    private String sourceStatisticsMonitorTopic;

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

    public List<SourceData> getSourceData() {
        return sourceData;
    }

    public String getSourceStatisticsMonitorTopic() {
        // TODO remove setting default logic
        if(Objects.isNull(sourceStatisticsMonitorTopic) || sourceStatisticsMonitorTopic.isEmpty()){
            return SOURCE_STATISTICS_MONITOR+"_"+this.producer+"_"+this.model.toLowerCase();
        }
        return sourceStatisticsMonitorTopic;
    }

    @JsonIgnore
    public SourceTypeIdentifier getSourceTypeIdentifier() {
        return new SourceTypeIdentifier(producer, model, catalogVersion);
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setProducer(String producer) {
        this.producer = producer;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setCatalogVersion(String catalogVersion) {
        this.catalogVersion = catalogVersion;
    }

    public void setSourceTypeScope(String sourceTypeScope) {
        this.sourceTypeScope = sourceTypeScope;
    }

    public void setCanRegisterDynamically(Boolean canRegisterDynamically) {
        this.canRegisterDynamically = canRegisterDynamically;
    }

    public void setSourceData(List<SourceData> sourceData) {
        this.sourceData = sourceData;
    }

    public void setSourceStatisticsMonitorTopic(String sourceStatisticsMonitorTopic) {
        this.sourceStatisticsMonitorTopic = sourceStatisticsMonitorTopic;
    }
}
