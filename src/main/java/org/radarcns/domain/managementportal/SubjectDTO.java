package org.radarcns.domain.managementportal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Map;

public class SubjectDTO extends org.radarcns.management.service.dto.SubjectDTO {

    private static final String HUMAN_READABLE_IDENTIFIER_KEY = "Human-readable-identifier";

    /**
     * Returns the Human Readable Identifier associated with this subject.
     *
     * @return {@link String} stating the Human Readable Identifier associated with this subject or
     * {@code null} if not set.
     */
    @JsonIgnore
    public String getHumanReadableIdentifier() {
        return getAttributes().entrySet().stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase(HUMAN_READABLE_IDENTIFIER_KEY))
                .findAny()
                .map(Map.Entry::getValue)
                .orElse(null);
    }

}
