package org.radarcns.status;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SourceData {
    @JsonProperty
    private String device;
    @JsonProperty
    private String status;
    @JsonProperty
    private String lastUpdate;
    @JsonProperty
    private Long count;
    @JsonProperty
    private Long total;

    /**
     * Constructor
     * @param device
     * @param status
     * @param lastUpdate
     * @param count
     * @param total
     */
    public SourceData(String device, String status, String lastUpdate, Long count, Long total) {
        this.device = device;
        this.status = status;
        this.lastUpdate = lastUpdate;
        this.count = count;
        this.total = total;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
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

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }
}
