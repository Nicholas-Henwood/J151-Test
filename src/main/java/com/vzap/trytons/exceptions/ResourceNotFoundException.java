package com.vzap.trytons.exceptions;

public class ResourceNotFoundException extends ApplicationException {
    public ResourceNotFoundException(String Message) {
        super(Message, 404, "RESOURCE_NOT_FOUND");
    }
}