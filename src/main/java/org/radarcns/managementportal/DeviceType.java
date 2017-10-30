package org.radarcns.managementportal;

import com.fasterxml.jackson.annotation.JsonProperty;



/**
 * Java class defining a RADAR Management Portal Device Type.
 */

public class DeviceType {

    @JsonProperty("id")
    private final Integer id;
    @JsonProperty("deviceProducer")
    private final String deviceProducer;
    @JsonProperty("deviceModel")
    private final String deviceModel;
    @JsonProperty("deviceVersion")
    private final String deviceVersion;
    @JsonProperty("sourceType")
    private final String sourceType;

    protected DeviceType(
            @JsonProperty("id") Integer id,
            @JsonProperty("deviceProducer") String deviceProducer,
            @JsonProperty("deviceModel") String deviceModel,
            @JsonProperty("deviceVersion") String deviceVersion,
            @JsonProperty("sourceType") String sourceType) {
        this.id = id;
        this.deviceProducer = deviceProducer;
        this.deviceModel = deviceModel;
        this.deviceVersion = deviceVersion;
        this.sourceType = sourceType;
    }

    public Integer getId() {
        return id;
    }

    public String getDeviceProducer() {
        return deviceProducer;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public String getDeviceVersion() {
        return deviceVersion;
    }

    public String getSourceType() {
        return sourceType;
    }
}
