package com.jonnymatts.jzonbie.responses;

import com.jonnymatts.jzonbie.body.ArrayBodyContent;
import com.jonnymatts.jzonbie.body.LiteralBodyContent;
import com.jonnymatts.jzonbie.body.ObjectBodyContent;
import org.junit.Test;

import static com.jonnymatts.jzonbie.body.StringBodyContent.stringBody;
import static com.jonnymatts.jzonbie.responses.AppResponse.builder;
import static com.jonnymatts.jzonbie.responses.AppResponse.ok;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

public class AppResponseBuilderTest {

    @Test
    public void builderCanConstructInstances() {
        final AppResponse response = builder(200)
                .withBody(stringBody("test"))
                .withHeader("header-name", "header-value")
                .build();

        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getBody().getContent()).isEqualTo("test");
        assertThat(response.getHeaders()).contains(entry("header-name", "header-value"));
        assertThat(response.isTemplated()).isFalse();
    }

    @Test
    public void builderCanConstructTemplatedInstances() {
        final AppResponse response = builder(200)
                .templated()
                .build();

        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.isTemplated()).isTrue();
    }

    @Test
    public void contentTypeAddsContentTypeHeader() throws Exception {
        final AppResponse response = builder(200)
                .contentType("header-value")
                .build();

        assertThat(response.getHeaders()).contains(entry("Content-Type", "header-value"));
    }

    @Test
    public void builderCanBeReused() throws Exception {
        final AppResponseBuilder builder = builder(201);

        final AppResponse response1 = builder.withBody(stringBody("test1")).build();
        final AppResponse response2 = builder.withBody(stringBody("test2")).build();

        assertThat(response1.getBody().getContent()).isEqualTo("test1");
        assertThat(response2.getBody().getContent()).isEqualTo("test2");
    }

    @Test
    public void builderWithMapBodyReturnsResponseWithObjectBody() {
        final AppResponse request = ok()
                .withBody(singletonMap("key", "val"))
                .build();

        assertThat(request.getBody()).isInstanceOf(ObjectBodyContent.class);
        assertThat(request.getBody().getContent()).isEqualTo(singletonMap("key", "val"));
    }

    @Test
    public void builderWithStringBodyReturnsResponseWithLiteralBody() {
        final AppResponse request = ok()
                .withBody("literal")
                .build();

        assertThat(request.getBody()).isInstanceOf(LiteralBodyContent.class);
        assertThat(request.getBody().getContent()).isEqualTo("literal");
    }

    @Test
    public void builderWithListBodyReturnsResponseWithArrayBody() {
        final AppResponse request = ok()
                .withBody(singletonList("val"))
                .build();

        assertThat(request.getBody()).isInstanceOf(ArrayBodyContent.class);
        assertThat(request.getBody().getContent()).isEqualTo(singletonList("val"));
    }

    @Test
    public void builderWithNumberBodyReturnsResponseWithLiteralBody() {
        final AppResponse request = ok()
                .withBody(1)
                .build();

        assertThat(request.getBody()).isInstanceOf(LiteralBodyContent.class);
        assertThat(request.getBody().getContent()).isEqualTo("1");
    }
}