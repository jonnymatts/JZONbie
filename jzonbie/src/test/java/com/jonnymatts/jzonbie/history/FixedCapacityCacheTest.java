package com.jonnymatts.jzonbie.history;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FixedCapacityCacheTest {

    private FixedCapacityCache<Integer> underTest;

    @BeforeEach
    void setUp() {
        underTest = new FixedCapacityCache<>(3);
    }

    @Test
    void getExchangesReturnsAllCallsInHistory() throws Exception {
        underTest.add(1);
        underTest.add(2);
        underTest.add(3);

        final List<Integer> got = underTest.getValues();

        assertThat(got).containsExactly(1, 2, 3);
    }

    @Test
    void getExchangesReturnsCopyOfHistory() {
        underTest.add(1);

        final List<Integer> got = underTest.getValues();

        underTest.add(2);


        assertThat(got).containsExactly(1);
    }

    @Test
    void addAddsPrimingToCallHistory() throws Exception {
        underTest.add(1);

        final List<Integer> got = underTest.getValues();

        assertThat(got).containsExactly(1);
    }

    @Test
    void clearRemovesAllHistory() throws Exception {
        underTest.add(1);
        underTest.add(2);
        underTest.add(3);

        underTest.clear();

        final List<Integer> got = underTest.getValues();

        assertThat(got).isEmpty();
    }

    @Test
    void addDropsOldestPrimingFromHistoryWhenCallHistoryIsAlreadyFull() {
        underTest.add(1);
        underTest.add(2);
        underTest.add(3);
        underTest.add(4);

        assertThat(underTest.getValues()).containsExactly(2, 3, 4);

        underTest.add(5);

        assertThat(underTest.getValues()).containsExactly(3, 4, 5);
    }

}