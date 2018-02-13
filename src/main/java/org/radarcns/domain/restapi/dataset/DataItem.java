package org.radarcns.domain.restapi.dataset;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import java.util.Objects;

public class DataItem {


    @JsonProperty
    private Object sample;

    @JsonProperty
    private Date startDateTime;


    /**
     * Default constructor.  Note that this does not initialize fields to their default values from
     * the schema.  If that is desired then one should use <code>newBuilder()</code>.
     */
    public DataItem() {
    }

    /**
     * All-args constructor.
     *
     * @param sample Sample value.
     * @param startDateTime Point in time (ISO8601) with UTC timezone of first sample in data-set.
     */
    public DataItem(java.lang.Object sample, Date startDateTime) {
        this.sample = sample;
        this.startDateTime = startDateTime;
    }

    public Object getSample() {
        return sample;
    }

    public void setSample(Object sample) {
        this.sample = sample;
    }

    public Date getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(Date startDateTime) {
        this.startDateTime = startDateTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DataItem sourceType = (DataItem) o;

        return Objects.equals(sample, sourceType.sample)
                && Objects.equals(startDateTime, sourceType.startDateTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sample, startDateTime);
    }
}
