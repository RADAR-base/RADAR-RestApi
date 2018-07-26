package org.radarcns.domain.restapi.header;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import org.radarcns.domain.restapi.TimeWindow;

public class Header {

    /**
     * Subject identifier.
     */
    @JsonProperty
    public String subjectId;
    /**
     * Source identifier.
     */
    @JsonProperty
    public String sourceId;
    /**
     * Project identifier.
     */
    @JsonProperty
    public String projectId;
    /**
     * SourceType information.
     */
    @JsonProperty
    public String sourceType;
    /**
     * Unit used by the sourceType.
     */
    @JsonProperty
    public String unit;
    /**
     * Time interval between two consecutive samples.
     */
    @JsonProperty
    public TimeWindow timeWindow;
    /**
     * Timestamps of the first and the last samples contained in the dataset.
     */
    @JsonProperty
    public TimeFrame effectiveTimeFrame;

    /**
     * Timestamps of the request if provided.
     */
    @JsonProperty
    public TimeFrame timeFrame;

    public Header() {
    }

    /**
     * All-args constructor.
     *
     * @param subjectId Subject identifier.
     * @param sourceId Source identifier.
     * @param sourceType Sourcetype information, it can be a device or assessment name.
     * @param unit Unit used by the sourceType.
     * @param timeWindow Time interval between two consecutive samples.
     * @param timeFrame Timestamps of request.
     * @param effectiveTimeFrame Timestamps of the first and the last samples in the data-set.
     */
    @SuppressWarnings("PMD.ExcessiveParameterList")
    public Header(String projectId, String subjectId, String sourceId, String sourceType,
            String unit, TimeWindow timeWindow, TimeFrame timeFrame,
            TimeFrame effectiveTimeFrame) {
        this.projectId = projectId;
        this.subjectId = subjectId;
        this.sourceId = sourceId;
        this.sourceType = sourceType;
        this.unit = unit;
        this.timeWindow = timeWindow;
        this.timeFrame = timeFrame;
        this.effectiveTimeFrame = effectiveTimeFrame;
    }

    public String getProjectId() {
        return projectId;
    }

    public Header projectId(String projectId) {
        this.projectId = projectId;
        return this;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public Header subjectId(String subjectId) {
        this.subjectId = subjectId;
        return this;
    }

    public String getSourceId() {
        return sourceId;
    }

    public Header sourceId(String sourceId) {
        this.sourceId = sourceId;
        return this;
    }

    public String getSourceType() {
        return sourceType;
    }

    public Header sourceType(String sourceType) {
        this.sourceType = sourceType;
        return this;
    }

    public String getUnit() {
        return unit;
    }

    public Header unit(String unit) {
        this.unit = unit;
        return this;
    }

    public TimeWindow getTimeWindow() {
        return timeWindow;
    }

    public Header timeWindow(TimeWindow timeWindow) {
        this.timeWindow = timeWindow;
        return this;
    }

    public TimeFrame getEffectiveTimeFrame() {
        return effectiveTimeFrame;
    }

    public Header effectiveTimeFrame(
            TimeFrame effectiveTimeFrame) {
        this.effectiveTimeFrame = effectiveTimeFrame;
        return this;
    }

    public TimeFrame getTimeFrame() {
        return timeFrame;
    }

    public Header timeFrame(TimeFrame timeFrame) {
        this.timeFrame = timeFrame;
        return this;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Header that = (Header) o;

        return Objects.equals(subjectId, that.subjectId)
                && Objects.equals(projectId, that.projectId)
                && Objects.equals(sourceId, that.sourceId)
                && Objects.equals(sourceType, that.sourceType)
                && Objects.equals(unit, that.unit)
                && Objects.equals(timeWindow, that.timeWindow)
                && Objects.equals(timeFrame, that.timeFrame)
                && Objects.equals(effectiveTimeFrame, that.effectiveTimeFrame);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subjectId, projectId, sourceId, sourceType, unit, timeWindow, timeFrame,
                effectiveTimeFrame);
    }
}
