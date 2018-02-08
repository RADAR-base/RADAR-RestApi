package org.radarcns.domain.restapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class Subject {

    @JsonProperty
    private String subjectId;

    @JsonProperty
    private String status;

    @JsonProperty
    private String humanReadableId;

    @JsonProperty
    private String projectName;

    @JsonProperty
    private List<Source> sources;

    @JsonProperty
    private String lastSeen;

    /**
     * Default constructor.  Note that this does not initialize fields to their default values from
     * the schema.  If that is desired then one should use <code>newBuilder()</code>.
     */
    public Subject() {
    }

//    /**
//     * All-args constructor.
//     *
//     * @param subjectId Subject identifier.
//     * @param active True if the subject is engaged, false otherwise. False means the subject is no
//     * longer monitored.
//     * @param sources List of sources used by the subject.
//     */
//    public Subject(String subjectId, Boolean active, List<Source> sources) {
//        this.subjectId = subjectId;
//        this.status = active ? "ACTIVATED" : "DISCONNECTED";
//        this.sources = sources;
//    }

    public Subject subjectId(String subjectId) {
        this.subjectId = subjectId;
        return this;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public Subject status(String status) {
        this.status = status;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Subject humanReadableId(String humanReadableId) {
        this.humanReadableId = humanReadableId;
        return this;
    }

    public String getHumanReadableId() {
        return humanReadableId;
    }

    public void setHumanReadableId(String humanReadableId) {
        this.humanReadableId = humanReadableId;
    }

    public Subject projectName(String projectName) {
        this.projectName = projectName;
        return this;
    }

    public String getProject() {
        return projectName;
    }

    public void setProject(String projectName) {
        this.projectName = projectName;
    }


    public Subject sources(List<Source> sources) {
        this.sources = sources;
        return this;
    }

    public Subject addSource(Source source) {
        this.sources.add(source);
        return this;
    }


    public List<Source> getSources() {
        return sources;
    }

    public void setSources(List<Source> sources) {
        this.sources = sources;
    }

    public Subject lastSeen(String lastSeen) {
        this.lastSeen = lastSeen;
        return this;
    }

    public String getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(String lastSeen) {
        this.lastSeen = lastSeen;
    }
}
