package com.jonnymatts.jzonbie.model;

import com.jonnymatts.jzonbie.requests.Request;
import com.jonnymatts.jzonbie.util.Deserializer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppRequestFactory {

    private final Deserializer deserializer;

    public AppRequestFactory(Deserializer deserializer) {
        this.deserializer = deserializer;
    }

    public AppRequest create(Request request) {
        final Map<String, List<String>> queryParams = request.getQueryParams();

        final Map<String, Object> primedRequestMap = new HashMap<String, Object>(){{
            put("path", request.getPath());
            put("method", request.getMethod());
            put("body", deserializer.deserialize(request.getBody()));
            put("queryParams", (queryParams == null || queryParams.isEmpty()) ? null : queryParams);
            put("headers", request.getHeaders());
        }};

        return deserializer.deserialize(primedRequestMap, AppRequest.class);
    }
}
