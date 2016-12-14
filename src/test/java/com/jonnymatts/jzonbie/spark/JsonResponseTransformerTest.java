package com.jonnymatts.jzonbie.spark;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JsonResponseTransformerTest {

    private static final String STRING_VALUE = "stringValue";

    @Mock
    private ObjectMapper objectMapper;

    private JsonResponseTransformer transformer;

    @Before
    public void setUp() throws Exception {
        transformer = new JsonResponseTransformer(objectMapper);
    }

    @Test
    public void renderReturnsStringRepresentationOfObjectFromObjectMapper() throws Exception {
        final Object o = new Object();

        when(objectMapper.writeValueAsString(o)).thenReturn(STRING_VALUE);

        final String got = transformer.render(o);

        assertThat(got).isEqualTo(STRING_VALUE);
    }
}