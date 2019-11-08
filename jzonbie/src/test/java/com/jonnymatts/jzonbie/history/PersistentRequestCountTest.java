package com.jonnymatts.jzonbie.history;

import com.jonnymatts.jzonbie.persistence.JzonbiePersistenceException;
import org.assertj.core.util.Files;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.OptionalInt;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static java.nio.file.Files.createTempFile;
import static java.nio.file.Files.lines;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.stream.IntStream.range;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

class PersistentRequestCountTest {

    private static final String REQUEST_KEY = "requestKey";
    private static final String NEW_REQUEST_KEY = "newRequestKey";

    @Test
    void addNewCounterEntryToFile() throws IOException {
        File fileLocation = createTempFile("a", "b").toFile();

        PersistentRequestCount underTest = new PersistentRequestCount(fileLocation);

        final int count = underTest.incrementCounter(REQUEST_KEY);

        assertThatFileHasLineMatching(fileLocation, REQUEST_KEY + ":1");
        assertThat(count).isEqualTo(1);
    }

    @Test
    void canIncrementCounterOnExistingEntry() throws IOException {
        File fileLocation = createTempFile("a", "b").toFile();

        PersistentRequestCount underTest = new PersistentRequestCount(fileLocation);

        underTest.incrementCounter(REQUEST_KEY);
        final int count = underTest.incrementCounter(REQUEST_KEY);

        assertThatFileHasLineMatching(fileLocation, REQUEST_KEY + ":2");
        assertThat(count).isEqualTo(2);
    }

    @Test
    void keyMatchesRequestKey() throws IOException {
        File fileLocation = createTempFile("a", "b").toFile();

        PersistentRequestCount underTest = new PersistentRequestCount(fileLocation);

        underTest.incrementCounter("newRequestKey");

        assertThatFileHasLineMatching(fileLocation, NEW_REQUEST_KEY + ":1");
    }

    @Test
    void incrementMultipleCounterEntriesOnFile() throws IOException {
        File fileLocation = createTempFile("a", "b").toFile();

        PersistentRequestCount underTest = new PersistentRequestCount(fileLocation);

        underTest.incrementCounter(REQUEST_KEY);
        underTest.incrementCounter(NEW_REQUEST_KEY);

        underTest.incrementCounter(NEW_REQUEST_KEY);
        final int requestCount = underTest.incrementCounter(REQUEST_KEY);
        final int newRequestCount = underTest.incrementCounter(NEW_REQUEST_KEY);

        assertThatFileHasLineMatching(fileLocation, REQUEST_KEY + ":2");
        assertThatFileHasLineMatching(fileLocation, NEW_REQUEST_KEY + ":3");
        assertThat(requestCount).isEqualTo(2);
        assertThat(newRequestCount).isEqualTo(3);
    }

    @Test
    void missingFileThrowsJzonbiePersistenceException() {
        assertThatThrownBy(() -> new PersistentRequestCount(new File("missingFileLocation")).incrementCounter(REQUEST_KEY))
                .hasMessage("Failure incrementing persistence counter")
                .isInstanceOf(JzonbiePersistenceException.class);
    }

    @Test
    void invalidFileThrowsJzonbiePersistenceException() throws IOException {
        File fileLocation = createTempFile("a", "b").toFile();

        try(FileWriter writer = new FileWriter(fileLocation)) {
         writer.write(REQUEST_KEY+":content");
        }

        assertThatThrownBy(() -> new PersistentRequestCount(fileLocation).incrementCounter(REQUEST_KEY))
                .hasMessage("Failure incrementing persistence counter")
                .isInstanceOf(JzonbiePersistenceException.class);
    }

    @Test
    void getCountFromPersistedFile() throws IOException {
        File fileLocation = createTempFile("a", "b").toFile();

        PersistentRequestCount underTest = new PersistentRequestCount(fileLocation);

        underTest.incrementCounter(REQUEST_KEY);
        underTest.incrementCounter(REQUEST_KEY);
        final int count = underTest.getCount(REQUEST_KEY).getAsInt();

        assertThat(count).isEqualTo(2);
    }

    @Test
    void getCountFromPersistedFileThatDoesNotExistReturnsOptionalEmpty() throws IOException {
        File fileLocation = createTempFile("a", "b").toFile();

        PersistentRequestCount underTest = new PersistentRequestCount(fileLocation);

        underTest.incrementCounter(REQUEST_KEY);
        final OptionalInt optionalCount = underTest.getCount("missingKey");

        assertThat(optionalCount).isEmpty();
    }

    @Test
    void getCountFromPersistedFileWithMissingFileThrowsJzonbiePersistenceException() throws IOException {
        PersistentRequestCount underTest = new PersistentRequestCount(new File("nonExistentFile"));

        assertThatThrownBy(() -> underTest.getCount(REQUEST_KEY))
                .isInstanceOf(JzonbiePersistenceException.class);
    }

    @Test
    void persistAllCountsWhenWritingConcurrently() {
        final String key = "key1234";
        PersistentRequestCount underTest = new PersistentRequestCount(Files.newTemporaryFile());
        range(1, 50).parallel().forEach( i -> underTest.incrementCounter(key));

        assertThat(underTest.getCount(key).getAsInt()).isEqualTo(49);
    }

    @Test
    void cannotReadWhilstWritingToFile() throws Exception {
        final String key = "key1234";
        PersistentRequestCount underTest = new PersistentRequestCount(Files.newTemporaryFile());
        CountDownLatch latch = new CountDownLatch(1);

        ExecutorService executorService = newFixedThreadPool(3);
        File mockedFile = spy(Files.newTemporaryFile());

        when(mockedFile.getPath()).then(invocation -> {
            while(latch.getCount() > 0){}
            return invocation;
        }).thenCallRealMethod();

        executorService.execute(() -> underTest.incrementCounter(key));

        latch.countDown();
        Future<Integer> count = executorService.submit(() -> underTest.getCount(key).getAsInt());

        assertThat(count.get()).isEqualTo(1);

    }

    private void assertThatFileHasLineMatching(File fileLocation, String lineExpected) throws IOException {
        assertThat(lines(fileLocation.toPath())).contains(lineExpected);
    }

}