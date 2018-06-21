package org.radarcns.domain.managementportal;


import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import javax.validation.constraints.NotNull;

/**
 * A DTO for the Source entity.
 */
public class SourceDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull
    private Long id;

    private String sourceId;

    @NotNull
    private String sourceName;

    private String expectedSourceName;

    @NotNull
    private Boolean assigned;

    @NotNull
    private SourceTypeDTO sourceType;

    private Map<String, String> attributes;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public Boolean getAssigned() {
        return assigned;
    }

    public void setAssigned(Boolean assigned) {
        this.assigned = assigned;
    }

    public SourceTypeDTO getSourceType() {
        return sourceType;
    }

    public void setSourceType(SourceTypeDTO sourceType) {
        this.sourceType = sourceType;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getExpectedSourceName() {
        return expectedSourceName;
    }

    public void setExpectedSourceName(String expectedSourceName) {
        this.expectedSourceName = expectedSourceName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SourceDTO sourceDto = (SourceDTO) o;

        return Objects.equals(sourceId, sourceDto.sourceId)
                && Objects.equals(sourceName, sourceDto.sourceName)
                && Objects.equals(id, sourceDto.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceId, sourceName);
    }

    @Override
    public String toString() {
        return "SourceDTO{"
                + ", id='" + id + '\''
                + ", sourceId='" + sourceId + '\''
                + ", sourceName='" + sourceName + '\''
                + ", assigned=" + assigned
                + ", sourceType=" + sourceType.getSourceTypeIdentifier().toString()
                + '}';
    }

    @JsonIgnore
    public SourceTypeIdentifier getSourceTypeIdentifier() {
        return sourceType.getSourceTypeIdentifier();
    }
}
