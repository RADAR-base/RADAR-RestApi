package org.radarcns.webapp.param;

import java.util.List;
import org.radarcns.domain.restapi.AggregateDataSource;


public class DataAggregateParam {

    private List<AggregateDataSource> sources;

    public DataAggregateParam() {}

    public DataAggregateParam(List<AggregateDataSource> sources) {
        this.sources = sources;
    }

    public List<AggregateDataSource> getSources() {
        return sources;
    }

    public void setSources(
            List<AggregateDataSource> sources) {
        this.sources = sources;
    }
}


