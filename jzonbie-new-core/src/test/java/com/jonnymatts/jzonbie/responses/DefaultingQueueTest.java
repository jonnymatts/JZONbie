package com.jonnymatts.jzonbie.responses;

import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static com.jonnymatts.jzonbie.responses.AppResponse.*;
import static com.jonnymatts.jzonbie.responses.DefaultAppResponse.StaticDefaultAppResponse.staticDefault;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class DefaultingQueueTest {

    private final AppResponse response1 = ok().build();
    private final AppResponse response2 = notFound().build();
    private final AppResponse response3 = internalServerError().build();

    @Test
    public void pollReturnsNullIfThereIsNoDefaultElementSetAndQueueIsEmpty() throws Exception {
        final DefaultingQueue queue = new DefaultingQueue();

        final AppResponse got = queue.poll();

        assertThat(got).isNull();
    }

    @Test
    public void pollReturnsDefaultValueIfThereIsADefaultElementSetAndQueueIsEmpty() throws Exception {
        final DefaultingQueue queue = new DefaultingQueue();
        queue.setDefault(staticDefault(response1));

        final AppResponse got = queue.poll();

        assertThat(got).isEqualTo(response1);
    }

    @Test
    public void pollReturnsQueueElementIfThereIsADefaultElementSetAndQueueContainsAnElement() throws Exception {
        final DefaultingQueue queue = new DefaultingQueue();
        queue.setDefault(staticDefault(response1));
        queue.add(response2);

        final AppResponse got = queue.poll();

        assertThat(got).isEqualTo(response2);
    }

    @Test
    public void pollReturnsQueueElementIfThereIsNoDefaultElementSetAndQueueContainsAnElement() throws Exception {
        final DefaultingQueue queue = new DefaultingQueue();
        queue.add(response2);

        final AppResponse got = queue.poll();

        assertThat(got).isEqualTo(response2);
    }

    @Test
    public void resetClearsQueueAndRemovesDefaultElement() throws Exception {
        final DefaultingQueue queue = new DefaultingQueue();
        queue.setDefault(staticDefault(response1));
        queue.add(response2);
        queue.reset();

        final AppResponse got = queue.poll();

        assertThat(got).isNull();
    }

    @Test
    public void hasSizeReturnsSizeOfQueue() throws Exception {
        final DefaultingQueue queue = new DefaultingQueue();
        queue.add(asList(response1, response2, response3));

        final int got = queue.hasSize();

        assertThat(got).isEqualTo(3);
    }

    @Test
    public void getEntriesReturnsQueueElementsInOrder() throws Exception {
        final DefaultingQueue queue = new DefaultingQueue();
        queue.add(asList(response1, response2, response3));

        final List<AppResponse> got = queue.getEntries();

        assertThat(got).containsExactly(response1, response2, response3);
    }

    @Test
    public void getDefaultReturnsEmptyOptionalIfDefaultIsNotSet() throws Exception {
        final DefaultingQueue queue = new DefaultingQueue();

        final Optional<DefaultAppResponse> got = queue.getDefault();

        assertThat(got.isPresent()).isFalse();
    }

    @Test
    public void getDefaultReturnsOptionalIfDefaultIsSet() throws Exception {
        final DefaultingQueue queue = new DefaultingQueue();
        queue.setDefault(staticDefault(response1));

        final Optional<DefaultAppResponse> got = queue.getDefault();

        assertThat(got.get().getResponse()).isEqualTo(response1);
    }
}