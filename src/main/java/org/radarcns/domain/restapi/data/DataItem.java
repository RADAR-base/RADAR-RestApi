package org.radarcns.domain.restapi.data;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DataItem {


    @JsonProperty
    private Object sample;

    @JsonProperty
    private String startDateTime;


    /**
     * Default constructor.  Note that this does not initialize fields
     * to their default values from the schema.  If that is desired then
     * one should use <code>newBuilder()</code>.
     */
    public DataItem() {}

    /**
     * All-args constructor.
     * @param sample Sample value. For more details, check org.radarcns.restapi.data.Acceleration, org.radarcns.restapi.data.DoubleSample and org.radarcns.questionnaire.Questionnaire.
     * @param startDateTime Point in time (ISO8601) with UTC timezone. It represents the timestamp of the first sample contained inside the aggregated data.
     */
    public DataItem(java.lang.Object sample, java.lang.String startDateTime) {
        this.sample = sample;
        this.startDateTime = startDateTime;
    }

    public Object getSample() {
        return sample;
    }

    public void setSample(Object sample) {
        this.sample = sample;
    }

    public String getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(String startDateTime) {
        this.startDateTime = startDateTime;
    }
}
