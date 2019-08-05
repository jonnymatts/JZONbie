package com.jonnymatts.jzonbie.requests;

import com.google.common.collect.Sets;
import com.jonnymatts.jzonbie.body.*;

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

/**
 * Defines a request Jzonbie will match against when an incoming request is received.
 * <p>
 * A request must be configured with a HTTP method and a path. Static factory methods
 * are provided for the most common HTTP methods. The request headers, body, query params,
 * and basic authentication can also be configured.
 * <p>
 * When creating requests, it is recommended to use the builder "withX" methods.
 * <p>
 * {@code
 * final AppRequest request = get("/api/data")
 *                 .accept("application/json")
 *                 .withBasicAuth("username", "password")
 *                 .withHeader("Trace-Id", "trace-.*");
 * }
 * <p>
 * When matching against the request, the path, header values, query param values are
 * treated as regex patterns. Request bodies are matched according to {@link BodyContentType}.
 */
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

    /**
     * Creates a new {@code AppRequest} with the given HTTP method and path pattern.
     * <p>
     * It is recommended to use the provided static factory methods.
     * <p>
     * eg. {@link #get(String path)}, {@link #post(String path)}, {@link #put(String path)}, etc.
     *
     * @param method HTTP method
     * @param path path pattern
     */
    public AppRequest(String method, String path) {
        this.method = method;
        this.path = path;
        this.headers = new HashMap<>();
        this.queryParams = new HashMap<>();
    }

    /**
     * Creates a new {@code AppRequest} that copies all fields of the input {@code AppRequest}.
     *
     * @param request request to copy
     */
    public AppRequest(AppRequest request) {
        this(request.method, request.path);
        setPath(request.getPath());
        setMethod(request.getMethod());
        setQueryParams(copyMap(request.getQueryParams()));
        setHeaders(copyMap(request.getHeaders()));
        setBody(copyBodyContent(request.getBody()));
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

    /**
     * Adds the header to this request.
     *
     * @param name header name
     * @param value header value
     * @return this request with the added header
     */
    public AppRequest withHeader(String name, String value) {
        if(this.getHeaders() == null)
            this.setHeaders(new HashMap<>());
        this.getHeaders().put(name, value);
        return this;
    }

    /**
     * Configures this request with the given {@link BodyContent} body.
     *
     * @param body body content
     * @return this request with a body
     */
    public AppRequest withBody(BodyContent<?> body) {
        this.setBody(body);
        return this;
    }

    /**
     * Configures this request with an {@link ObjectBodyContent} body.
     *
     * @param body object body
     * @return this request with a body
     */
    public AppRequest withBody(Map<String, ?> body) {
        this.setBody(objectBody(body));
        return this;
    }

    /**
     * Configures this request with a {@link LiteralBodyContent} body.
     *
     * @param body string body
     * @return this request with a body
     */
    public AppRequest withBody(String body) {
        this.setBody(literalBody(body));
        return this;
    }

    /**
     * Configures this request with an {@link ArrayBodyContent} body.
     *
     * @param body list body
     * @return this request with a body
     */
    public AppRequest withBody(List<?> body) {
        this.setBody(arrayBody(body));
        return this;
    }

    /**
     * Configures this request with a {@link BigDecimal} {@link LiteralBodyContent} body.
     *
     * @param body number body
     * @return this request with a body
     */
    public AppRequest withBody(Number body) {
        this.setBody(literalBody(new BigDecimal(body.doubleValue())));
        return this;
    }

    /**
     * Adds the query param to this request.
     *
     * @param name param name
     * @param value param value
     * @return this request with the added query param
     */
    public AppRequest withQueryParam(String name, String value) {
        Map<String, List<String>> queryParams = this.getQueryParams();
        if (queryParams == null) {
            queryParams = new HashMap<>();
            this.setQueryParams(queryParams);
        }

        if (!queryParams.containsKey(name)) {
            queryParams.put(name, new ArrayList<>());
        }

        queryParams.get(name).add(value);

        return this;
    }

    /**
     * Adds the "Authorization" header with the Basic Authentication value.
     * <p>
     * Header value is "Basic {encodedValue}" where {@code encodedValue} is "{username}:{password}" base-64 encoded.
     *
     * @param username basic authentication username
     * @param password basic authentication password
     * @return this request with the authorization header configured for Basic Authentication
     */
    public AppRequest withBasicAuth(String username, String password) {
        this.setBasicAuth(username, password);
        return this;
    }

    /**
     * Sets the "Accept" header of this request.
     *
     * @param contentType accepted content type
     * @return this request with an accept header
     */
    public AppRequest accept(String contentType) {
        return withHeader("Accept", contentType);
    }

    /**
     * Sets the "Content-Type" header of this request.
     *
     * @param contentType content type header value
     * @return this request with a content type header
     */
    public AppRequest contentType(String contentType) {
        return withHeader("Content-Type", contentType);
    }

    /**
     * Returns a request with the given HTTP method and path pattern.
     *
     * @param method HTTP method of the request
     * @param path Path pattern of the request
     * @return reuqest with HTTP method and path
     */
    public static AppRequest request(String method, String path) {
        return new AppRequest(method, path);
    }

    /**
     * Returns a request with the GET HTTP method at the given path.
     *
     * @param path path
     * @return GET request at path
     */
    public static AppRequest get(String path) {
        return new AppRequest("GET", path);
    }

    /**
     * Returns a request with the POST HTTP method at the given path.
     *
     * @param path path
     * @return POST request at path
     */
    public static AppRequest post(String path) {
        return new AppRequest("POST", path);
    }

    /**
     * Returns a request with the HEAD HTTP method at the given path.
     *
     * @param path path
     * @return HEAD request at path
     */
    public static AppRequest head(String path) {
        return new AppRequest("HEAD", path);
    }

    /**
     * Returns a request with the PUT HTTP method at the given path.
     *
     * @param path path
     * @return PUT request at path
     */
    public static AppRequest put(String path) {
        return new AppRequest("PUT", path);
    }

    /**
     * Returns a request with the OPTIONS HTTP method at the given path.
     *
     * @param path path
     * @return OPTIONS request at path
     */
    public static AppRequest options(String path) {
        return new AppRequest("OPTIONS", path);
    }

    /**
     * Returns a request with the DELETE HTTP method at the given path.
     *
     * @param path path
     * @return DELETE request at path
     */
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