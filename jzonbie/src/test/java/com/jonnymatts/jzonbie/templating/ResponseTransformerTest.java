package com.jonnymatts.jzonbie.templating;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jonnymatts.jzonbie.Response;
import com.jonnymatts.jzonbie.jackson.JzonbieObjectMapper;
import com.jonnymatts.jzonbie.metadata.MetaDataContext;
import com.jonnymatts.jzonbie.responses.AppResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class ResponseTransformerTest {

    @Mock(answer = RETURNS_DEEP_STUBS) private MetaDataContext metaDataContext;
    @Mock private MetaDataContext.RequestContext requestContext;

    private ResponseTransformer underTest;

    @BeforeEach
    void setUp() throws Exception {
        lenient().when(metaDataContext.getContext().get("request")).thenReturn(requestContext);
        lenient().when(requestContext.getMethod()).thenReturn("method");
        lenient().when(requestContext.getUrl()).thenReturn("url");

        underTest = new ResponseTransformer(new JzonbieObjectMapper(), new JzonbieHandlebars());
    }

    @Test
    void doNothingIfNotTemplated() throws JsonProcessingException {
        AppResponse appResponse = new AppResponse(200);
        appResponse.withHeader("header", "{{ request.method }}");
        appResponse.withBody("");
        Response got = underTest.transform(metaDataContext, appResponse);
        assertThat(got.getHeaders()).containsExactly(entry("header", "{{ request.method }}"));
    }

    @Test
    void templatedHeadersReturnsTransformedHeaders() throws JsonProcessingException {
        AppResponse appResponse = new AppResponse(200);
        appResponse.withHeader("header", "{{ request.method }}");
        appResponse.withBody("");
        appResponse.templated();
        Response got = underTest.transform(metaDataContext, appResponse);
        assertThat(got.getHeaders()).containsExactly(entry("header", "method"));
    }

    @Test
    void transformHeadersThrowsExceptionIfHandlebarsThrowsException() {
        AppResponse appResponse = new AppResponse(200);
        appResponse.templated();
        appResponse.withHeader("header", null);
        assertThatThrownBy(() -> underTest.transform(metaDataContext, appResponse))
                .isInstanceOf(TransformResponseException.class);
    }

    @Test
    void transformBodyReturnsTransformedBody() throws JsonProcessingException {
        AppResponse appResponse = new AppResponse(200);
        appResponse.templated();
        appResponse.withBody("{{ request.url }}");
        final Response got = underTest.transform(metaDataContext, appResponse);

        assertThat(((String)got.getBody().getContent())).isEqualTo("url");
    }

    @Test
    void transformBodyThrowsExceptionIfHandlebarsThrowsException() {
        AppResponse appResponse = new AppResponse(200);
        appResponse.templated();
        assertThatThrownBy(() -> underTest.transform(metaDataContext, appResponse))
                .isInstanceOf(TransformResponseException.class);
    }
}