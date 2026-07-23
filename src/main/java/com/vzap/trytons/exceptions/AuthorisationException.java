package com.vzap.trytons.exceptions;

public class AuthorisationException extends ApplicationException {
    public AuthorisationException(String message) {
        super(message, 403, "FORBIDDEN");
    }

    public AuthorisationException(String message, Throwable cause) {
        super(message, 403, "FORBIDDEN", cause);
    }
}
