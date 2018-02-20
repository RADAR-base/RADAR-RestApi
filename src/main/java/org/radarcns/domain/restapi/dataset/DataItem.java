package org.radarcns.domain.restapi.dataset;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Objects;

public class DataItem {


    @JsonProperty
    private Object sample;

    @JsonProperty
    private Instant startDateTime;


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
    public DataItem(java.lang.Object sample, Instant startDateTime) {
        this.sample = sample;
        this.startDateTime = startDateTime;
    }

    public Object getSample() {
        return sample;
    }

    public void setSample(Object sample) {
        this.sample = sample;
    }

    public Instant getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(Instant startDateTime) {
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
        DataItem dataItem = (DataItem) o;

        return Objects.equals(sample, dataItem.sample)
                && Objects.equals(startDateTime, dataItem.startDateTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sample, startDateTime);
    }
}
