package com.jonnymatts.jzonbie.requests;

import com.jonnymatts.jzonbie.body.ArrayBodyContent;
import com.jonnymatts.jzonbie.body.BodyContent;
import com.jonnymatts.jzonbie.body.LiteralBodyContent;
import com.jonnymatts.jzonbie.body.ObjectBodyContent;
import org.junit.jupiter.api.Test;

import static com.jonnymatts.jzonbie.body.StringBodyContent.stringBody;
import static com.jonnymatts.jzonbie.requests.AppRequest.builder;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

class AppRequestBuilderTest {

    @Test
    void builderCanConstructInstances() {
        final BodyContent body = stringBody("test");
        final AppRequest request = builder("GET", "/.*")
                .withBody(body)
                .withBasicAuth("username", "password")
                .withHeader("header-name", "header-value")
                .withQueryParam("param-name", "param-value")
                .build();

        assertThat(request.getMethod()).isEqualTo("GET");
        assertThat(request.getPath()).isEqualTo("/.*");
        assertThat(request.getQueryParams()).contains(entry("param-name", singletonList("param-value")));
        assertThat(request.getBody().getContent()).isEqualTo("test");
        assertThat(request.getHeaders()).contains(entry("header-name", "header-value"));
        assertThat(request.getHeaders()).containsKey("Authorization");
    }

    @Test
    void acceptAddsAcceptHeader() {
        final AppRequest response = builder("GET", "/.*")
                .accept("header-value")
                .build();

        assertThat(response.getHeaders()).contains(entry("Accept", "header-value"));
    }

    @Test
    void contentTypeAddsContentTypeHeader() {
        final AppRequest response = builder("GET", "/.*")
                .contentType("header-value")
                .build();

        assertThat(response.getHeaders()).contains(entry("Content-Type", "header-value"));
    }

    @Test
    void builderCanBeReused() {
        final AppRequestBuilder builder = builder("GET", "/");

        final AppRequest request1 = builder.withBody(stringBody("test1")).build();
        final AppRequest request2 = builder.withBody(stringBody("test2")).build();

        assertThat(request1.getBody().getContent()).isEqualTo("test1");
        assertThat(request2.getBody().getContent()).isEqualTo("test2");
    }

    @Test
    void builderWithMapBodyReturnsRequestWithObjectBody() {
        final AppRequest request = builder("GET", "/")
                .withBody(singletonMap("key", "val"))
                .build();

        assertThat(request.getBody()).isInstanceOf(ObjectBodyContent.class);
        assertThat(request.getBody().getContent()).isEqualTo(singletonMap("key", "val"));
    }

    @Test
    void builderWithStringBodyReturnsRequestWithLiteralBody() {
        final AppRequest request = builder("GET", "/")
                .withBody("literal")
                .build();

        assertThat(request.getBody()).isInstanceOf(LiteralBodyContent.class);
        assertThat(request.getBody().getContent()).isEqualTo("literal");
    }

    @Test
    void builderWithListBodyReturnsRequestWithArrayBody() {
        final AppRequest request = builder("GET", "/")
                .withBody(singletonList("val"))
                .build();

        assertThat(request.getBody()).isInstanceOf(ArrayBodyContent.class);
        assertThat(request.getBody().getContent()).isEqualTo(singletonList("val"));
    }

    @Test
    void builderWithNumberBodyReturnsRequestWithLiteralBody() {
        final AppRequest request = builder("GET", "/")
                .withBody(1)
                .build();

        assertThat(request.getBody()).isInstanceOf(LiteralBodyContent.class);
        assertThat(request.getBody().getContent()).isEqualTo("1");
    }
}