package org.radarcns.domain.restapi.header;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import org.radarcns.util.RadarConverter;

public class EffectiveTimeFrame {

    @JsonProperty
    private Date startDateTime;

    @JsonProperty
    private Date endDateTime;

    public EffectiveTimeFrame() {
        // default constructor for json
    }

    public EffectiveTimeFrame(String startDateTime, String endDateTime) {
        this.startDateTime = RadarConverter.getISO8601(startDateTime);
        this.endDateTime = RadarConverter.getISO8601(endDateTime);
    }

    public EffectiveTimeFrame(Date startDateTime, Date endDateTime) {
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
    }

    public EffectiveTimeFrame(Long startDateTime, Long endDateTime) {
        this.startDateTime = RadarConverter.getISO8601ToDate(startDateTime);
        this.endDateTime = RadarConverter.getISO8601ToDate(endDateTime);
    }

    public Date getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(Date startDateTime) {
        this.startDateTime = startDateTime;
    }

    public Date getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(Date endDateTime) {
        this.endDateTime = endDateTime;
    }
}
