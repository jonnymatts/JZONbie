package com.jonnymatts.jzonbie.response;

import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class DefaultingQueueTest {

    @Test
    public void pollReturnsNullIfThereIsNoDefaultElementSetAndQueueIsEmpty() throws Exception {
        final DefaultingQueue<Integer> queue = new DefaultingQueue<>();

        final Integer got = queue.poll();

        assertThat(got).isNull();
    }

    @Test
    public void pollReturnsDefaultValueIfThereIsADefaultElementSetAndQueueIsEmpty() throws Exception {
        final DefaultingQueue<Integer> queue = new DefaultingQueue<>();
        queue.setDefault(1);

        final Integer got = queue.poll();

        assertThat(got).isEqualTo(1);
    }

    @Test
    public void pollReturnsQueueElementIfThereIsADefaultElementSetAndQueueContainsAnElement() throws Exception {
        final DefaultingQueue<Integer> queue = new DefaultingQueue<>();
        queue.setDefault(1);
        queue.add(2);

        final Integer got = queue.poll();

        assertThat(got).isEqualTo(2);
    }

    @Test
    public void pollReturnsQueueElementIfThereIsNoDefaultElementSetAndQueueContainsAnElement() throws Exception {
        final DefaultingQueue<Integer> queue = new DefaultingQueue<>();
        queue.add(2);

        final Integer got = queue.poll();

        assertThat(got).isEqualTo(2);
    }

    @Test
    public void resetClearsQueueAndRemovesDefaultElement() throws Exception {
        final DefaultingQueue<Integer> queue = new DefaultingQueue<>();
        queue.setDefault(1);
        queue.add(2);
        queue.reset();

        final Integer got = queue.poll();

        assertThat(got).isNull();
    }

    @Test
    public void hasSizeReturnsSizeOfQueue() throws Exception {
        final DefaultingQueue<Integer> queue = new DefaultingQueue<>();
        queue.add(asList(1, 2, 3));

        final int got = queue.hasSize();

        assertThat(got).isEqualTo(3);
    }

    @Test
    public void getEntriesReturnsQueueElementsInOrder() throws Exception {
        final DefaultingQueue<Integer> queue = new DefaultingQueue<>();
        queue.add(asList(1, 2, 3));

        final List<Integer> got = queue.getEntries();

        assertThat(got).containsExactlyInAnyOrder(1, 2, 3);
    }

    @Test
    public void getDefaultReturnsEmptyOptionalIfDefaultIsNotSet() throws Exception {
        final DefaultingQueue<Integer> queue = new DefaultingQueue<>();

        final Optional<Integer> got = queue.getDefault();

        assertThat(got.isPresent()).isFalse();
    }

    @Test
    public void getDefaultReturnsOptionalIfDefaultIsSet() throws Exception {
        final DefaultingQueue<Integer> queue = new DefaultingQueue<>();
        queue.setDefault(1);

        final Optional<Integer> got = queue.getDefault();

        assertThat(got).contains(1);
    }
}