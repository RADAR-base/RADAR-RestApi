package org.radarcns.webapp.param;

import java.time.Instant;

/**
 * Param object that can take {@link String} and convert to {@link Instant}.
 */
public class InstantParam {

    private Instant value;

    public InstantParam(String value) {
        this.value = Instant.parse(value);
    }

    public Instant getValue() {
        return value;
    }

    public void setValue(Instant value) {
        this.value = value;
    }
}
