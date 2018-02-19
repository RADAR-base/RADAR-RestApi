package org.radarcns.domain.managementportal;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class SourceDTO extends org.radarcns.management.service.dto.SourceDTO {

    @JsonIgnore
    public SourceTypeIdentifier getSourceTypeIdentifier() {
        return new SourceTypeIdentifier(getSourceType().getProducer(), getSourceType().getModel(),
                getSourceType().getCatalogVersion());
    }

    public String getSourceIdentifier() {
        return super.getSourceId().toString();
    }
}
