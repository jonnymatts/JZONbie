package com.jonnymatts.jzonbie.spark;

import com.fasterxml.jackson.databind.ObjectMapper;
import spark.ResponseTransformer;

public class JsonResponseTransformer implements ResponseTransformer {

    private final ObjectMapper objectMapper;

    public JsonResponseTransformer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String render(Object model) throws Exception {
        return objectMapper.writeValueAsString(model);
    }
}
