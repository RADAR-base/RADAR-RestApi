package org.radarcns.domain.restapi.dataset;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.radarcns.domain.restapi.header.TimeFrame;

public class AggregatedDataItem {

    @JsonProperty
    private Integer count;

    @JsonProperty
    private TimeFrame timeFrame;

    public AggregatedDataItem() {
    }

    public AggregatedDataItem(Integer count, TimeFrame timeFrame) {
        this.count = count;
        this.timeFrame = timeFrame;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public TimeFrame getTimeFrame() {
        return timeFrame;
    }

    public void setTimeFrame(TimeFrame timeFrame) {
        this.timeFrame = timeFrame;
    }
}
