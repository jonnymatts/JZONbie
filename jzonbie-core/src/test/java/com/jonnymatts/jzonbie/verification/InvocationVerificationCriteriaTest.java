package com.jonnymatts.jzonbie.verification;

import org.junit.Test;

import static com.jonnymatts.jzonbie.verification.InvocationVerificationCriteria.*;
import static org.assertj.core.api.Assertions.assertThat;

public class InvocationVerificationCriteriaTest {

    private final InvocationVerificationCriteria atLeast = atLeast(1);
    private final InvocationVerificationCriteria atMost = atMost(1);
    private final InvocationVerificationCriteria equalTo = equalTo(1);
    private final InvocationVerificationCriteria between = between(2, 4);

    @Test
    public void atLeastReturnsTrueIfTimesIsGreaterThanExpectedTimes() throws Exception {
        final boolean got = atLeast.accept(2);

        assertThat(got).isTrue();
    }

    @Test
    public void atLeastReturnsTrueIfTimesIsEqualToExpectedTimes() throws Exception {
        final boolean got = atLeast.accept(1);

        assertThat(got).isTrue();
    }

    @Test
    public void atLeastReturnsFalseIfTimesIsLessThanExpectedTimes() throws Exception {
        final boolean got = atLeast.accept(0);

        assertThat(got).isFalse();
    }

    @Test
    public void atMostReturnsFalseIfTimesIsGreaterThanExpectedTimes() throws Exception {
        final boolean got = atMost.accept(2);

        assertThat(got).isFalse();
    }

    @Test
    public void atMostReturnsTrueIfTimesIsEqualToExpectedTimes() throws Exception {
        final boolean got = atMost.accept(1);

        assertThat(got).isTrue();
    }

    @Test
    public void atMostReturnsTrueIfTimesIsLessThanExpectedTimes() throws Exception {
        final boolean got = atMost.accept(0);

        assertThat(got).isTrue();
    }

    @Test
    public void equalToReturnsFalseIfTimesIsGreaterThanExpectedTimes() throws Exception {
        final boolean got = equalTo.accept(2);

        assertThat(got).isFalse();
    }

    @Test
    public void equalToReturnsTrueIfTimesIsEqualToExpectedTimes() throws Exception {
        final boolean got = equalTo.accept(1);

        assertThat(got).isTrue();
    }

    @Test
    public void equalToReturnsFalseIfTimesIsLessThanExpectedTimes() throws Exception {
        final boolean got = equalTo.accept(0);

        assertThat(got).isFalse();
    }

    @Test
    public void betweenReturnsTrueIfTimesIsBetweenAtLeastAndAtMostExpectedTimes() throws Exception {
        final boolean got = between.accept(3);

        assertThat(got).isTrue();
    }

    @Test
    public void betweenReturnsTrueIfTimesIsEqualToAtLeastExpectedTime() throws Exception {
        final boolean got = between.accept(2);

        assertThat(got).isTrue();
    }

    @Test
    public void betweenReturnsTrueIfTimesIsEqualToAtMostExpectedTime() throws Exception {
        final boolean got = between.accept(4);

        assertThat(got).isTrue();
    }

    @Test
    public void betweenReturnsFalseIfTimesIsLessThanAtLeastExpectedTime() throws Exception {
        final boolean got = between.accept(1);

        assertThat(got).isFalse();
    }

    @Test
    public void betweenReturnsFalseIfTimesIsGreaterThanAtMostExpectedTime() throws Exception {
        final boolean got = between.accept(5);

        assertThat(got).isFalse();
    }
}