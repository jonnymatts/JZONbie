package com.jonnymatts.jzonbie.verification;

import com.jonnymatts.jzonbie.requests.AppRequest;

import static java.lang.String.format;

/**
 * Thrown to indicate that an {@link AppRequest} has not been matched as
 * many times as required by the criteria for this Jzonbie.
 */
public class VerificationException extends RuntimeException {

    public VerificationException(InvocationVerificationCriteria criteria, int count) {
        super(format("Expected: %s, actual: %s", criteria.getDescription(), count));
    }
}