package org.radarcns.domain.restapi.dataset;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Objects;

public class DataItem {
    @JsonProperty
    private Object value;

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
     * @param value Sample value.
     * @param startDateTime Point in time (ISO8601) with UTC timezone of first value in data-set.
     */
    public DataItem(java.lang.Object value, Instant startDateTime) {
        this.value = value;
        this.startDateTime = startDateTime;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
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

        return Objects.equals(value, dataItem.value)
                && Objects.equals(startDateTime, dataItem.startDateTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, startDateTime);
    }
}
