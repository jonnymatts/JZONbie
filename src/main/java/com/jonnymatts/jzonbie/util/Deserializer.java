package com.jonnymatts.jzonbie.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import spark.Request;

import java.io.IOException;
import java.util.Map;

public class Deserializer {

    private final ObjectMapper objectMapper;

    public Deserializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public <T> T deserialize(Request request, Class<T> clazz) {
        try {
            return objectMapper.readValue(request.body(), clazz);
        } catch (IOException e) {
            throw new DeserializationException(String.format("Error deserializing %s", clazz.getSimpleName()), e);
        }
    }

    public <T> T deserialize(Map<String, Object> map, Class<T> clazz) {
        try {
            return objectMapper.convertValue(map, clazz);
        } catch (Exception e) {
            throw new DeserializationException(String.format("Error deserializing %s", clazz.getSimpleName()), e);
        }
    }


    public Map<String, Object> deserialize(String s) {
        try {
            return objectMapper.readValue(s, new TypeReference<Map<String, Object>>() {});
        } catch (IOException e) {
            throw new DeserializationException("Error deserializing to map", e);
        }
    }
}