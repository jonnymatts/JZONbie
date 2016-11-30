package com.jonnymatts.jzonbie;

import spark.Request;

import java.util.HashMap;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

public class PrimedRequestFactory {

    private final JsonDeserializer jsonDeserializer;

    public PrimedRequestFactory(JsonDeserializer jsonDeserializer) {
        this.jsonDeserializer = jsonDeserializer;
    }

    public PrimedRequest create(Request request) {
        final Map<String, Object> primedRequestMap = new HashMap<String, Object>(){{
            put("path", request.pathInfo());
            put("method", request.requestMethod());
            put("body", jsonDeserializer.deserialize(request.body()));
            put("headers", request.headers().stream()
                    .collect(
                            toMap(
                                    h -> h,
                                    request::headers
                            )
                    ));
        }};

        return jsonDeserializer.deserialize(primedRequestMap, PrimedRequest.class);
    }
}
