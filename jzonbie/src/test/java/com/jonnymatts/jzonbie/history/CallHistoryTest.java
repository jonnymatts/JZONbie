package com.jonnymatts.jzonbie.history;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.jonnymatts.jzonbie.requests.AppRequest.get;
import static com.jonnymatts.jzonbie.responses.AppResponse.ok;
import static org.assertj.core.api.Assertions.assertThat;

class CallHistoryTest {

    private final Exchange exchange1 = new Exchange(get("1"), ok());

    private CallHistory underTest;

    @BeforeEach
    void setUp() {
        underTest = new CallHistory(3);
    }

    @Test
    void countReturnsRequestCountOfMatchingRequestWhenHistoryHasASingleRequest() throws Exception {
        underTest.add(exchange1);

        final int got = underTest.count(exchange1.getRequest());

        assertThat(got).isEqualTo(1);
    }

    @Test
    void countReturnsRequestZeroIfCallHistoryIsEmpty() throws Exception {
        final int got = underTest.count(exchange1.getRequest());

        assertThat(got).isEqualTo(0);
    }
}