package org.radarcns.domain.restapi.monitor;

import java.time.Instant;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

public class QuestionnaireCompletionStatus {

    @JsonProperty
    private Instant timeRecorded;

    @JsonProperty
    private String questionnaireName;

    @JsonProperty
    private Double completionPercentage = 0d;

    /**
     * Default constructor.
     */
    public QuestionnaireCompletionStatus() {
        // default constructor
    }

    /**
     * Constructor.
     * @param timeRecorded recorded time of the status.
     * @param questionnaireName name of the questionnaire.
     * @param completionPercentage percentage of completion.
     */
    public QuestionnaireCompletionStatus(Instant timeRecorded,
            String questionnaireName, Double completionPercentage) {
        this.timeRecorded = timeRecorded;
        this.questionnaireName = questionnaireName;
        this.completionPercentage = completionPercentage;
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
        QuestionnaireCompletionStatus that = (QuestionnaireCompletionStatus) o;
        return  Objects.equals(timeRecorded, that.timeRecorded) && Objects
                .equals(questionnaireName, that.questionnaireName) && Objects
                .equals(completionPercentage, that.completionPercentage);
    }

    @Override
    public int hashCode() {

        return Objects.hash(timeRecorded, questionnaireName, completionPercentage);
    }

    @Override
    public String toString() {
        return "QuestionnaireCompletionStatus{" + ", timeRecorded="
                + timeRecorded + ", questionnaireName='" + questionnaireName + '\''
                + ", completionPercentage=" + completionPercentage + '}';
    }
}
