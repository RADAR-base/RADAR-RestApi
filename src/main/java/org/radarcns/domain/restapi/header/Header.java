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
     * Report the source data name or specific type of assessment.
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
     * @param sourceDataType Source data information, it can be a device or assessment name.
     * @param descriptiveStatistic Statical value expressed by samples.
     * @param unit Unit used by the sourceType.
     * @param timeWindow Time interval between two consecutive samples.
     * @param timeFrame Timestamps of request.
     * @param effectiveTimeFrame Timestamps of the first and the last samples in the data-set.
     */
    @SuppressWarnings("PMD.ExcessiveParameterList")
    public Header(String projectId, String subjectId, String sourceId, String sourceType, String
            sourceDataType,
            DescriptiveStatistic descriptiveStatistic, String unit, TimeWindow timeWindow,
            TimeFrame timeFrame, TimeFrame effectiveTimeFrame) {
        this.projectId = projectId;
        this.subjectId = subjectId;
        this.sourceId = sourceId;
        this.sourceType = sourceType;
        this.sourceDataType = sourceDataType;
        this.descriptiveStatistic = descriptiveStatistic;
        this.unit = unit;
        this.timeWindow = timeWindow;
        this.timeFrame = timeFrame;
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

    public TimeFrame getEffectiveTimeFrame() {
        return effectiveTimeFrame;
    }

    public void setEffectiveTimeFrame(
            TimeFrame effectiveTimeFrame) {
        this.effectiveTimeFrame = effectiveTimeFrame;
    }

    public TimeFrame getTimeFrame() {
        return timeFrame;
    }

    public void setTimeFrame(TimeFrame timeFrame) {
        this.timeFrame = timeFrame;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Header effectiveTimeFrame = (Header) o;

        return Objects.equals(subjectId, effectiveTimeFrame.subjectId)
                && Objects.equals(projectId, effectiveTimeFrame.projectId)
                && Objects.equals(sourceId, effectiveTimeFrame.sourceId)
                && Objects.equals(sourceType, effectiveTimeFrame.sourceType)
                && Objects.equals(sourceDataType, effectiveTimeFrame.sourceDataType)
                && Objects.equals(descriptiveStatistic, effectiveTimeFrame.descriptiveStatistic)
                && Objects.equals(unit, effectiveTimeFrame.unit)
                && Objects.equals(timeWindow, effectiveTimeFrame.timeWindow)
                && Objects.equals(timeFrame, effectiveTimeFrame.timeFrame)
                && Objects.equals(effectiveTimeFrame, effectiveTimeFrame.effectiveTimeFrame);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subjectId, projectId, sourceId, sourceType, sourceDataType,
                descriptiveStatistic, unit, timeWindow, timeFrame, effectiveTimeFrame);
    }
}
