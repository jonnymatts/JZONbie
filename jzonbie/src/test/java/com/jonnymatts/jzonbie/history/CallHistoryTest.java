package com.jonnymatts.jzonbie.history;


import com.jonnymatts.jzonbie.requests.AppRequest;
import org.assertj.core.util.Files;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static com.jonnymatts.jzonbie.requests.AppRequest.get;
import static com.jonnymatts.jzonbie.responses.AppResponse.ok;
import static org.assertj.core.api.Assertions.assertThat;

class CallHistoryTest {

    private final AppRequest primedRequest = get("1");
    private final AppRequest primedRequest2 = get("2");
    private final Exchange exchange1 = new Exchange(primedRequest, ok());
    private final Exchange exchange2 = new Exchange(primedRequest2, ok());

    private CallHistory underTest;
    private File persistedFile;

    @BeforeEach
    void setUp() {
        persistedFile = Files.newTemporaryFile();
        underTest = new CallHistory(3, persistedFile);
    }

    @Test
    void addReturnsAccurateSnapshotOfHistory() {
        CallHistorySnapshot snapshot = underTest.add(primedRequest, exchange1);

        assertThat(snapshot.getCount()).isEqualTo(1);
        assertThat(snapshot.getPersistedCount()).isEqualTo(1);
    }

    @Test
    void addReturnsAccurateSnapshotOfHistoryForResetHistory() {
        underTest.add(primedRequest, exchange1);
        underTest.clear();
        CallHistorySnapshot snapshot = underTest.add(primedRequest, exchange1);

        assertThat(snapshot.getCount()).isEqualTo(1);
        assertThat(snapshot.getPersistedCount()).isEqualTo(2);
    }

    @Test
    void addReturnsAccurateSnapshotOfHistoryForMultipleRequests() {
        CallHistorySnapshot snapshot = underTest.add(primedRequest, exchange1);

        underTest.add(primedRequest2, exchange2);
        CallHistorySnapshot snapshotTwo = underTest.add(primedRequest2, exchange2);

        assertThat(snapshot.getCount()).isEqualTo(1);
        assertThat(snapshot.getPersistedCount()).isEqualTo(1);

        assertThat(snapshotTwo.getCount()).isEqualTo(2);
        assertThat(snapshotTwo.getPersistedCount()).isEqualTo(2);
    }

    @Test
    void addCanContinueToIncrementOverMultipleInstances() {
        underTest.add(exchange1.getRequest(), exchange1);
        new CallHistory(6, persistedFile).add(exchange1.getRequest(), exchange1);

        final CallHistorySnapshot snapshot = new CallHistory(5, persistedFile).add(exchange1.getRequest(), exchange1);
        assertThat(snapshot.getCount()).isEqualTo(1);
        assertThat(snapshot.getPersistedCount()).isEqualTo(3);
    }

    @Test
    void countReturnsRequestCountOfMatchingRequestWhenHistoryHasASingleRequest() throws Exception {
        underTest.add(primedRequest, exchange1);

        final int got = underTest.count(exchange1.getRequest());

        assertThat(got).isEqualTo(1);
    }

    @Test
    void countIsNotAffectedBySizeOfHistoryCache() throws Exception {
        underTest.add(primedRequest, exchange1);
        underTest.add(primedRequest, exchange1);
        underTest.add(primedRequest, exchange1);
        underTest.add(primedRequest, exchange1);
        underTest.add(primedRequest, exchange1);

        final int got = underTest.count(exchange1.getRequest());

        assertThat(got).isEqualTo(5);
    }

    @Test
    void countReturnsRequestZeroIfCallHistoryIsEmpty() throws Exception {
        final int got = underTest.count(exchange1.getRequest());

        assertThat(got).isEqualTo(0);
    }

    @Test
    void countReturnsPersistedRequestCountOfMatchingRequestWhenHistoryHasASingleRequest() throws Exception {
        underTest.add(primedRequest, exchange1);

        final int got = underTest.getPersistedCount(exchange1.getRequest());

        assertThat(got).isEqualTo(1);
    }

    @Test
    void canPersistCountOverMultipleInstances() {
        underTest.add(exchange1.getRequest(), exchange1);
        new CallHistory(6, persistedFile).add(exchange1.getRequest(), exchange1);

        assertThat(new CallHistory(5, persistedFile).getPersistedCount(exchange1.getRequest())).isEqualTo(2);
    }

    @Test
    void persistedCountReturnsZeroWhenRequestNotFound() throws Exception {
        final int got = underTest.getPersistedCount(exchange1.getRequest());

        assertThat(got).isEqualTo(0);
    }

    @Test
    void clearOnlyClearsNonPersistentStores() {
        underTest.add(exchange1.getRequest(), exchange1);
        underTest.add(exchange1.getRequest(), exchange1);
        underTest.add(exchange1.getRequest(), exchange1);

        underTest.clear();

        final int count = underTest.count(exchange1.getRequest());
        final int persistentCount = underTest.getPersistedCount(exchange1.getRequest());

        assertThat(count).isEqualTo(0);
        assertThat(underTest.getValues()).isEmpty();
        assertThat(persistentCount).isEqualTo(3);
    }
}