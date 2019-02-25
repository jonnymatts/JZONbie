package com.jonnymatts.jzonbie.responses;

import com.jonnymatts.jzonbie.Body;
import com.jonnymatts.jzonbie.Response;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.util.Optional.ofNullable;

public class AppResponse implements Response {

    private int statusCode;
    private Map<String, String> headers;
    private Duration delay;
    private Body<?> body;
    private boolean templated;

    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    @Override
    public Body<?> getBody() {
        return body;
    }

    public void setBody(Body<?> body) {
        this.body = body;
    }

    @Override
    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
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
    public boolean isTemplated() {
        return templated;
    }

    public void setTemplated(boolean templated) {
        this.templated = templated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppResponse that = (AppResponse) o;
        return statusCode == that.statusCode &&
                templated == that.templated &&
                Objects.equals(headers, that.headers) &&
                Objects.equals(delay, that.delay) &&
                Objects.equals(body, that.body);
    }

    @Override
    public int hashCode() {
        return Objects.hash(statusCode, headers, delay, body, templated);
    }

    @Override
    public String toString() {
        return "AppResponse{" +
                "statusCode=" + statusCode +
                ", headers=" + headers +
                ", delay=" + delay +
                ", body=" + body +
                ", templated=" + templated +
                '}';
    }

    public static AppResponseBuilder builder(int statusCode) {
        return new AppResponseBuilder(statusCode);
    }

    public static AppResponseBuilder ok() {
        return new AppResponseBuilder(200);
    }

    public static AppResponseBuilder created() {
        return new AppResponseBuilder(201);
    }

    public static AppResponseBuilder accepted() {
        return new AppResponseBuilder(202);
    }

    public static AppResponseBuilder noContent() {
        return new AppResponseBuilder(204);
    }

    public static AppResponseBuilder badRequest() {
        return new AppResponseBuilder(400);
    }

    public static AppResponseBuilder unauthorized() {
        return new AppResponseBuilder(401);
    }

    public static AppResponseBuilder forbidden() {
        return new AppResponseBuilder(403);
    }

    public static AppResponseBuilder notFound() {
        return new AppResponseBuilder(404);
    }

    public static AppResponseBuilder methodNotAllowed() {
        return new AppResponseBuilder(405);
    }

    public static AppResponseBuilder conflict() {
        return new AppResponseBuilder(409);
    }

    public static AppResponseBuilder internalServerError() {
        return new AppResponseBuilder(500);
    }

    public static AppResponseBuilder serviceUnavailable() {
        return new AppResponseBuilder(503);
    }

    public static AppResponseBuilder gatewayTimeout() {
        return new AppResponseBuilder(504);
    }
}