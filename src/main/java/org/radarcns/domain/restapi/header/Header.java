package org.radarcns.domain.restapi.header;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.radarcns.domain.restapi.TimeWindow;

public class Header {

    /**
     * Source identifier.
     */
    @JsonProperty
    public String subjectId;
    /**
     * Source identifier.
     */
    @JsonProperty
    public String sourceId;
    /**
     * Source identifier.
     */
    @JsonProperty
    public String projectId;
    /**
     * Source information, it can be a device or assessment name.
     */
    @JsonProperty
    public String sourceType;

    /**
     * Report the sensor name or specific type of assessment.
     */
    @JsonProperty
    public String sourceDataType;

    /**
     * Statical value expressed by samples.
     */
    @JsonProperty
    public DescriptiveStatistic descriptiveStatistic;
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
    public EffectiveTimeFrame effectiveTimeFrame;

    public Header() {
    }

    /**
     * All-args constructor.
     *
     * @param subjectId Subject identifier.
     * @param sourceId Source identifier.
     * @param sourceType Source information, it can be a device or assessment name.
     * @param sourceDataType Source information, it can be a device or assessment name.
     * @param descriptiveStatistic Statical value expressed by samples.
     * @param unit Unit used by the sourceType.
     * @param timeWindow Time interval between two consecutive samples.
     * @param effectiveTimeFrame Timestamps of the first and the last samples contained in the
     * dataset.
     */
    public Header(String projectId, String subjectId, String sourceId, String sourceType, String
            sourceDataType,
            DescriptiveStatistic descriptiveStatistic, String unit, TimeWindow timeWindow,
            EffectiveTimeFrame effectiveTimeFrame) {
        this.projectId = projectId;
        this.subjectId = subjectId;
        this.sourceId = sourceId;
        this.sourceType = sourceType;
        this.sourceDataType = sourceDataType;
        this.descriptiveStatistic = descriptiveStatistic;
        this.unit = unit;
        this.timeWindow = timeWindow;
        this.effectiveTimeFrame = effectiveTimeFrame;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getSourceDataType() {
        return sourceDataType;
    }

    public void setSourceDataType(String sourceDataType) {
        this.sourceDataType = sourceDataType;
    }

    public DescriptiveStatistic getDescriptiveStatistic() {
        return descriptiveStatistic;
    }

    public void setDescriptiveStatistic(
            DescriptiveStatistic descriptiveStatistic) {
        this.descriptiveStatistic = descriptiveStatistic;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public TimeWindow getTimeWindow() {
        return timeWindow;
    }

    public void setTimeWindow(TimeWindow timeWindow) {
        this.timeWindow = timeWindow;
    }

    public EffectiveTimeFrame getEffectiveTimeFrame() {
        return effectiveTimeFrame;
    }

    public void setEffectiveTimeFrame(
            EffectiveTimeFrame effectiveTimeFrame) {
        this.effectiveTimeFrame = effectiveTimeFrame;
    }
}
