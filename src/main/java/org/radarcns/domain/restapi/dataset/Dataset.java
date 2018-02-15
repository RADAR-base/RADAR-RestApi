package org.radarcns.domain.restapi.dataset;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.radarcns.domain.restapi.header.Header;

public class Dataset {

    @JsonProperty
    public Header header;

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
            @JsonProperty("header") Header header,
            @JsonProperty("dataset") List<DataItem> dataset) {
        this.header = header;
        this.dataset = dataset;
    }

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public List<DataItem> getDataset() {
        return dataset;
    }

    public void setDataset(List<DataItem> dataset) {
        this.dataset = dataset;
    }
}
