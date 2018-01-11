package org.radarcns.security.exception;

import java.security.GeneralSecurityException;

public class AccessDeniedException extends GeneralSecurityException {
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
