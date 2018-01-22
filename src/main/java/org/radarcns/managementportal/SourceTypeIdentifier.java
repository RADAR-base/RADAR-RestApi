package org.radarcns.managementportal;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

public class SourceTypeIdentifier {

    @JsonProperty
    private String producer;

    @JsonProperty
    private String model;

    @JsonProperty
    private String catalogVersion;

    /**
     * Creates a source-type identifier from parameters.
     * @param producer of source-type
     * @param model of source-type
     * @param catalogVersion of source-type
     */
    public SourceTypeIdentifier(String producer, String model, String catalogVersion) {
        this.producer = producer;
        this.model = model;
        this.catalogVersion = catalogVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SourceTypeIdentifier sourceType = (SourceTypeIdentifier) o;

        return Objects.equals(producer, sourceType.producer)
                && Objects.equals(model, sourceType.model)
                && Objects.equals(catalogVersion, sourceType.catalogVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(model, producer, catalogVersion);
    }

    @Override
    public String toString() {
        return this.producer + "_" + this.model + "_" + this.catalogVersion;
    }

}