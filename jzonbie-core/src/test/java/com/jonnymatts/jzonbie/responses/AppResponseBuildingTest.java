package com.jonnymatts.jzonbie.responses;

import com.jonnymatts.jzonbie.body.ArrayBodyContent;
import com.jonnymatts.jzonbie.body.LiteralBodyContent;
import com.jonnymatts.jzonbie.body.ObjectBodyContent;
import org.junit.jupiter.api.Test;

import static com.jonnymatts.jzonbie.body.StringBodyContent.stringBody;
import static com.jonnymatts.jzonbie.responses.AppResponse.ok;
import static com.jonnymatts.jzonbie.responses.AppResponse.response;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

class AppResponseBuildingTest {

    @Test
    void responsesCanBeBuilt() {
        final AppResponse response = response(200)
                .withBody(stringBody("test"))
                .withHeader("header-name", "header-value");

        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getBody().getContent()).isEqualTo("test");
        assertThat(response.getHeaders()).contains(entry("header-name", "header-value"));
        assertThat(response.isTemplated()).isFalse();
    }

    @Test
    void templatedResponsesCanBeBuilt() {
        final AppResponse response = response(200)
                .templated();

        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.isTemplated()).isTrue();
    }

    @Test
    void contentTypeAddsContentTypeHeader() throws Exception {
        final AppResponse response = response(200)
                .contentType("header-value");

        assertThat(response.getHeaders()).contains(entry("Content-Type", "header-value"));
    }

    @Test
    void responsesAreImmutable() throws Exception {
        final AppResponse response = response(201);

        final AppResponse response1 = response.withBody(stringBody("test1"));
        final AppResponse response2 = response.withBody(stringBody("test2"));

        assertThat(response1.getBody().getContent()).isEqualTo("test1");
        assertThat(response2.getBody().getContent()).isEqualTo("test2");
    }

    @Test
    void responseWithMapBodyReturnsResponseWithObjectBody() {
        final AppResponse request = ok()
                .withBody(singletonMap("key", "val"));

        assertThat(request.getBody()).isInstanceOf(ObjectBodyContent.class);
        assertThat(request.getBody().getContent()).isEqualTo(singletonMap("key", "val"));
    }

    @Test
    void responseWithStringBodyReturnsResponseWithLiteralBody() {
        final AppResponse request = ok()
                .withBody("literal");

        assertThat(request.getBody()).isInstanceOf(LiteralBodyContent.class);
        assertThat(request.getBody().getContent()).isEqualTo("literal");
    }

    @Test
    void responseWithListBodyReturnsResponseWithArrayBody() {
        final AppResponse request = ok()
                .withBody(singletonList("val"));

        assertThat(request.getBody()).isInstanceOf(ArrayBodyContent.class);
        assertThat(request.getBody().getContent()).isEqualTo(singletonList("val"));
    }

    @Test
    void responseWithNumberBodyReturnsResponseWithLiteralBody() {
        final AppResponse request = ok()
                .withBody(1);

        assertThat(request.getBody()).isInstanceOf(LiteralBodyContent.class);
        assertThat(request.getBody().getContent()).isEqualTo("1");
    }
}