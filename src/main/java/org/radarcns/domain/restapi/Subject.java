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

    public Subject subjectId(String subjectId) {
        this.subjectId = subjectId;
        return this;
    }

    public String getSubjectId() {
        return subjectId;
    }


    public Subject status(String status) {
        this.status = status;
        return this;
    }

    public String getStatus() {
        return status;
    }


    public Subject humanReadableId(String humanReadableId) {
        this.humanReadableId = humanReadableId;
        return this;
    }

    public String getHumanReadableId() {
        return humanReadableId;
    }


    public Subject projectName(String projectName) {
        this.projectName = projectName;
        return this;
    }

    public String getProject() {
        return projectName;
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


    public Subject lastSeen(String lastSeen) {
        this.lastSeen = lastSeen;
        return this;
    }

    public String getLastSeen() {
        return lastSeen;
    }

}
