package com.jonnymatts.jzonbie.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jonnymatts.jzonbie.model.content.BodyContent;
import com.jonnymatts.jzonbie.response.Response;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.ofNullable;

public class AppResponse implements Response {

    private int statusCode;
    private Map<String, String> headers;
    private Duration delay;
    private BodyContent body;

    public Map<String, String> getHeaders() {
        return headers;
    }

    void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public BodyContent getBody() {
        return body;
    }

    @Override
    @JsonIgnore
    public boolean isFileResponse() {
        return false;
    }

    void setBody(BodyContent body) {
        this.body = body;
    }

    public int getStatusCode() {
        return statusCode;
    }

    void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public void setDelay(Duration delay) {
        this.delay = delay;
    }

    @Override
    public Optional<Duration> getDelay() {
        return ofNullable(delay);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AppResponse that = (AppResponse) o;

        if (statusCode != that.statusCode) return false;
        if (headers != null ? !headers.equals(that.headers) : that.headers != null) return false;
        if (body != null ? !body.equals(that.body) : that.body != null) return false;
        return delay != null ? delay.equals(that.delay) : that.delay == null;
    }

    @Override
    public int hashCode() {
        int result = statusCode;
        result = 31 * result + (headers != null ? headers.hashCode() : 0);
        result = 31 * result + (body != null ? body.hashCode() : 0);
        result = 31 * result + (delay != null ? delay.hashCode() : 0);
        return result;
    }

    public static AppResponseBuilder builder(int statusCode) {
        return new AppResponseBuilder(statusCode);
    }
}