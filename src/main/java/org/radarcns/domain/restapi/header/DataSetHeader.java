package org.radarcns.domain.restapi.header;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    public DataSetHeader(String projectId, String subjectId, String sourceId, String sourceType, String
            sourceDataType,
            DescriptiveStatistic descriptiveStatistic, String unit, TimeWindow timeWindow,
            TimeFrame timeFrame, TimeFrame effectiveTimeFrame) {
        this.projectId = projectId;
        this.subjectId = subjectId;
        this.sourceId = sourceId;
        this.sourceType = sourceType;
        this.sourceDataType = sourceDataType;
        this.descriptiveStatistic = descriptiveStatistic;
        this.unit = unit;
        this.timeWindow = timeWindow;
        this.timeFrame = timeFrame;
        this.effectiveTimeFrame = effectiveTimeFrame;
    }

    public String getSourceDataType() {
        return sourceDataType;
    }

    public void setSourceDataType(String sourceDataType) {
        this.sourceDataType = sourceDataType;
    }

    public DescriptiveStatistic getDescriptiveStatistic() {
        return descriptiveStatistic;
    }

    public void setDescriptiveStatistic(DescriptiveStatistic descriptiveStatistic) {
        this.descriptiveStatistic = descriptiveStatistic;
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
