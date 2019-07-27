package com.jonnymatts.jzonbie.requests;

import com.google.common.collect.Sets;
import com.jonnymatts.jzonbie.body.BodyContent;

import java.math.BigDecimal;
import java.util.*;

import static com.jonnymatts.jzonbie.body.ArrayBodyContent.arrayBody;
import static com.jonnymatts.jzonbie.body.LiteralBodyContent.literalBody;
import static com.jonnymatts.jzonbie.body.ObjectBodyContent.objectBody;
import static com.jonnymatts.jzonbie.util.Copier.copyBodyContent;
import static com.jonnymatts.jzonbie.util.Copier.copyMap;
import static com.jonnymatts.jzonbie.util.Matching.bodyContentsMatch;
import static com.jonnymatts.jzonbie.util.Matching.mapValuesMatchWithRegex;
import static java.lang.String.format;
import static java.util.Collections.singletonMap;

public class AppRequest {

    private String path;
    private Map<String, String> headers;
    private String method;
    private BodyContent<?> body;
    private Map<String, List<String>> queryParams;

    public AppRequest() {
        this.headers = new HashMap<>();
        this.queryParams = new HashMap<>();
    }

    public AppRequest(String method, String path) {
        this.method = method;
        this.path = path;
        this.headers = new HashMap<>();
        this.queryParams = new HashMap<>();
    }

    public AppRequest(AppRequest appRequest) {
        this(appRequest.method, appRequest.path);
        setPath(appRequest.getPath());
        setMethod(appRequest.getMethod());
        setQueryParams(copyMap(appRequest.getQueryParams()));
        setHeaders(copyMap(appRequest.getHeaders()));
        setBody(copyBodyContent(appRequest.getBody()));
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public BodyContent<?> getBody() {
        return body;
    }

    public void setBody(BodyContent<?> body) {
        this.body = body;
    }

    private void setBasicAuth(Map<String, String> basicAuth) {
        if(basicAuth != null) {
            basicAuth.forEach((key, value) -> {
                final String authValue = format("%s:%s", key, value);
                headers.put("Authorization", "Basic " + Base64.getEncoder().encodeToString(authValue.getBytes()));
            });
        }
    }

    public void setBasicAuth(String username, String password) {
        setBasicAuth(singletonMap(username, password));
    }

    public Map<String, List<String>> getQueryParams() {
        return queryParams;
    }

    public void setQueryParams(Map<String, List<String>> queryParams) {
        this.queryParams = queryParams;
    }

    public AppRequest withHeader(String name, String value) {
        final AppRequest clone = new AppRequest(this);
        if(clone.getHeaders() == null)
            clone.setHeaders(new HashMap<>());
        clone.getHeaders().put(name, value);
        return clone;
    }

    public AppRequest withBody(BodyContent<?> body) {
        final AppRequest clone = new AppRequest(this);
        clone.setBody(body);
        return clone;
    }

    public AppRequest withBody(Map<String, ?> body) {
        final AppRequest clone = new AppRequest(this);
        clone.setBody(objectBody(body));
        return clone;
    }

    public AppRequest withBody(String body) {
        final AppRequest clone = new AppRequest(this);
        clone.setBody(literalBody(body));
        return clone;
    }

    public AppRequest withBody(List<?> body) {
        final AppRequest clone = new AppRequest(this);
        clone.setBody(arrayBody(body));
        return clone;
    }

    public AppRequest withBody(Number body) {
        final AppRequest clone = new AppRequest(this);
        clone.setBody(literalBody(new BigDecimal(body.doubleValue())));
        return clone;
    }

    public AppRequest withQueryParam(String name, String value) {
        final AppRequest clone = new AppRequest(this);
        Map<String, List<String>> queryParams = clone.getQueryParams();
        if (queryParams == null) {
            queryParams = new HashMap<>();
            clone.setQueryParams(queryParams);
        }

        if (!queryParams.containsKey(name)) {
            queryParams.put(name, new ArrayList<>());
        }

        queryParams.get(name).add(value);

        return clone;
    }

    public AppRequest withBasicAuth(String username, String password) {
        final AppRequest clone = new AppRequest(this);
        clone.setBasicAuth(username, password);
        return clone;
    }

    public AppRequest accept(String contentType) {
        return withHeader("Accept", contentType);
    }

    public AppRequest contentType(String contentType) {
        return withHeader("Content-Type", contentType);
    }

    public static AppRequest request(String method, String path) {
        return new AppRequest(method, path);
    }

    public static AppRequest get(String path) {
        return new AppRequest("GET", path);
    }

    public static AppRequest post(String path) {
        return new AppRequest("POST", path);
    }

    public static AppRequest head(String path) {
        return new AppRequest("HEAD", path);
    }

    public static AppRequest put(String path) {
        return new AppRequest("PUT", path);
    }

    public static AppRequest options(String path) {
        return new AppRequest("OPTIONS", path);
    }

    public static AppRequest delete(String path) {
        return new AppRequest("DELETE", path);
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