package com.jonnymatts.jzonbie.requests;

import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

public class SparkRequest implements Request {

    private final String path;
    private final String method;
    private final Map<String, String> headers;
    private final String body;
    private final Map<String, List<String>> queryMap;

    public SparkRequest(spark.Request request) {
        path = request.pathInfo();
        method = request.requestMethod();
        headers = createHeaders(request);
        body = request.body();
        queryMap = createQueryMap(request);
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }

    @Override
    public String getBody() {
        return body;
    }

    @Override
    public Map<String, List<String>> getQueryParams() {
        return queryMap;
    }

    private Map<String, String> createHeaders(spark.Request request) {
        return request.headers().stream().collect(
                toMap(
                        identity(),
                        request::headers
                )
        );
    }

    private Map<String, List<String>> createQueryMap(spark.Request request) {
        return request.queryMap().toMap().entrySet()
                .stream()
                .collect(
                        toMap(
                                Map.Entry::getKey,
                                e -> asList(e.getValue())
                        )
                );
    }
}
