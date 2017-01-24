package com.jonnymatts.jzonbie.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jonnymatts.jzonbie.requests.Request;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public class Deserializer {

    private final ObjectMapper objectMapper;

    public Deserializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public <T> T deserialize(Request request, Class<T> clazz) {
        try {
            return objectMapper.readValue(request.getBody(), clazz);
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
        if(s == null || s.isEmpty()) return null;

        try {
            return objectMapper.readValue(s, new TypeReference<Map<String, Object>>() {});
        } catch (IOException e) {
            throw new DeserializationException("Error deserializing to map", e);
        }
    }

    public <T> T deserialize(HttpResponse response, Class<T> clazz) {
        try {
            final String s = EntityUtils.toString(response.getEntity());
            return objectMapper.readValue(s, clazz);
        } catch (IOException e) {
            throw new DeserializationException("Error deserializing http response", e);
        }
    }

    public <T> List<T> deserializeCollection(HttpResponse response, Class<T> clazz) {
        try {
            final String s = EntityUtils.toString(response.getEntity());
            final List<Map<String, Object>> maps = objectMapper.readValue(s, new TypeReference<List<T>>() {});
            return maps.stream().map(t -> deserialize(t, clazz)).collect(toList());
        } catch (IOException e) {
            throw new DeserializationException("Error deserializing http response", e);
        }
    }
}