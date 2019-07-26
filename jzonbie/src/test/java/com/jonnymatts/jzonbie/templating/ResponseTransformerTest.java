package com.jonnymatts.jzonbie.templating;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ResponseTransformerTest {

    @Mock(answer = RETURNS_DEEP_STUBS) private TransformationContext transformationContext;

    private ResponseTransformer underTest;

    @Before
    public void setUp() throws Exception {
        when(transformationContext.getRequest().getUrl()).thenReturn("url");
        when(transformationContext.getRequest().getMethod()).thenReturn("method");

        underTest = new ResponseTransformer(new JzonbieHandlebars());
    }

    @Test
    public void transformHeadersReturnsTransformedBody() {
        final Map<String, String> got = underTest.transformHeaders(transformationContext, singletonMap("header", "{{ request.method }}"));

        assertThat(got).containsExactly(
                entry("header", "method")
        );
    }

    @Test
    public void transformHeadersThrowsExceptionIfHandlebarsThrowsException() {
        assertThatThrownBy(() -> underTest.transformHeaders(transformationContext, singletonMap("header", null)))
                .isInstanceOf(TransformResponseException.class);
    }

    @Test
    public void transformBodyReturnsTransformedBody() {
        final String got = underTest.transformBody(transformationContext, "{{ request.url }}");

        assertThat(got).isEqualTo("url");
    }

    @Test
    public void transformBodyThrowsExceptionIfHandlebarsThrowsException() {
        assertThatThrownBy(() -> underTest.transformBody(transformationContext, null))
                .isInstanceOf(TransformResponseException.class);
    }
}