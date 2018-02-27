package org.radarcns.domain.restapi.dataset;

import static org.radarcns.domain.restapi.header.DescriptiveStatistic.DISTINCT;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.radarcns.domain.restapi.AggregateDataSource;
import org.radarcns.domain.restapi.TimeWindow;
import org.radarcns.domain.restapi.header.AggregatedDataPointsHeader;
import org.radarcns.domain.restapi.header.TimeFrame;

public class AggregatedDataPoints {

    @JsonProperty
    private AggregatedDataPointsHeader header;

    @JsonProperty
    private List<DataItem> dataset;

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
            List<DataItem> dataset) {

        this.dataset = dataset;
        this.header = new AggregatedDataPointsHeader(projectName, subjectId, maximumCount,
                timeFrame, timeWindow, DISTINCT, sources);

    }

    public List<DataItem> getDataset() {
        return dataset;
    }

    public void setDataset(List<DataItem> dataset) {
        this.dataset = dataset;
    }

    public AggregatedDataPointsHeader getHeader() {
        return header;
    }

    public void setHeader(AggregatedDataPointsHeader header) {
        this.header = header;
    }
}
