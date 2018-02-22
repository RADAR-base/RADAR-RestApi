package org.radarcns.domain.restapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.radarcns.domain.restapi.format.AggregatedDataSet;
import org.radarcns.domain.restapi.header.DescriptiveStatistic;
import org.radarcns.domain.restapi.header.TimeFrame;

public class AggregatedData {

    @JsonProperty
    private String projectName;

    @JsonProperty
    private String subjectId;

    @JsonProperty
    private Integer maximumCount;

    @JsonProperty
    private TimeFrame timeFrame;

    @JsonProperty
    private TimeWindow timeWindow;

    @JsonProperty
    private DescriptiveStatistic statistic = DescriptiveStatistic.AGGREGATED_DATA_POINTS;

    @JsonProperty
    private List<AggregateDataSource> sources;

    @JsonProperty
    private List<AggregatedDataSet> aggregatedDataSetList;

    public AggregatedData() {

    }

    public AggregatedData(String projectName, String subjectId, Integer maximumCount, TimeFrame
            timeFrame, TimeWindow timeWindow, List<AggregateDataSource> sources,
            List<AggregatedDataSet>
                    aggregatedDataPoints) {
        this.projectName = projectName;
        this.subjectId = subjectId;
        this.maximumCount = maximumCount;
        this.timeFrame = timeFrame;
        this.aggregatedDataSetList = aggregatedDataPoints;
        this.timeWindow = timeWindow;
        this.sources = sources;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public Integer getMaximumCount() {
        return maximumCount;
    }

    public void setMaximumCount(Integer maximumCount) {
        this.maximumCount = maximumCount;
    }

    public TimeFrame getTimeFrame() {
        return timeFrame;
    }

    public void setTimeFrame(TimeFrame timeFrame) {
        this.timeFrame = timeFrame;
    }

    public DescriptiveStatistic getStatistic() {
        return statistic;
    }

    public void setStatistic(DescriptiveStatistic statistic) {
        this.statistic = statistic;
    }

    public List<AggregatedDataSet> getAggregatedDataSetList() {
        return aggregatedDataSetList;
    }

    public void setAggregatedDataSetList(List<AggregatedDataSet> aggregatedDataSetList) {
        this.aggregatedDataSetList = aggregatedDataSetList;
    }

    public TimeWindow getTimeWindow() {
        return timeWindow;
    }

    public void setTimeWindow(TimeWindow timeWindow) {
        this.timeWindow = timeWindow;
    }

    public List<AggregateDataSource> getSources() {
        return sources;
    }

    public void setSources(List<AggregateDataSource> sources) {
        this.sources = sources;
    }
}
