package org.radarcns.security.exception;

public class AccessDeniedException extends Exception{
    public AccessDeniedException() {

    }

    public AccessDeniedException(String message) {
        super(message);
    }

    public AccessDeniedException(Throwable cause) {
        super(cause);
    }

    public AccessDeniedException(String message, Throwable cause) {
        super(message,cause);
    }
}
