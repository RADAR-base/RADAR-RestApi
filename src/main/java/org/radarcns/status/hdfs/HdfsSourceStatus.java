package org.radarcns.status.hdfs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class HdfsSourceStatus {
    @JsonProperty
    private String sourceId;
    @JsonProperty
    private String status;
    @JsonProperty
    private String lastUpdate;
    @JsonProperty
    private long count;
    @JsonProperty
    private long total;
    @JsonIgnore
    private ZonedDateTime timestamp;

    /**
     * Constructor.
     * @param sourceId sourceId
     * @param status sourceId status
     * @param lastUpdate late updated timestamp
     * @param count number of records
     * @param total total records
     */
    public HdfsSourceStatus(String sourceId, String status, ZonedDateTime lastUpdate, long count,
            long total) {
        this.sourceId = sourceId;
        this.status = status;
        this.lastUpdate = lastUpdate.format(DateTimeFormatter.ISO_INSTANT);
        this.count = count;
        this.total = total;
        this.timestamp = lastUpdate;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(ZonedDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HdfsSourceStatus that = (HdfsSourceStatus) o;
        return count == that.count &&
                total == that.total &&
                Objects.equals(sourceId, that.sourceId) &&
                Objects.equals(status, that.status) &&
                Objects.equals(lastUpdate, that.lastUpdate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceId, status, lastUpdate, count, total);
    }
}
