package com.vzap.trytons.exceptions;

public class ConflictException extends ApplicationException {
    public ConflictException(String Message) {
        super(Message, 409, "CONFLICT");
    }
}