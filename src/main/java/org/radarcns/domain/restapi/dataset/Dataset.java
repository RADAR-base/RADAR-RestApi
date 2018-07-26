package org.radarcns.domain.restapi.dataset;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

import org.radarcns.domain.restapi.header.DataSetHeader;

public class Dataset {

    @JsonProperty
    public DataSetHeader header;

    @JsonProperty
    public List<DataItem> dataset;

    /**
     * All-args constructor.
     *
     * @param header Information useful to contextualise the data set.
     * @param dataset Collection of samples.
     */
    @JsonCreator
    public Dataset(
            @JsonProperty("header") DataSetHeader header,
            @JsonProperty("dataset") List<DataItem> dataset) {
        this.header = header;
        this.dataset = dataset;
    }

    public DataSetHeader getHeader() {
        return header;
    }

    public void setHeader(DataSetHeader header) {
        this.header = header;
    }

    public List<DataItem> getDataset() {
        return dataset;
    }

    public void setDataset(List<DataItem> dataset) {
        this.dataset = dataset;
    }
}
