package com.jonnymatts.jzonbie.model;

import com.jonnymatts.jzonbie.util.Deserializer;
import spark.Request;

import java.util.HashMap;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

public class PrimedRequestFactory {

    private final Deserializer deserializer;

    public PrimedRequestFactory(Deserializer deserializer) {
        this.deserializer = deserializer;
    }

    public PrimedRequest create(Request request) {
        final Map<String, Object> primedRequestMap = new HashMap<String, Object>(){{
            put("path", request.pathInfo());
            put("method", request.requestMethod());
            put("body", deserializer.deserialize(request.body()));
            put("headers", request.headers().stream()
                    .collect(
                            toMap(
                                    h -> h,
                                    request::headers
                            )
                    ));
        }};

        return deserializer.deserialize(primedRequestMap, PrimedRequest.class);
    }
}
