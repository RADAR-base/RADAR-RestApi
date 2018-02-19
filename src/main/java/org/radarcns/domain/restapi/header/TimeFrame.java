package org.radarcns.domain.restapi.header;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import org.radarcns.util.RadarConverter;

public class TimeFrame {

    @JsonProperty
    private Instant startDateTime;

    @JsonProperty
    private Instant endDateTime;

    public TimeFrame() {
        // default constructor for json
    }

    public TimeFrame(Instant startDateTime, Instant endDateTime) {
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
    }

    public TimeFrame(Date startDateTime, Date endDateTime) {
        this.startDateTime = startDateTime.toInstant();
        this.endDateTime = startDateTime.toInstant();
    }

    public Instant getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(Instant startDateTime) {
        this.startDateTime = startDateTime;
    }

    public Instant getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(Instant endDateTime) {
        this.endDateTime = endDateTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TimeFrame effectiveTimeFrame = (TimeFrame) o;

        return Objects.equals(startDateTime, effectiveTimeFrame.startDateTime)
                && Objects.equals(endDateTime, effectiveTimeFrame.endDateTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startDateTime, endDateTime);
    }
}
