package org.radarcns.domain.restapi;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EffectiveTimeFrame {

    @JsonProperty
    private String startDateTime;

    @JsonProperty
    private String endDateTime;

    public EffectiveTimeFrame() {
        // default constructor for json
    }

    public EffectiveTimeFrame(String startDateTime , String endDateTime) {
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
    }

    public String getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(String startDateTime) {
        this.startDateTime = startDateTime;
    }

    public String getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(String endDateTime) {
        this.endDateTime = endDateTime;
    }
}
