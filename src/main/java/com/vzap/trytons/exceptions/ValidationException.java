package com.vzap.trytons.exceptions;

public class ValidationException extends ApplicationException {
    public ValidationException(String message) {
        super(message, 400, "VALIDATION_ERROR");
    }
}
