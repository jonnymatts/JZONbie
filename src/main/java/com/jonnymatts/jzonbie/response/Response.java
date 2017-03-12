package com.jonnymatts.jzonbie.response;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

public interface Response {
    int getStatusCode();
    Map<String, String> getHeaders();
    Object getBody();
    boolean isFileResponse();
    Optional<Duration> getDelay();
}