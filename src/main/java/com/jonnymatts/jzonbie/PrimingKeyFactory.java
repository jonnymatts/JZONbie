package com.jonnymatts.jzonbie;

import spark.Request;

import java.util.HashMap;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

public class PrimingKeyFactory {

    private final JsonDeserializer jsonDeserializer;

    public PrimingKeyFactory(JsonDeserializer jsonDeserializer) {
        this.jsonDeserializer = jsonDeserializer;
    }

    public PrimingKey create(Request request) {
        final Map<String, Object> primedRequestMap = new HashMap<String, Object>(){{
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

        final PrimedRequest primedRequest = jsonDeserializer.deserialize(primedRequestMap, PrimedRequest.class);

        return new PrimingKey(request.pathInfo(), primedRequest);
    }
}
