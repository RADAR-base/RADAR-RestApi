package org.radarcns.domain.restapi.dataset;

import static org.radarcns.domain.restapi.header.DescriptiveStatistic.AGGREGATED_DATA_POINTS;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.radarcns.domain.restapi.AggregateDataSource;
import org.radarcns.domain.restapi.TimeWindow;
import org.radarcns.domain.restapi.header.AggregatedDataPointsHeader;
import org.radarcns.domain.restapi.header.TimeFrame;

public class AggregatedDataPoints {


    @JsonProperty
    private List<AggregatedDataItem> dataset;

    @JsonProperty
    private AggregatedDataPointsHeader header;

    public AggregatedDataPoints() {

    }

    /**
     * Constructor.
     *
     * @param projectName of project
     * @param subjectId of subject
     * @param maximumCount of records
     * @param timeFrame start to end
     * @param timeWindow interval window
     * @param sources to request availability
     * @param dataset computed data.
     */
    public AggregatedDataPoints(String projectName, String subjectId, Integer maximumCount,
            TimeFrame timeFrame, TimeWindow timeWindow, List<AggregateDataSource> sources,
            List<AggregatedDataItem>
                    dataset) {

        this.dataset = dataset;
        this.header = new AggregatedDataPointsHeader(projectName, subjectId, maximumCount,
                timeFrame, timeWindow, AGGREGATED_DATA_POINTS, sources);

    }

    public List<AggregatedDataItem> getDataset() {
        return dataset;
    }

    public void setDataset(List<AggregatedDataItem> dataset) {
        this.dataset = dataset;
    }

    public AggregatedDataPointsHeader getHeader() {
        return header;
    }

    public void setHeader(AggregatedDataPointsHeader header) {
        this.header = header;
    }
}
