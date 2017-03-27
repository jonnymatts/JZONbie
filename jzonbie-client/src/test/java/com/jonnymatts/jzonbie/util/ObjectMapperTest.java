package com.jonnymatts.jzonbie.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.util.Map;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class ObjectMapperTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void readValueForMapOfStringToObjectReturnsCorrectMapIfStringContainsValidJson() throws Exception {
        final String json = "{\"blah\": \"foo\", \"blah2\": [1,2,3]}";

        final Map<String, Object> got = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});

        assertThat(got).containsKeys("blah", "blah2");
        assertThat(got.get("blah")).isEqualTo("foo");
        assertThat(got.get("blah2")).isEqualTo(asList(1,2,3));
    }
}
