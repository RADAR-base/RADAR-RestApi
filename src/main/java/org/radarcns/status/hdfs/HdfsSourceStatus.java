package org.radarcns.status.hdfs;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Objects;

public class HdfsSourceStatus {
    public enum Status {
        HEALTHY, UNHEALTHY
    }

    @JsonProperty
    private String sourceId;
    @JsonProperty
    private Status status;
    @JsonProperty
    private String lastUpdate;
    @JsonProperty
    private long count;
    @JsonProperty
    private long total;
    @JsonIgnore
    private Instant timestamp;

    @JsonCreator
    public HdfsSourceStatus(
            @JsonProperty("sourceId") String sourceId,
            @JsonProperty("status") Status status,
            @JsonProperty("lastUpdate") String lastUpdate,
            @JsonProperty("count") long count,
            @JsonProperty("total") long total) {
        this.sourceId = sourceId;
        this.status = status;
        this.lastUpdate = lastUpdate;
        this.count = count;
        this.total = total;
        this.timestamp = Instant.parse(lastUpdate);
    }

    /**
     * Constructor.
     * @param sourceId sourceId
     * @param status sourceId status
     * @param lastUpdate late updated timestamp
     * @param count number of records
     * @param total total records
     */
    public HdfsSourceStatus(String sourceId, Status status, Instant lastUpdate, long count,
            long total) {
        this.sourceId = sourceId;
        this.status = status;
        this.lastUpdate = lastUpdate.toString();
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

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
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

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
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
