package org.radarcns.webapp.exception;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Status message to return on error.
 */
public class StatusMessage {

    @JsonProperty
    private String error;

    @JsonProperty
    private String message;

    /**
     * Creates an instance of StatusMessage.
     *
     * @param error error code
     * @param message error message
     */
    public StatusMessage(String error, String message) {
        this.error = error;
        this.message = message;
    }

    @Override
    public String toString() {
        return error + " - " + message;
    }
}
