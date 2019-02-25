package com.jonnymatts.jzonbie.responses;

import com.jonnymatts.jzonbie.Body;

import java.time.Duration;
import java.util.HashMap;

import static com.jonnymatts.jzonbie.util.Cloner.cloneResponse;

public class AppResponseBuilder {
    private AppResponse response;

    AppResponseBuilder(int statusCode) {
        this.response = new AppResponse();
        response.setStatusCode(statusCode);
    }

    public AppResponse build() {
        return response;
    }

    public AppResponseBuilder templated() {
        response = cloneResponse(response);
        response.setTemplated(true);
        return this;
    }

    public AppResponseBuilder withHeader(String name, String value) {
        response = cloneResponse(response);
        if(response.getHeaders() == null)
            response.setHeaders(new HashMap<>());
        response.getHeaders().put(name, value);
        return this;
    }

    public AppResponseBuilder withBody(Body<?> body) {
        response = cloneResponse(response);
        response.setBody(body);
        return this;
    }

    public AppResponseBuilder withDelay(Duration delay) {
        response = cloneResponse(response);
        response.setDelay(delay);
        return this;
    }

    public AppResponseBuilder contentType(String contentType) {
        return withHeader("Content-Type", contentType);
    }
}