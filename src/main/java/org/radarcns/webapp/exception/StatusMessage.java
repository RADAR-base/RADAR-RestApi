package org.radarcns.webapp.exception;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StatusMessage {
    @JsonProperty
    private String error;

    @JsonProperty
    private String message;

    @JsonProperty
    private Object data;

    public StatusMessage(String error, String message) {
        this(error, message, null);
    }

    public StatusMessage(String error, String message, Object data) {
        this.error = error;
        this.message = message;
        this.data = data;
    }

    @Override
    public String toString() {
        return error + " - " + message;
    }

    public Object getData() {
        return data;
    }
}
