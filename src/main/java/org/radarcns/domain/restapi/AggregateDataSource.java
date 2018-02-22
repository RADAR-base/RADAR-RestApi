package org.radarcns.domain.restapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class AggregateDataSource {

    @JsonProperty
    private String sourceId;

    @JsonProperty
    private List<String> sourceDataName;

    public AggregateDataSource() {
    }

    public AggregateDataSource(String sourceId, List<String> sourceDataName) {
        this.sourceId = sourceId;
        this.sourceDataName = sourceDataName;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public void setSourceDataName(List<String> sourceDataName) {
        this.sourceDataName = sourceDataName;
    }

    public List<String> getSourceDataName() {
        return sourceDataName;
    }

}