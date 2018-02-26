package org.radarcns.domain.restapi;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.stream.Collectors;
import org.radarcns.domain.restapi.format.SourceData;

public class AggregateDataSource {

    @JsonProperty
    private String sourceId;

    @JsonProperty
    private List<SourceData> sourceData;

    public AggregateDataSource() {
    }

    public AggregateDataSource(String sourceId, List<SourceData> sourceData) {
        this.sourceId = sourceId;
        this.sourceData = sourceData;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    @JsonIgnore
    public void setSourceDataNames(List<String> sourceData) {
        this.sourceData = sourceData.stream().map(SourceData::new).collect(Collectors.toList());
    }

    public void setSourceData(List<SourceData> sourceData) {
        this.sourceData = sourceData;
    }

    public List<SourceData> getSourceData() {
        return sourceData;
    }

}