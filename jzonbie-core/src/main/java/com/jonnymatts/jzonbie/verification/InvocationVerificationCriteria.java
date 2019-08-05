package com.jonnymatts.jzonbie.verification;


import com.jonnymatts.jzonbie.requests.AppRequest;

import static java.lang.String.format;

/**
 * Criteria defining how many times a matching {@link AppRequest} has been called.
 */
public class InvocationVerificationCriteria {

    private final Integer expectedAtLeast;
    private final Integer expectedAtMost;

    private InvocationVerificationCriteria(Integer expectedAtLeast,
                                           Integer expectedAtMost) {
        this.expectedAtLeast = expectedAtLeast;
        this.expectedAtMost = expectedAtMost;
    }

    /**
     * Verifies that the matching {@code AppRequest} has been called at least this many times.
     *
     * @param times minimum occurrences
     * @return criteria with minimum value
     */
    public static InvocationVerificationCriteria atLeast(int times) {
        return new InvocationVerificationCriteria(times, null);
    }

    /**
     * Verifies that the matching {@code AppRequest} has been called at most this many times.
     *
     * @param times maximum occurrences
     * @return criteria with maximum value
     */
    public static InvocationVerificationCriteria atMost(int times) {
        return new InvocationVerificationCriteria(null, times);
    }

    /**
     * Verifies that the matching {@code AppRequest} has been called exactly this many times.
     *
     * @param times exact occurrences
     * @return criteria with exact value
     */
    public static InvocationVerificationCriteria equalTo(int times) {
        return new InvocationVerificationCriteria(times, times);
    }

    /**
     * Verifies that the matching {@code AppRequest} has been called between these two times.
     *
     * @param atLeast minimum occurrences
     * @param atMost maximum occurrences
     * @return criteria with minimum and maximum values
     */
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