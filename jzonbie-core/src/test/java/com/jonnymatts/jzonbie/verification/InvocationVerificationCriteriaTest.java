package com.jonnymatts.jzonbie.verification;

import org.junit.jupiter.api.Test;

import static com.jonnymatts.jzonbie.verification.InvocationVerificationCriteria.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InvocationVerificationCriteriaTest {
    private final InvocationVerificationCriteria atLeast = atLeast(1);
    private final InvocationVerificationCriteria atMost = atMost(1);
    private final InvocationVerificationCriteria equalTo = equalTo(1);
    private final InvocationVerificationCriteria between = between(2, 4);

    @Test
    void atLeastDoesNotThrowExceptionIfTimesIsGreaterThanExpectedTimes() throws Exception {
        atLeast.verify(2);
    }

    @Test
    void atLeastDoesNotThrowExceptionIfTimesIsEqualToExpectedTimes() throws Exception {
        atLeast.verify(1);
    }

    @Test
    void atLeastThrowsVerificationExceptionIfTimesIsLessThanExpectedTimes() throws Exception {
        assertThatThrownBy(() -> atLeast.verify(0))
                .isExactlyInstanceOf(VerificationException.class)
                .hasMessageContaining("0")
                .hasMessageContaining(atLeast.getDescription());
    }

    @Test
    void atMostThrowsVerificationExceptionIfTimesIsGreaterThanExpectedTimes() throws Exception {
        assertThatThrownBy(() -> atMost.verify(2))
                .isExactlyInstanceOf(VerificationException.class)
                .hasMessageContaining("2")
                .hasMessageContaining(atMost.getDescription());
    }

    @Test
    void atMostDoesNotThrowExceptionIfTimesIsEqualToExpectedTimes() throws Exception {
        atMost.verify(1);
    }

    @Test
    void atMostDoesNotThrowExceptionIfTimesIsLessThanExpectedTimes() throws Exception {
        atMost.verify(0);
    }

    @Test
    void equalToThrowsVerificationExceptionIfTimesIsGreaterThanExpectedTimes() throws Exception {
        assertThatThrownBy(() -> equalTo.verify(2))
                .isExactlyInstanceOf(VerificationException.class)
                .hasMessageContaining("2")
                .hasMessageContaining(equalTo.getDescription());
    }

    @Test
    void equalToDoesNotThrowExceptionIfTimesIsEqualToExpectedTimes() throws Exception {
        equalTo.verify(1);
    }

    @Test
    void equalToThrowsVerificationExceptionIfTimesIsLessThanExpectedTimes() throws Exception {
        assertThatThrownBy(() -> equalTo.verify(0))
                .isExactlyInstanceOf(VerificationException.class)
                .hasMessageContaining("0")
                .hasMessageContaining(equalTo.getDescription());
    }

    @Test
    void betweenDoesNotThrowExceptionIfTimesIsBetweenAtLeastAndAtMostExpectedTimes() throws Exception {
        between.verify(3);
    }

    @Test
    void betweenDoesNotThrowExceptionIfTimesIsEqualToAtLeastExpectedTime() throws Exception {
        between.verify(2);
    }

    @Test
    void betweenDoesNotThrowExceptionIfTimesIsEqualToAtMostExpectedTime() throws Exception {
        between.verify(4);
    }

    @Test
    void betweenThrowsVerificationExceptionIfTimesIsLessThanAtLeastExpectedTime() throws Exception {
        assertThatThrownBy(() -> between.verify(1))
                .isExactlyInstanceOf(VerificationException.class)
                .hasMessageContaining("1")
                .hasMessageContaining(between.getDescription());
    }

    @Test
    void betweenThrowsVerificationExceptionIfTimesIsGreaterThanAtMostExpectedTime() throws Exception {
        assertThatThrownBy(() -> between.verify(5))
                .isExactlyInstanceOf(VerificationException.class)
                .hasMessageContaining("5")
                .hasMessageContaining(between.getDescription());
    }

    @Test
    void getDescriptionForEqualTo() throws Exception {
        final String got = equalTo.getDescription();

        assertThat(got).isEqualTo("equal to 1");
    }

    @Test
    void getDescriptionForAtLeast() throws Exception {
        final String got = atLeast.getDescription();

        assertThat(got).isEqualTo("at least 1");
    }

    @Test
    void getDescriptionForAtMost() throws Exception {
        final String got = atMost.getDescription();

        assertThat(got).isEqualTo("at most 1");
    }

    @Test
    void getDescriptionForBetween() throws Exception {
        final String got = between.getDescription();

        assertThat(got).isEqualTo("between 2 and 4");
    }
}