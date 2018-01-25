package org.radarcns.domain.restapi.dataset;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.radarcns.domain.restapi.header.Header;

public class Dataset {

    @JsonProperty
    public Header header;

    @JsonProperty
    public List<DataItem> dataset;

    /**
     * Default constructor.  Note that this does not initialize fields
     * to their default values from the schema.  If that is desired then
     * one should use <code>newBuilder()</code>.
     */
    public Dataset() {}

    /**
     * All-args constructor.
     * @param header Information useful to contextualise the data set.
     * @param dataset Collection of samples.
     */
    public Dataset(Header header, List<DataItem> dataset) {
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
