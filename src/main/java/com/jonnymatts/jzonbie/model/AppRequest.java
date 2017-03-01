package com.jonnymatts.jzonbie.model;

import com.google.common.collect.Sets;

import java.math.BigDecimal;
import java.util.*;

import static java.lang.String.format;
import static java.util.Collections.singletonMap;

public class AppRequest {

    private static final char[] REGEX_CHARACTERS = new char[]{'+', '.', '*', '[', '{', '^', '|', '$', '?'};

    private String path;
    private Map<String, String> headers;
    private String method;
    private Map<String, Object> body;
    private Map<String, String> basicAuth;
    private Map<String, List<String>> queryParams;

    AppRequest() {
        this.headers = new HashMap<>();
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

    public Map<String, Object> getBody() {
        return body;
    }

    void setBody(Map<String, ?> body) {
        this.body = body == null ? null : new HashMap<>(body);
    }

    public Map<String, String> getBasicAuth() {
        return basicAuth;
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
        if(queryParams != null ? !queryParametersMatchWithRegex(that.queryParams) : that.queryParams != null) return false;
        if(headers != null ? !headersAreContainedWithinOtherRequestsHeaders(that.headers) : that.headers != null) return false;

        return body != null ? mapValuesMatchWithRegex(body, that.body) : that.body == null;
    }

    @Override
    public String toString() {
        return "AppRequest{" +
                "path='" + path + '\'' +
                ", headers=" + headers +
                ", method='" + method + '\'' +
                ", body=" + body +
                ", basicAuth=" + basicAuth +
                ", queryParams=" + queryParams +
                '}';
    }

    private boolean headersAreContainedWithinOtherRequestsHeaders(Map<String, String> otherHeaders) {
        final Set<String> primedHeaderKeys = headers.keySet();
        final Set<String> otherHeaderKeys = otherHeaders.keySet();

        if(!otherHeaderKeys.containsAll(primedHeaderKeys)) return false;

        final HashMap<String, String> copy = new HashMap<>(otherHeaders);
        Sets.difference(otherHeaderKeys, primedHeaderKeys).forEach(copy::remove);

        return mapValuesMatchWithRegex(headers, copy);
    }

    private boolean queryParametersMatchWithRegex(Map<String, List<String>> otherQueryParams) {
        return queryParams.entrySet().parallelStream().allMatch(e -> {
            final List<String> otherValues = otherQueryParams.get(e.getKey());
            return otherValues != null && e.getValue() != null && listsMatchesRegex(e.getValue(), otherValues);
        });
    }

    private boolean mapValuesMatchWithRegex(Map<?, ?> patterns, Map<?, ?> values) {
        if(isNullOrEmpty(patterns) && isNullOrEmpty(values)) return true;

        if(!patterns.keySet().equals(values.keySet())) return false;

        return patterns.entrySet().parallelStream().allMatch(e -> {
            final Object pattern = e.getValue();
            final Object value = values.get(e.getKey());
            return matchRegexRecursively(pattern, value);
        });
    }

    private boolean isNullOrEmpty(Map<?,?> map) {
        return map == null || map.isEmpty();
    }

    private boolean listsMatchesRegex(List<?> patterns, List<?> values) {
        if (patterns.size() != values.size())
            return false;

        for(int i = 0; i < patterns.size(); i++) {
            final Object pattern = patterns.get(i);
            final Object value = values.get(i);
            if (!matchRegexRecursively(pattern, value))
                return false;
        }

        return true;
    }

    private boolean matchRegexRecursively(Object pattern, Object value) {
        if(value instanceof String)
            return stringsMatch((String)pattern, (String) value);
        if(value instanceof Map)
            return mapValuesMatchWithRegex((Map<?,?>) pattern, (Map<?,?>)value);
        if(value instanceof List)
            return listsMatchesRegex((List<?>) pattern, (List<?>)value);
        if(value instanceof Number)
            return numbersEqual((Number)pattern, (Number)value);
        return Objects.equals(pattern, value);
    }

    private boolean stringsMatch(String pattern, String value) {
        boolean isRegex = false;
        for(char c : REGEX_CHARACTERS) {
            if (pattern.indexOf(c) > -1) {
                isRegex = true;
                break;
            }
        }
        
        return isRegex ? value.matches(pattern) : value.equals(pattern);
    }

    private boolean numbersEqual(Number number1, Number number2) {
        final BigDecimal bigDecimal1 = new BigDecimal(number1.toString());
        final BigDecimal bigDecimal2 = new BigDecimal(number2.toString());
        return bigDecimal1.compareTo(bigDecimal2) == 0;
    }
}