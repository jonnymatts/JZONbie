package com.jonnymatts.jzonbie.jackson.verification;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class InvocationVerificationCriteriaMixIn {

    @JsonProperty("atLeast") private final Integer expectedAtLeast;
    @JsonProperty("atMost") private final Integer expectedAtMost;

    @JsonCreator
    private InvocationVerificationCriteriaMixIn(@JsonProperty("atLeast") Integer expectedAtLeast,
                                           @JsonProperty("atMost") Integer expectedAtMost) {
        this.expectedAtLeast = null;
        this.expectedAtMost = null;
    }
}