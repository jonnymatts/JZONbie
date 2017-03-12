package com.jonnymatts.jzonbie.model;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class AppResponseBuilder {
    private final AppResponse response;

    AppResponseBuilder(int statusCode) {
        this.response = new AppResponse();
        response.setStatusCode(statusCode);
    }

    public AppResponse build() {
        return response;
    }

    public AppResponseBuilder withHeader(String name, String value) {
        if(response.getHeaders() == null)
            response.setHeaders(new HashMap<>());
        response.getHeaders().put(name, value);
        return this;
    }

    public AppResponseBuilder withBody(Map<String, Object> body) {
        response.setBody(body);
        return this;
    }

    public AppResponseBuilder contentType(String contentType) {
        return withHeader("Content-Type", contentType);
    }

    public AppResponseBuilder withDelay(Duration delay) {
        response.setDelay(delay);
        return this;
    }
}