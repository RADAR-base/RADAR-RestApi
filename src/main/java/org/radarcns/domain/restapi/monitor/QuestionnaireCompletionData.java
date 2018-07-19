package org.radarcns.domain.restapi.monitor;

import java.time.Instant;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.radarcns.domain.restapi.header.MonitorHeader;

public class QuestionnaireCompletionData {

    @JsonProperty
    private MonitorHeader header;

    @JsonProperty
    private Instant timeRecorded;

    @JsonProperty
    private String questionnaireName;

    @JsonProperty
    private Double completionPercentage;

    /**
     * Default constructor.
     */
    public QuestionnaireCompletionData() {
        // default constructor
    }

    /**
     * Constructor.
     * @param header contains the monitor meta-data.
     * @param timeRecorded recorded time of the status.
     * @param questionnaireName name of the questionnaire.
     * @param completionPercentage percentage of completion.
     */
    public QuestionnaireCompletionData(MonitorHeader header, Instant timeRecorded,
            String questionnaireName, Double completionPercentage) {
        this.header = header;
        this.timeRecorded = timeRecorded;
        this.questionnaireName = questionnaireName;
        this.completionPercentage = completionPercentage;
    }

    public MonitorHeader getHeader() {
        return header;
    }

    public void setHeader(MonitorHeader header) {
        this.header = header;
    }

    public Instant getTimeRecorded() {
        return timeRecorded;
    }

    public void setTimeRecorded(Instant timeRecorded) {
        this.timeRecorded = timeRecorded;
    }

    public String getQuestionnaireName() {
        return questionnaireName;
    }

    public void setQuestionnaireName(String questionnaireName) {
        this.questionnaireName = questionnaireName;
    }

    public Double getCompletionPercentage() {
        return completionPercentage;
    }

    public void setCompletionPercentage(Double completionPercentage) {
        this.completionPercentage = completionPercentage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        QuestionnaireCompletionData that = (QuestionnaireCompletionData) o;
        return Objects.equals(header, that.header) && Objects
                .equals(timeRecorded, that.timeRecorded) && Objects
                .equals(questionnaireName, that.questionnaireName) && Objects
                .equals(completionPercentage, that.completionPercentage);
    }

    @Override
    public int hashCode() {

        return Objects.hash(header, timeRecorded, questionnaireName, completionPercentage);
    }

    @Override
    public String toString() {
        return "QuestionnaireCompletionData{" + "header=" + header + ", timeRecorded="
                + timeRecorded + ", questionnaireName='" + questionnaireName + '\''
                + ", completionPercentage=" + completionPercentage + '}';
    }
}
