package org.radarcns.domain.restapi.header;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

import org.radarcns.domain.restapi.TimeWindow;

public class MonitorHeader extends Header {

    public enum MonitorCategory {
        PASSIVE, QUESTIONNAIRE
    }

    @JsonProperty
    private MonitorCategory monitorCategory;

    /**
     * Default constructor.
     */
    public MonitorHeader() {

    }

    /**
     * Contains meta-data of the monitored source.
     * @param projectId project name
     * @param subjectId subject identifier.
     * @param sourceId source identifier.
     * @param monitorCategory monitor category.
     */
    public MonitorHeader(String projectId, String subjectId, String sourceId,
            MonitorCategory monitorCategory) {
        super(projectId, subjectId, sourceId, null, null, null, null, null);
        this.monitorCategory = monitorCategory;
    }


    /**
     * Contains meta-data of the monitored source.
     * @param projectId project name
     * @param subjectId subject identifier.
     * @param sourceId source identifier.
     * @param sourceType source-type of source.
     * @param unit unit of measurement
     * @param timeWindow timewindow between records.
     * @param timeFrame requested time frame.
     * @param effectiveTimeFrame effective time frame.
     * @param monitorCategory monitor category.
     */
    public MonitorHeader(String projectId, String subjectId, String sourceId, String sourceType,
            String unit, TimeWindow timeWindow, TimeFrame timeFrame, TimeFrame effectiveTimeFrame,
            MonitorCategory monitorCategory) {
        super(projectId, subjectId, sourceId, sourceType, unit, timeWindow, timeFrame,
                effectiveTimeFrame);
        this.monitorCategory = monitorCategory;
    }



    public MonitorCategory getMonitorCategory() {
        return monitorCategory;
    }

    public MonitorHeader monitorCategory(MonitorCategory monitorCategory) {
        this.monitorCategory = monitorCategory;
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
        MonitorHeader that = (MonitorHeader) o;
        return Objects.equals(projectId, that.projectId) && Objects
                .equals(subjectId, that.subjectId) && Objects.equals(sourceId, that.sourceId)
                && monitorCategory == that.monitorCategory;
    }

    @Override
    public int hashCode() {

        return Objects.hash(projectId, subjectId, sourceId, monitorCategory);
    }

    @Override
    public String toString() {
        return "MonitorHeader{" + "projectId='" + projectId + '\'' + ", subjectId='" + subjectId
                + '\'' + ", sourceId='" + sourceId + '\'' + ", monitorCategory=" + monitorCategory
                + '}';
    }
}
