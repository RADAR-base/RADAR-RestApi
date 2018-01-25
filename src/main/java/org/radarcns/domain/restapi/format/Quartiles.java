package org.radarcns.domain.restapi.format;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Quartiles {

    /**
     * The middle number between the smallest number and the median of the data set.
     */
    @JsonProperty
    private double first;
    /**
     * The median of the data.
     */
    @JsonProperty
    private double second;
    /**
     * The middle value between the median and the highest value of the data set.
     */
    @JsonProperty
    private double third;

    /**
     * Default constructor.  Note that this does not initialize fields to their default values from
     * the schema.  If that is desired then one should use <code>newBuilder()</code>.
     */
    public Quartiles() {
    }

    /**
     * All-args constructor.
     *
     * @param first The middle number between the smallest number and the median of the data set.
     * @param second The median of the data.
     * @param third The middle value between the median and the highest value of the data set.
     */
    public Quartiles(java.lang.Double first, java.lang.Double second, java.lang.Double third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public double getFirst() {
        return first;
    }

    public void setFirst(double first) {
        this.first = first;
    }

    public double getSecond() {
        return second;
    }

    public void setSecond(double second) {
        this.second = second;
    }

    public double getThird() {
        return third;
    }

    public void setThird(double third) {
        this.third = third;
    }
}