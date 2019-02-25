package com.jonnymatts.jzonbie.verification;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static com.jonnymatts.jzonbie.verification.InvocationVerificationCriteria.*;
import static org.assertj.core.api.Assertions.assertThat;

public class InvocationVerificationCriteriaTest {

    @Rule public ExpectedException expectedException = ExpectedException.none();
    
    private final InvocationVerificationCriteria atLeast = atLeast(1);
    private final InvocationVerificationCriteria atMost = atMost(1);
    private final InvocationVerificationCriteria equalTo = equalTo(1);
    private final InvocationVerificationCriteria between = between(2, 4);

    @Test
    public void atLeastDoesNotThrowExceptionIfTimesIsGreaterThanExpectedTimes() throws Exception {
        atLeast.verify(2);
    }

    @Test
    public void atLeastDoesNotThrowExceptionIfTimesIsEqualToExpectedTimes() throws Exception {
        atLeast.verify(1);
    }

    @Test
    public void atLeastThrowsVerificationExceptionIfTimesIsLessThanExpectedTimes() throws Exception {
        expectedException.expect(VerificationException.class);
        expectedException.expectMessage("0");
        expectedException.expectMessage(atLeast.getDescription());

        atLeast.verify(0);
    }

    @Test
    public void atMostThrowsVerificationExceptionIfTimesIsGreaterThanExpectedTimes() throws Exception {
        expectedException.expect(VerificationException.class);
        expectedException.expectMessage("2");
        expectedException.expectMessage(atMost.getDescription());

        atMost.verify(2);
    }

    @Test
    public void atMostDoesNotThrowExceptionIfTimesIsEqualToExpectedTimes() throws Exception {
        atMost.verify(1);
    }

    @Test
    public void atMostDoesNotThrowExceptionIfTimesIsLessThanExpectedTimes() throws Exception {
        atMost.verify(0);
    }

    @Test
    public void equalToThrowsVerificationExceptionIfTimesIsGreaterThanExpectedTimes() throws Exception {
        expectedException.expect(VerificationException.class);
        expectedException.expectMessage("2");
        expectedException.expectMessage(equalTo.getDescription());

        equalTo.verify(2);
    }

    @Test
    public void equalToDoesNotThrowExceptionIfTimesIsEqualToExpectedTimes() throws Exception {
        equalTo.verify(1);
    }

    @Test
    public void equalToThrowsVerificationExceptionIfTimesIsLessThanExpectedTimes() throws Exception {
        expectedException.expect(VerificationException.class);
        expectedException.expectMessage("0");
        expectedException.expectMessage(equalTo.getDescription());

        equalTo.verify(0);
    }

    @Test
    public void betweenDoesNotThrowExceptionIfTimesIsBetweenAtLeastAndAtMostExpectedTimes() throws Exception {
        between.verify(3);
    }

    @Test
    public void betweenDoesNotThrowExceptionIfTimesIsEqualToAtLeastExpectedTime() throws Exception {
        between.verify(2);
    }

    @Test
    public void betweenDoesNotThrowExceptionIfTimesIsEqualToAtMostExpectedTime() throws Exception {
        between.verify(4);
    }

    @Test
    public void betweenThrowsVerificationExceptionIfTimesIsLessThanAtLeastExpectedTime() throws Exception {
        expectedException.expect(VerificationException.class);
        expectedException.expectMessage("1");
        expectedException.expectMessage(between.getDescription());

        between.verify(1);
    }

    @Test
    public void betweenThrowsVerificationExceptionIfTimesIsGreaterThanAtMostExpectedTime() throws Exception {
        expectedException.expect(VerificationException.class);
        expectedException.expectMessage("5");
        expectedException.expectMessage(between.getDescription());

        between.verify(5);
    }

    @Test
    public void getDescriptionForEqualTo() throws Exception {
        final String got = equalTo.getDescription();

        assertThat(got).isEqualTo("equal to 1");
    }

    @Test
    public void getDescriptionForAtLeast() throws Exception {
        final String got = atLeast.getDescription();

        assertThat(got).isEqualTo("at least 1");
    }

    @Test
    public void getDescriptionForAtMost() throws Exception {
        final String got = atMost.getDescription();

        assertThat(got).isEqualTo("at most 1");
    }

    @Test
    public void getDescriptionForBetween() throws Exception {
        final String got = between.getDescription();

        assertThat(got).isEqualTo("between 2 and 4");
    }
}