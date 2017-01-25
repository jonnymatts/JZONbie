package com.jonnymatts.jzonbie.pippo;

import com.jonnymatts.jzonbie.requests.Request;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static java.util.Collections.list;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

public class PippoRequest implements Request {

    private final String path;
    private final String method;
    private final Map<String, String> headers;
    private final String body;
    private final Map<String, List<String>> queryMap;

    public PippoRequest(ro.pippo.core.Request request) {
        path = request.getPath();
        method = request.getMethod();
        headers = createHeaders(request);
        body = request.getBody();
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

    private Map<String,String> createHeaders(ro.pippo.core.Request request) {
        return list(request.getHttpServletRequest().getHeaderNames())
                .stream()
                .collect(
                        toMap(
                                identity(),
                                request::getHeader
                        )
                );
    }

    private Map<String,List<String>> createQueryMap(ro.pippo.core.Request request) {
        return request.getQueryParameters().entrySet()
                .stream()
                .collect(
                        toMap(
                                Entry::getKey,
                                e -> e.getValue().toList()
                        )
                );
    }
}