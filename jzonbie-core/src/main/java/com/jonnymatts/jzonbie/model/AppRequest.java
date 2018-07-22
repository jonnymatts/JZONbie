package com.jonnymatts.jzonbie.model;

import com.google.common.collect.Sets;
import com.jonnymatts.jzonbie.model.content.BodyContent;

import java.util.*;

import static com.jonnymatts.jzonbie.util.Matching.*;
import static java.lang.String.format;
import static java.util.Collections.singletonMap;

public class AppRequest {

    private String path;
    private Map<String, String> headers;
    private String method;
    private BodyContent body;
    private Map<String, List<String>> queryParams;

    AppRequest() {
        this.headers = new HashMap<>();
        this.queryParams = new HashMap<>();
    }

    public static AppRequestBuilder get(String path) {
        return new AppRequestBuilder("GET", path);
    }

    public static AppRequestBuilder post(String path) {
        return new AppRequestBuilder("POST", path);
    }

    public static AppRequestBuilder head(String path) {
        return new AppRequestBuilder("HEAD", path);
    }

    public static AppRequestBuilder put(String path) {
        return new AppRequestBuilder("PUT", path);
    }

    public static AppRequestBuilder options(String path) {
        return new AppRequestBuilder("OPTIONS", path);
    }

    public static AppRequestBuilder delete(String path) {
        return new AppRequestBuilder("DELETE", path);
    }

    public static AppRequestBuilder builder(String method, String path) {
        return new AppRequestBuilder(method, path);
    }

    public String getPath() {
        return path;
    }

    void setPath(String path) {
        this.path = path;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getMethod() {
        return method;
    }

    void setMethod(String method) {
        this.method = method;
    }

    public BodyContent getBody() {
        return body;
    }

    void setBody(BodyContent body) {
        this.body = body;
    }

    void setBasicAuth(Map<String, String> basicAuth) {
        if(basicAuth != null) {
            basicAuth.entrySet().forEach(entry -> {
                final String authValue = format("%s:%s", entry.getKey(), entry.getValue());
                headers.put("Authorization", "Basic " + Base64.getEncoder().encodeToString(authValue.getBytes()));
            });
        }
    }

    void setBasicAuth(String username, String password) {
        setBasicAuth(singletonMap(username, password));
    }

    public Map<String, List<String>> getQueryParams() {
        return queryParams;
    }

    void setQueryParams(Map<String, List<String>> queryParams) {
        this.queryParams = queryParams;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        AppRequest request = (AppRequest) o;

        if(path != null ? !path.equals(request.path) : request.path != null) return false;
        if(headers != null ? !headers.equals(request.headers) : request.headers != null) return false;
        if(method != null ? !method.equals(request.method) : request.method != null) return false;
        if(body != null ? !body.equals(request.body) : request.body != null) return false;
        return queryParams != null ? queryParams.equals(request.queryParams) : request.queryParams == null;

    }

    @Override
    public int hashCode() {
        int result = path != null ? path.hashCode() : 0;
        result = 31 * result + (headers != null ? headers.hashCode() : 0);
        result = 31 * result + (method != null ? method.hashCode() : 0);
        result = 31 * result + (body != null ? body.hashCode() : 0);
        result = 31 * result + (queryParams != null ? queryParams.hashCode() : 0);
        return result;
    }

    public boolean matches(AppRequest that) {
        if(this == that) return true;

        if(path != null ? !that.path.matches(path) : that.path != null) return false;
        if(method != null ? !method.equals(that.method) : that.method != null) return false;
        if(queryParams != null ? !primedMapValuesAreContainedWithinOtherMap(queryParams, that.queryParams) : that.queryParams != null) return false;
        if(headers != null ? !primedMapValuesAreContainedWithinOtherMap(headers, that.headers) : that.headers != null) return false;

        return bodyContentsMatch(body, that.body);
    }

    @Override
    public String toString() {
        return "AppRequest{" +
                "path='" + path + '\'' +
                ", headers=" + headers +
                ", method='" + method + '\'' +
                ", body=" + body +
                ", queryParams=" + queryParams +
                '}';
    }

    private boolean primedMapValuesAreContainedWithinOtherMap(Map<String, ?> primedParams, Map<String, ?> otherParams) {
        final Set<String> primedParamsKeys = primedParams.keySet();
        final Set<String> otherParamsKeys = otherParams.keySet();

        if(!otherParamsKeys.containsAll(primedParamsKeys)) return false;

        final HashMap<String, ?> copy = new HashMap<>(otherParams);
        Sets.difference(otherParamsKeys, primedParamsKeys).forEach(copy::remove);

        return mapValuesMatchWithRegex(primedParams, copy);
    }
}