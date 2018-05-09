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

    @Override
    public String toString() {
        return "AppResponse{" +
                "statusCode=" + statusCode +
                ", headers=" + headers +
                ", delay=" + delay +
                ", body=" + body +
                '}';
    }

    public static AppResponseBuilder builder(int statusCode) {
        return new AppResponseBuilder(statusCode);
    }
}