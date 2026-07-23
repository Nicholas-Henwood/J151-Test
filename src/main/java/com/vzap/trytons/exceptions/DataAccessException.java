package com.vzap.trytons.exceptions;

public class DataAccessException extends ApplicationException {
    public DataAccessException(String Message, Throwable cause) {
        super(Message, 500, "DATA_ACCESS_ERROR", cause);
    }
}
