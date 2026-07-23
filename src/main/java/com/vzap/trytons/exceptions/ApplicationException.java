package com.vzap.trytons.exceptions;

import lombok.Getter;

@Getter
public abstract class ApplicationException extends RuntimeException {
    private final String errorCode;
    private final int statusCode;

    protected ApplicationException(String message, int statusCode, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.statusCode = statusCode;
    }

    protected ApplicationException(String message, int statusCode, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.statusCode = statusCode;
    }

}
