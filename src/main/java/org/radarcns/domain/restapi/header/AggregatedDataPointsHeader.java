package org.radarcns.domain.restapi.header;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.radarcns.domain.restapi.AggregateDataSource;
import org.radarcns.domain.restapi.TimeWindow;

public class AggregatedDataPointsHeader {

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
    private DescriptiveStatistic statistic = DescriptiveStatistic.DISTINCT;

    @JsonProperty
    private List<AggregateDataSource> sources;

    public AggregatedDataPointsHeader() {
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
     */
    public AggregatedDataPointsHeader(String projectName, String subjectId,
            Integer maximumCount, TimeFrame timeFrame,
            TimeWindow timeWindow, DescriptiveStatistic statistic,
            List<AggregateDataSource> sources) {
        this.projectName = projectName;
        this.subjectId = subjectId;
        this.maximumCount = maximumCount;
        this.timeFrame = timeFrame;
        this.timeWindow = timeWindow;
        this.statistic = statistic;
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

    public TimeWindow getTimeWindow() {
        return timeWindow;
    }

    public void setTimeWindow(TimeWindow timeWindow) {
        this.timeWindow = timeWindow;
    }

    public DescriptiveStatistic getStatistic() {
        return statistic;
    }

    public void setStatistic(DescriptiveStatistic statistic) {
        this.statistic = statistic;
    }

    public List<AggregateDataSource> getSources() {
        return sources;
    }

    public void setSources(List<AggregateDataSource> sources) {
        this.sources = sources;
    }
}
