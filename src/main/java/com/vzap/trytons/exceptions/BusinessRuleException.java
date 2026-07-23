package com.vzap.trytons.exceptions;

public class BusinessRuleException extends ApplicationException {
    public BusinessRuleException(String message) {
        super(message, 422, "BUSINESS_RULE_ERROR");
    }

    public BusinessRuleException(String message, Throwable cause) {
        super(message, 422, "BUSINESS_RULE_ERROR", cause);
    }
}
