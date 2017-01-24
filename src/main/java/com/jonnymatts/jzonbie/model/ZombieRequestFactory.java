package com.jonnymatts.jzonbie.model;

import com.jonnymatts.jzonbie.requests.Request;
import com.jonnymatts.jzonbie.util.Deserializer;

import java.util.HashMap;
import java.util.Map;

public class ZombieRequestFactory {

    private final Deserializer deserializer;

    public ZombieRequestFactory(Deserializer deserializer) {
        this.deserializer = deserializer;
    }

    public ZombieRequest create(Request request) {
        final Map<String, Object> primedRequestMap = new HashMap<String, Object>(){{
            put("path", request.getPath());
            put("method", request.getMethod());
            put("body", deserializer.deserialize(request.getBody()));
            put("queryParams", request.getQueryParams());
            put("headers", request.getHeaders());
        }};

        return deserializer.deserialize(primedRequestMap, ZombieRequest.class);
    }
}
