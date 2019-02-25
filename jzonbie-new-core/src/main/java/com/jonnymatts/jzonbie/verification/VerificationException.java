package com.jonnymatts.jzonbie.verification;

import static java.lang.String.format;

public class VerificationException extends RuntimeException {

    public VerificationException(InvocationVerificationCriteria criteria, int count) {
        super(format("Expected: %s, actual: %s", criteria.getDescription(), count));
    }
}