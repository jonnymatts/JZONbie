package com.jonnymatts.jzonbie.requests;

import com.jonnymatts.jzonbie.Body;
import com.jonnymatts.jzonbie.Response;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.util.Collections.singletonMap;

public class ZombieResponse implements Response {

    private static final Map<String, String> JSON_HEADERS_MAP = singletonMap("Content-Type", "application/json");

    private final int statusCode;
    private final Body<Object> body;

    public ZombieResponse(int statusCode, Object body) {
        this.statusCode = statusCode;
        this.body = new ObjectBody(body);
    }

    @Override
    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public Map<String, String> getHeaders() {
        return JSON_HEADERS_MAP;
    }

    @Override
    public Body<?> getBody() {
        return body;
    }

    @Override
    public Optional<Duration> getDelay() {
        return Optional.empty();
    }

    @Override
    public boolean isTemplated() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ZombieResponse that = (ZombieResponse) o;
        return statusCode == that.statusCode &&
                Objects.equals(body, that.body);
    }

    @Override
    public int hashCode() {
        return Objects.hash(statusCode, body);
    }

    @Override
    public String toString() {
        return "ZombieResponse{" +
                "statusCode=" + statusCode +
                ", body=" + body +
                '}';
    }

    private class ObjectBody implements Body<Object> {

        private Object obj;

        public ObjectBody(Object obj) {
            this.obj = obj;
        }

        @Override
        public Object getContent() {
            return obj;
        }

        @Override
        public boolean matches(Body<?> other) {
            return false;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ObjectBody that = (ObjectBody) o;
            return Objects.equals(obj, that.obj);
        }

        @Override
        public int hashCode() {
            return Objects.hash(obj);
        }

        @Override
        public String toString() {
            return "ObjectBody{" +
                    "obj=" + obj +
                    '}';
        }
    }
}