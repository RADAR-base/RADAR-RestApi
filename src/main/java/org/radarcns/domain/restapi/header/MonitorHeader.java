package org.radarcns.domain.restapi.header;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;


public class MonitorHeader {

    public enum MonitorCategory {
        PASSIVE, QUESTIONNAIRE
    }

    @JsonProperty
    private String projectId;

    @JsonProperty
    private String subjectId;

    @JsonProperty
    private String sourceId;

    @JsonProperty
    private MonitorCategory monitorCategory;

    /**
     * Default constructor.
     */
    public MonitorHeader() {
        // default constructor.
    }

    /**
     * Contains meta-data of the monitored source.
     * @param projectId project Id.
     * @param subjectId subject id.
     * @param sourceId source id.
     * @param monitorCategory category of the monitor. Type of {@link MonitorCategory}
     */
    public MonitorHeader(String projectId, String subjectId, String sourceId,
            MonitorCategory monitorCategory) {
        this.projectId = projectId;
        this.subjectId = subjectId;
        this.sourceId = sourceId;
        this.monitorCategory = monitorCategory;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
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

    public MonitorCategory getMonitorCategory() {
        return monitorCategory;
    }

    public void setMonitorCategory(MonitorCategory monitorCategory) {
        this.monitorCategory = monitorCategory;
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
