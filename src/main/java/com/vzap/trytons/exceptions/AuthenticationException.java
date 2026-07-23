package com.vzap.trytons.exceptions;

public class AuthenticationException extends ApplicationException {
    public AuthenticationException(String message) {
        super(message, 401, "AUTHENTICATION_ERROR");
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, 401, "AUTHENTICATION_ERROR", cause);
    }
}
