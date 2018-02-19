package org.radarcns.webapp.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

public class BadGatewayException extends WebApplicationException {

    public BadGatewayException() {
        super(Status.BAD_GATEWAY);
    }

    public BadGatewayException(Throwable cause) {
        super(cause, Status.BAD_GATEWAY);
    }

    public BadGatewayException(String message) {
        super(message, Status.BAD_GATEWAY);
    }

}
