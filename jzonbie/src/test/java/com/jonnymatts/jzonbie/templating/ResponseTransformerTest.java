package com.jonnymatts.jzonbie.templating;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class ResponseTransformerTest {

    @Mock(answer = RETURNS_DEEP_STUBS) private TransformationContext transformationContext;

    private ResponseTransformer underTest;

    @BeforeEach
    void setUp() throws Exception {
        lenient().when(transformationContext.getRequest().getUrl()).thenReturn("url");
        lenient().when(transformationContext.getRequest().getMethod()).thenReturn("method");

        underTest = new ResponseTransformer(new JzonbieHandlebars());
    }

    @Test
    void transformHeadersReturnsTransformedBody() {
        final Map<String, String> got = underTest.transformHeaders(transformationContext, singletonMap("header", "{{ request.method }}"));

        assertThat(got).containsExactly(
                entry("header", "method")
        );
    }

    @Test
    void transformHeadersThrowsExceptionIfHandlebarsThrowsException() {
        assertThatThrownBy(() -> underTest.transformHeaders(transformationContext, singletonMap("header", null)))
                .isInstanceOf(TransformResponseException.class);
    }

    @Test
    void transformBodyReturnsTransformedBody() {
        final String got = underTest.transformBody(transformationContext, "{{ request.url }}");

        assertThat(got).isEqualTo("url");
    }

    @Test
    void transformBodyThrowsExceptionIfHandlebarsThrowsException() {
        assertThatThrownBy(() -> underTest.transformBody(transformationContext, null))
                .isInstanceOf(TransformResponseException.class);
    }
}