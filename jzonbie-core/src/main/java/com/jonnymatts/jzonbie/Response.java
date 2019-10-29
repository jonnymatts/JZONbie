package com.jonnymatts.jzonbie;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

public interface Response {

    int getStatusCode();

    Map<String, String> getHeaders();

    Body<?> getBody();

    default Optional<Duration> getDelay() {
        return Optional.empty();
    }

    boolean isTemplated();
}