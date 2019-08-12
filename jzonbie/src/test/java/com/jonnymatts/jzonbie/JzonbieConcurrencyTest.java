package com.jonnymatts.jzonbie;

import com.google.common.base.Stopwatch;
import com.jonnymatts.jzonbie.junit.JzonbieExtension;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

import static com.jonnymatts.jzonbie.requests.AppRequest.get;
import static com.jonnymatts.jzonbie.responses.AppResponse.ok;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(JzonbieExtension.class)
class JzonbieConcurrencyTest {

    private HttpClient httpClient;

    @BeforeEach
    void setUp(Jzonbie jzonbie) throws Exception {
        final PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(10);
        connectionManager.setDefaultMaxPerRoute(10);
        httpClient = HttpClientBuilder.create().setConnectionManager(connectionManager).build();

        IntStream.range(0, 10).boxed().forEach(i -> primeZombieWithDelay(i, jzonbie));
    }

    @Test
    void jzonbieCanServePrimedResponsesWithDifferentDelaysConcurrently(Jzonbie jzonbie) throws Exception {
        final List<Callable<Integer>> callables = IntStream.range(0, 10).boxed()
                .map(i -> (Callable<Integer>)() -> {
                    final HttpUriRequest request = createRequest(i, jzonbie);
                    final Stopwatch stopwatch = Stopwatch.createStarted();
                    final HttpResponse response;
                    try {
                        response = httpClient.execute(request);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    stopwatch.stop();

                    final int statusCode = response.getStatusLine().getStatusCode();
                    HttpClientUtils.closeQuietly(response);

                    assertThat(statusCode).isEqualTo(200);

                    final long elapsed = stopwatch.elapsed(MILLISECONDS);

                    assertThat(elapsed).as("Request with %d second delay duration: %d", i, elapsed).isLessThanOrEqualTo((i * 1000) + 1500);
                    return i;
                }).collect(toList());

        final ExecutorService executorService = Executors.newFixedThreadPool(10);

        final List<Future<?>> futures = callables.stream().map(executorService::submit).collect(toList());

        final Stopwatch stopwatch = Stopwatch.createStarted();

        while(!futures.stream().allMatch(Future::isDone)) {
            System.out.println("Waiting for futures to finish, time elapsed: " + stopwatch.elapsed(MILLISECONDS) + " millis");
            Thread.sleep(500);
        }

        futures.forEach(future -> {
            try {
                final Object i = future.get();
                System.out.println("Request worked when i = " + i);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private HttpUriRequest createRequest(int i, Jzonbie jzonbie) {
        return RequestBuilder.get("http://localhost:" + jzonbie.getHttpPort() + "/" + i).build();
    }

    private void primeZombieWithDelay(int i, Jzonbie jzonbie) {
        jzonbie.prime(
                get("/" + i),
                ok().withDelay(Duration.of(i, SECONDS))
        );
    }
}