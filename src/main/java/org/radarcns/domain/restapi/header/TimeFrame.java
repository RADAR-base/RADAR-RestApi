package org.radarcns.domain.restapi.header;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;

/**
 * Time frame between two instants. The start time is inclusive, the end time is exclusive.
 */
public class TimeFrame {

    @JsonProperty
    private Instant startDateTime;

    @JsonProperty
    private Instant endDateTime;

    public TimeFrame() {
        // default constructor for json
    }

    /**
     * Time frame between given instants. The start must fall before the end.
     * @param startDateTime start date time, may be {@code null}.
     * @param endDateTime end date time, may be {@code null}.
     */
    public TimeFrame(Instant startDateTime, Instant endDateTime) {
        if (startDateTime != null && endDateTime != null && !startDateTime.isBefore(endDateTime)) {
            throw new IllegalArgumentException("Time frame must start before it ends; "
                    + startDateTime + " is not before " + endDateTime + ".");
        }
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
    }

    /**
     * Time frame between given dates. The start must fall before the end.
     * @param startDateTime start date, may be {@code null}.
     * @param endDateTime end date, may be {@code null}.
     */
    public TimeFrame(Date startDateTime, Date endDateTime) {
        this(startDateTime == null ? null : startDateTime.toInstant(),
                endDateTime == null ? null : endDateTime.toInstant());
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

    /**
     * Get the duration between the start and end time.
     * @return duration or {@code null} if either start or end times are {@code null}.
     */
    public Duration getDuration() {
        if (startDateTime == null || endDateTime == null) {
            return null;
        }
        return Duration.between(startDateTime, endDateTime);
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

    @Override
    public String toString() {
        return "TimeFrame{"
                + "startDateTime=" + startDateTime
                + ", endDateTime=" + endDateTime
                + '}';
    }

    /**
     * Get a time frame that spans this time frame and the provided one.
     * If one time frame already spans the other or the other is {@code null}, that time frame is
     * returned. Otherwise a new time frame that spans both time frames is returned.
     * @param first TimeFrame to span, may be {@code null}
     * @param second TimeFrame to span, may be {@code null}
     * @return time frame spanning both time frames, or {@code null} if both time frames were
     *         {@code null}.
     */
    public static TimeFrame span(TimeFrame first, TimeFrame second) {
        if (first == null) {
            return second;
        }
        if (second == null) {
            return first;
        }
        boolean firstStartsFirst = first.startDateTime != null
                && (second.startDateTime == null
                || first.startDateTime.isBefore(second.startDateTime));
        boolean firstEndsLast = first.endDateTime != null
                && (second.endDateTime == null
                || first.endDateTime.isAfter(second.endDateTime));
        if (firstStartsFirst && firstEndsLast) {
            return first;
        } else if (!firstStartsFirst && !firstEndsLast) {
            return second;
        } else if (firstStartsFirst) {
            return new TimeFrame(first.startDateTime, second.endDateTime);
        } else {
            return new TimeFrame(second.startDateTime, first.endDateTime);
        }
    }
}
