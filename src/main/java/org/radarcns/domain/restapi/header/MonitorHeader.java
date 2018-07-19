package org.radarcns.domain.restapi.header;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MonitorHeader {

    public enum MONITOR_CATEGORY {
        PASSIVE, QUESTIONNAIRE
    }

    @JsonProperty
    private String projectId;

    @JsonProperty
    private String subjectId;

    @JsonProperty
    private String sourceId;

    @JsonProperty
    private MONITOR_CATEGORY monitor_category;

    public MonitorHeader() {
        // default constructor.
    }

    public MonitorHeader(String projectId, String subjectId, String sourceId,
            MONITOR_CATEGORY monitor_category) {
        this.projectId = projectId;
        this.subjectId = subjectId;
        this.sourceId = sourceId;
        this.monitor_category = monitor_category;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
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
                && monitor_category == that.monitor_category;
    }

    @Override
    public int hashCode() {

        return Objects.hash(projectId, subjectId, sourceId, monitor_category);
    }

    @Override
    public String toString() {
        return "MonitorHeader{" + "projectId='" + projectId + '\'' + ", subjectId='" + subjectId
                + '\'' + ", sourceId='" + sourceId + '\'' + ", monitor_category=" + monitor_category
                + '}';
    }
}
