package com.jonnymatts.jzonbie;

import java.util.Base64;
import java.util.Map;

import static java.lang.String.format;

public class PrimedRequest {

    private Map<String, String> headers;
    private Map<String, Object> body;
    private Map<String, String> basicAuth;

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
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

//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//
//        PrimedRequest that = (PrimedRequest) o;
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

        PrimedRequest that = (PrimedRequest) o;

        return body != null ? body.equals(that.body) : that.body == null;
    }

    @Override
    public int hashCode() {
        return body != null ? body.hashCode() : 0;
    }
}
