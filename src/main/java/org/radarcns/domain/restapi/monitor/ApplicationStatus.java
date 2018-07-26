package org.radarcns.domain.restapi.monitor;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import org.radarcns.domain.restapi.ServerStatus;

public class ApplicationStatus {

    /**
     * Hardware identifier of client application.
     */
    @JsonProperty
    private java.lang.String ipAddress = null;
    /**
     * Time since last app start (s).
     */
    @JsonProperty
    private double uptime = 0d;
    /**
     * Server connection status.
     */
    @JsonProperty
    private ServerStatus serverStatus = ServerStatus.UNKNOWN;
    /**
     * Number of records currently being cached.
     */
    @JsonProperty
    private int recordsCached = -1;
    /**
     * Number of records sent since application start.
     */
    @JsonProperty
    private int recordsSent = -1;
    /**
     * Number of unsent records.
     */
    @JsonProperty
    private int recordsUnsent = -1;

    /**
     * Default constructor.
     */
    public ApplicationStatus() {
    }

    /**
     * All-args constructor.
     *
     * @param ipAddress Hardware identifier of client application.
     * @param uptime Time since last app start (s).
     * @param serverStatus Server connection status.
     * @param recordsCached Number of records currently being cached.
     * @param recordsSent Number of records sent since application start.
     * @param recordsUnsent Number of unsent records.
     */
    public ApplicationStatus(java.lang.String ipAddress, java.lang.Double uptime,
            ServerStatus serverStatus,
            java.lang.Integer recordsCached, java.lang.Integer recordsSent,
            java.lang.Integer recordsUnsent) {
        this.ipAddress = ipAddress;
        this.uptime = uptime;
        this.serverStatus = serverStatus;
        this.recordsCached = recordsCached;
        this.recordsSent = recordsSent;
        this.recordsUnsent = recordsUnsent;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public double getUptime() {
        return uptime;
    }

    public void setUptime(double uptime) {
        this.uptime = uptime;
    }

    public ServerStatus getServerStatus() {
        return serverStatus;
    }

    public void setServerStatus(ServerStatus serverStatus) {
        this.serverStatus = serverStatus;
    }

    public int getRecordsCached() {
        return recordsCached;
    }

    public void setRecordsCached(int recordsCached) {
        this.recordsCached = recordsCached;
    }

    public int getRecordsSent() {
        return recordsSent;
    }

    public void setRecordsSent(int recordsSent) {
        this.recordsSent = recordsSent;
    }

    public int getRecordsUnsent() {
        return recordsUnsent;
    }

    public void setRecordsUnsent(int recordsUnsent) {
        this.recordsUnsent = recordsUnsent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ApplicationStatus applicationStatus = (ApplicationStatus) o;

        return Objects.equals(ipAddress, applicationStatus.ipAddress)
                && Objects.equals(uptime, applicationStatus.uptime)
                && Objects.equals(serverStatus, applicationStatus.serverStatus)
                && Objects.equals(recordsCached, applicationStatus.recordsCached)
                && Objects.equals(recordsSent, applicationStatus.recordsSent)
                && Objects.equals(recordsUnsent, applicationStatus.recordsUnsent);
    }

    @Override
    public int hashCode() {
        return Objects
                .hash(ipAddress, uptime, serverStatus, recordsCached, recordsSent, recordsUnsent);
    }
}