package com.jonnymatts.jzonbie.verification;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import static java.lang.String.format;

public class InvocationVerificationCriteria {

    @JsonProperty("atLeast") private final Integer expectedAtLeast;
    @JsonProperty("atMost") private final Integer expectedAtMost;

    @JsonCreator
    private InvocationVerificationCriteria(@JsonProperty("atLeast") Integer expectedAtLeast,
                                           @JsonProperty("atMost") Integer expectedAtMost) {
        this.expectedAtLeast = expectedAtLeast;
        this.expectedAtMost = expectedAtMost;
    }

    public static InvocationVerificationCriteria atLeast(int times) {
        return new InvocationVerificationCriteria(times, null);
    }

    public static InvocationVerificationCriteria atMost(int times) {
        return new InvocationVerificationCriteria(null, times);
    }

    public static InvocationVerificationCriteria equalTo(int times) {
        return new InvocationVerificationCriteria(times, times);
    }

    public static InvocationVerificationCriteria between(int atLeast, int atMost) {
        return new InvocationVerificationCriteria(atLeast, atMost);
    }

    public void verify(int times) throws VerificationException {
        if(!accept(times)) throw new VerificationException(this, times);
    }

    private boolean accept(int times) {
        if (expectedAtLeast == null) return expectedAtMost >= times;
        if (expectedAtMost == null) return expectedAtLeast <= times;
        return expectedAtMost >= times && expectedAtLeast <= times;
    }

    public String getDescription() {
        if (expectedAtLeast == null) return "at most " + expectedAtMost;
        if (expectedAtMost == null) return "at least " + expectedAtLeast;
        if (expectedAtLeast == expectedAtMost) return "equal to " + expectedAtLeast;
        return format("between %s and %s", expectedAtLeast, expectedAtMost);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InvocationVerificationCriteria that = (InvocationVerificationCriteria) o;

        if (expectedAtLeast != null ? !expectedAtLeast.equals(that.expectedAtLeast) : that.expectedAtLeast != null)
            return false;
        return expectedAtMost != null ? expectedAtMost.equals(that.expectedAtMost) : that.expectedAtMost == null;
    }

    @Override
    public int hashCode() {
        int result = expectedAtLeast != null ? expectedAtLeast.hashCode() : 0;
        result = 31 * result + (expectedAtMost != null ? expectedAtMost.hashCode() : 0);
        return result;
    }
}