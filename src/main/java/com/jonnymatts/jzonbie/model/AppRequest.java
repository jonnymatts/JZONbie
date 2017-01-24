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
        this.basicAuth = basicAuth;
        basicAuth.entrySet().forEach(entry -> {
            final String authValue = format("%s:%s", entry.getKey(), entry.getValue());
            headers.put("Authorization", Base64.getEncoder().encodeToString(authValue.getBytes()));
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

    //    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//
//        ZombieRequest that = (ZombieRequest) o;
//
//        if (headers != null ? !headers.equals(that.headers) : that.headers != null) return false;
//        return body != null ? body.equals(that.body) : that.body == null;
//    }
//
//    @Override
//    public int hashCode() {
//        int result = headers != null ? headers.hashCode() : 0;
//        result = 31 * result + (body != null ? body.hashCode() : 0);
//        return result;
//    }


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
}
