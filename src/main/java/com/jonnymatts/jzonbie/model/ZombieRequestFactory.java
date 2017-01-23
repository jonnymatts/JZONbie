package com.jonnymatts.jzonbie.model;

import com.jonnymatts.jzonbie.util.Deserializer;
import spark.Request;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toMap;

public class ZombieRequestFactory {

    private final Deserializer deserializer;

    public ZombieRequestFactory(Deserializer deserializer) {
        this.deserializer = deserializer;
    }

    public ZombieRequest create(Request request) {
        final Map<String, List<String>> queryParams = request.queryMap().toMap().entrySet()
                .stream()
                .collect(
                        toMap(
                                Map.Entry::getKey,
                                e -> asList(e.getValue())
                        )
                );

        final Map<String, Object> primedRequestMap = new HashMap<String, Object>(){{
            put("path", request.pathInfo());
            put("method", request.requestMethod());
            put("body", deserializer.deserialize(request.body()));
            put("queryParams", queryParams);
            put("headers", request.headers().stream()
                    .collect(
                            toMap(
                                    h -> h,
                                    request::headers
                            )
                    ));
        }};

        return deserializer.deserialize(primedRequestMap, ZombieRequest.class);
    }
}
