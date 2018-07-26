package org.radarcns.domain.restapi.header;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

import org.radarcns.domain.restapi.TimeWindow;

public class DataSetHeader extends Header {

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
     * Default constructor.
     */
    public DataSetHeader() {
        // default constructor
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
    public DataSetHeader(String projectId, String subjectId, String sourceId, String sourceType,
            String sourceDataType, DescriptiveStatistic descriptiveStatistic, String unit,
            TimeWindow timeWindow, TimeFrame timeFrame, TimeFrame effectiveTimeFrame) {
        super(projectId, subjectId, sourceId, sourceType, unit, timeWindow, timeFrame,
                effectiveTimeFrame);
        this.sourceDataType = sourceDataType;
        this.descriptiveStatistic = descriptiveStatistic;
    }

    public String getSourceDataType() {
        return sourceDataType;
    }

    public DataSetHeader sourceDataType(String sourceDataType) {
        this.sourceDataType = sourceDataType;
        return this;
    }

    public DescriptiveStatistic getDescriptiveStatistic() {
        return descriptiveStatistic;
    }

    public DataSetHeader descriptiveStatistic(DescriptiveStatistic descriptiveStatistic) {
        this.descriptiveStatistic = descriptiveStatistic;
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
        if (!super.equals(o)) {
            return false;
        }
        DataSetHeader that = (DataSetHeader) o;
        return Objects.equals(sourceDataType, that.sourceDataType)
                && descriptiveStatistic == that.descriptiveStatistic;
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), sourceDataType, descriptiveStatistic);
    }
}
