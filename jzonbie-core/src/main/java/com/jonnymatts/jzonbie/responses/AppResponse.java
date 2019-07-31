package com.jonnymatts.jzonbie.responses;

import com.jonnymatts.jzonbie.Response;
import com.jonnymatts.jzonbie.body.BodyContent;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.*;

import static com.jonnymatts.jzonbie.body.ArrayBodyContent.arrayBody;
import static com.jonnymatts.jzonbie.body.LiteralBodyContent.literalBody;
import static com.jonnymatts.jzonbie.body.ObjectBodyContent.objectBody;
import static com.jonnymatts.jzonbie.util.Copier.copyBodyContent;
import static com.jonnymatts.jzonbie.util.Copier.copyMap;
import static java.util.Optional.ofNullable;

public class AppResponse implements Response {

    private int statusCode;
    private Map<String, String> headers;
    private Duration delay;
    private BodyContent<?> body;
    private boolean templated;

    public AppResponse() {
    }

    public AppResponse(int statusCode) {
        this.statusCode = statusCode;
    }

    @SuppressWarnings("CopyConstructorMissesField")
    public AppResponse(AppResponse appResponse) {
        setStatusCode(appResponse.getStatusCode());
        setHeaders(copyMap(appResponse.getHeaders()));
        setBody(copyBodyContent(appResponse.getBody()));
        appResponse.getDelay().ifPresent(this::setDelay);
        setTemplated(appResponse.isTemplated());
    }

    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    @Override
    public BodyContent<?> getBody() {
        return body;
    }

    public void setBody(BodyContent<?> body) {
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

    public AppResponse templated() {
        this.setTemplated(true);
        return this;
    }

    public AppResponse withHeader(String name, String value) {
        if(this.getHeaders() == null)
            this.setHeaders(new HashMap<>());
        this.getHeaders().put(name, value);
        return this;
    }

    public AppResponse withBody(BodyContent<?> body) {
        this.setBody(body);
        return this;
    }

    public AppResponse withBody(Map<String, ?> body) {
        this.setBody(objectBody(body));
        return this;
    }

    public AppResponse withBody(String body) {
        this.setBody(literalBody(body));
        return this;
    }

    public AppResponse withBody(List<?> body) {
        this.setBody(arrayBody(body));
        return this;
    }

    public AppResponse withBody(Number body) {
        this.setBody(literalBody(new BigDecimal(body.doubleValue())));
        return this;
    }

    public AppResponse withDelay(Duration delay) {
        this.setDelay(delay);
        return this;
    }

    public AppResponse contentType(String contentType) {
        return withHeader("Content-Type", contentType);
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

    public static AppResponse response(int statusCode) {
        return new AppResponse(statusCode);
    }

    public static AppResponse ok() {
        return new AppResponse(200);
    }

    public static AppResponse created() {
        return new AppResponse(201);
    }

    public static AppResponse accepted() {
        return new AppResponse(202);
    }

    public static AppResponse noContent() {
        return new AppResponse(204);
    }

    public static AppResponse badRequest() {
        return new AppResponse(400);
    }

    public static AppResponse unauthorized() {
        return new AppResponse(401);
    }

    public static AppResponse forbidden() {
        return new AppResponse(403);
    }

    public static AppResponse notFound() {
        return new AppResponse(404);
    }

    public static AppResponse methodNotAllowed() {
        return new AppResponse(405);
    }

    public static AppResponse conflict() {
        return new AppResponse(409);
    }

    public static AppResponse internalServerError() {
        return new AppResponse(500);
    }

    public static AppResponse serviceUnavailable() {
        return new AppResponse(503);
    }

    public static AppResponse gatewayTimeout() {
        return new AppResponse(504);
    }
}