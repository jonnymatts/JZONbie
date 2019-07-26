package com.jonnymatts.jzonbie.priming;


import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.jonnymatts.jzonbie.requests.AppRequest.get;
import static com.jonnymatts.jzonbie.responses.AppResponse.ok;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

class CallHistoryTest {

    private final ZombiePriming zombiePriming1 = new ZombiePriming(get("").build(), ok().build());
    private final ZombiePriming zombiePriming2 = new ZombiePriming(get("").build(), ok().build());
    private final ZombiePriming zombiePriming3 = new ZombiePriming(get("").build(), ok().build());

    @Test
    void getEntriesReturnsAllCallsInHistory() throws Exception {
        final CallHistory callHistory = new CallHistory(asList(zombiePriming1, zombiePriming2, zombiePriming3));

        final List<ZombiePriming> got = callHistory.getEntries();

        assertThat(got).containsExactly(zombiePriming1, zombiePriming2, zombiePriming3);
    }

    @Test
    void addAddsPrimingToCallHistory() throws Exception {
        final CallHistory callHistory = new CallHistory(new ArrayList<>());

        callHistory.add(zombiePriming1);

        final List<ZombiePriming> history = callHistory.getEntries();

        assertThat(history).containsExactly(zombiePriming1);
    }

    @Test
    void clearRemovesAllHistory() throws Exception {
        final CallHistory callHistory = new CallHistory(new ArrayList<ZombiePriming>(){{
            add(zombiePriming1);
            add(zombiePriming2);
            add(zombiePriming3);
        }});

        callHistory.clear();

        final List<ZombiePriming> history = callHistory.getEntries();

        assertThat(history).isEmpty();
    }

    @Test
    void countReturnsRequestCountOfMatchingRequestWhenHistoryHasASingleRequest() throws Exception {
        final CallHistory callHistory = new CallHistory(singletonList(zombiePriming1));

        final int got = callHistory.count(zombiePriming1.getRequest());

        assertThat(got).isEqualTo(1);
    }

    @Test
    void countReturnsRequestZeroIfCallHistoryIsEmpty() throws Exception {
        final CallHistory callHistory = new CallHistory(emptyList());

        final int got = callHistory.count(zombiePriming1.getRequest());

        assertThat(got).isEqualTo(0);
    }
}