package org.radarcns.status.hdfs;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.Map;

public class HdfsTopicStatistics {
    @JsonProperty
    private final Map<String, HdfsSourceStatus> sources = new HashMap<>();

    public Map<String, HdfsSourceStatus> getSources() {
        return sources;
    }
}
