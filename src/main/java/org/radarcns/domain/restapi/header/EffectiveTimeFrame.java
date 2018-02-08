package org.radarcns.domain.restapi.header;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import org.radarcns.util.RadarConverter;

public class EffectiveTimeFrame {

    @JsonProperty
    private String startDateTime;

    @JsonProperty
    private String endDateTime;

    public EffectiveTimeFrame() {
        // default constructor for json
    }

    public EffectiveTimeFrame(String startDateTime, String endDateTime) {
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
    }

    public EffectiveTimeFrame(Date startDateTime, Date endDateTime) {
        this.startDateTime = RadarConverter.getISO8601(startDateTime);
        this.endDateTime = RadarConverter.getISO8601(endDateTime);
    }

    public EffectiveTimeFrame(Long startDateTime, Long endDateTime) {
        this.startDateTime = RadarConverter.getISO8601(startDateTime);
        this.endDateTime = RadarConverter.getISO8601(endDateTime);
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
