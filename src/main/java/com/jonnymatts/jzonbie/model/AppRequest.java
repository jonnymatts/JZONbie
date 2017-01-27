package com.jonnymatts.jzonbie.model;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Collections.singletonMap;

public class AppRequest {

    private String path;
    private Map<String, String> headers;
    private String method;
    private Map<String, Object> body;
    private Map<String, String> basicAuth;
    private Map<String, List<String>> queryParams;

    public AppRequest() {
        this.headers = new HashMap<>();
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

    public Map<String, Object> getBody() {
        return body;
    }

    public void setBody(Map<String, Object> body) {
        this.body = body;
    }

    public Map<String, String> getBasicAuth() {
        return basicAuth;
    }

    public void setBasicAuth(Map<String, String> basicAuth) {
        basicAuth.entrySet().forEach(entry -> {
            final String authValue = format("%s:%s", entry.getKey(), entry.getValue());
            headers.put("Authorization", "Basic " + Base64.getEncoder().encodeToString(authValue.getBytes()));
        });
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AppRequest that = (AppRequest) o;

        if (path != null ? !path.equals(that.path) : that.path != null) return false;
        if (method != null ? !method.equals(that.method) : that.method != null) return false;
        if (queryParams != null ? !queryParams.equals(that.queryParams) : that.queryParams != null) return false;
        return body != null ? body.equals(that.body) : that.body == null;

    }

    @Override
    public int hashCode() {
        int result = path != null ? path.hashCode() : 0;
        result = 31 * result + (method != null ? method.hashCode() : 0);
        result = 31 * result + (body != null ? body.hashCode() : 0);
        result = 31 * result + (queryParams != null ? queryParams.hashCode() : 0);
        return result;
    }

    public boolean matches(AppRequest that) {
        if(this == that) return true;

        if(path != null ? !path.equals(that.path) : that.path != null) return false;
        if(method != null ? !method.equals(that.method) : that.method != null) return false;
        if(queryParams != null ? !queryParametersMatchWithRegex(that.queryParams) : that.queryParams != null) return false;
        if(headers != null ? !headersAreContainedWithinOtherRequestsHeaders(that.headers) : that.headers != null) return false;

        return body != null ? body.equals(that.body) : that.body == null;
    }

    private boolean headersAreContainedWithinOtherRequestsHeaders(Map<String, String> otherHeaders) {
        return headers.entrySet().stream().allMatch(e -> {
            final String value = otherHeaders.get(e.getKey());
            return value != null && value.equals(e.getValue());
        });
    }

    private boolean queryParametersMatchWithRegex(Map<String, List<String>> otherQueryParams) {
        return queryParams.entrySet().stream().allMatch(e -> {
            final List<String> otherValues = otherQueryParams.get(e.getKey());
            return otherValues != null && e.getValue() != null && listsMatchesRegex(e.getValue(), otherValues);
        });
    }

    private boolean listsMatchesRegex(List<String> patterns, List<String> values) {
        if (patterns.size() != values.size())
            return false;

        for (int i = 0; i < patterns.size(); i++) {
            if (!values.get(i).matches(patterns.get(i)))
                return false;
        }

        return true;
    }
}