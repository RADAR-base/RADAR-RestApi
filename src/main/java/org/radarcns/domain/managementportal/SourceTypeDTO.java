package org.radarcns.domain.managementportal;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class SourceTypeDTO extends org.radarcns.management.service.dto.SourceTypeDTO {

    @JsonIgnore
    private static final String SOURCE_STATISTICS_MONITOR = "source_statistics";

    private String sourceStatisticsMonitorTopic;

    /**
     * Returns the source-monitor-statistics collection name for this source-type based on
     * convention.
     *
     * @return the source-monitor-statistics collection name
     */
    @JsonIgnore
    public String getSourceStatisticsMonitorTopic() {
        // based on the convention
        if (sourceStatisticsMonitorTopic == null || sourceStatisticsMonitorTopic
                .isEmpty()) {
            return (SOURCE_STATISTICS_MONITOR + "_" + this.getProducer() + "_" + this.getModel())
                    .toLowerCase();
        }
        return sourceStatisticsMonitorTopic;
    }

    @JsonIgnore
    public SourceTypeIdentifier getSourceTypeIdentifier() {
        return new SourceTypeIdentifier(getProducer(), getModel(), getCatalogVersion());
    }

    public SourceTypeDTO sourceStatisticsMonitorTopic(String sourceStatisticsMonitorTopic) {
        this.sourceStatisticsMonitorTopic = sourceStatisticsMonitorTopic;
        return this;
    }

}
