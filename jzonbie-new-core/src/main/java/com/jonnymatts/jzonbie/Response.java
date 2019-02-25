package com.jonnymatts.jzonbie;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

public interface Response<T extends Body<?>> {

    int getStatusCode();

    Map<String, String> getHeaders();

    T getBody();

    default Optional<Duration> getDelay() {
        return Optional.empty();
    }

    boolean isTemplated();
}