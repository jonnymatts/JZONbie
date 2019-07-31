package com.jonnymatts.jzonbie.requests;

import com.jonnymatts.jzonbie.body.ArrayBodyContent;
import com.jonnymatts.jzonbie.body.BodyContent;
import com.jonnymatts.jzonbie.body.LiteralBodyContent;
import com.jonnymatts.jzonbie.body.ObjectBodyContent;
import org.junit.jupiter.api.Test;

import static com.jonnymatts.jzonbie.body.StringBodyContent.stringBody;
import static com.jonnymatts.jzonbie.requests.AppRequest.request;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

class AppRequestBuildingTest {

    @Test
    void requestsCanBeBuilt() {
        final BodyContent body = stringBody("test");
        final AppRequest request = request("GET", "/.*")
                .withBody(body)
                .withBasicAuth("username", "password")
                .withHeader("header-name", "header-value")
                .withQueryParam("param-name", "param-value");

        assertThat(request.getMethod()).isEqualTo("GET");
        assertThat(request.getPath()).isEqualTo("/.*");
        assertThat(request.getQueryParams()).contains(entry("param-name", singletonList("param-value")));
        assertThat(request.getBody().getContent()).isEqualTo("test");
        assertThat(request.getHeaders()).contains(entry("header-name", "header-value"));
        assertThat(request.getHeaders()).containsKey("Authorization");
    }

    @Test
    void acceptAddsAcceptHeader() {
        final AppRequest response = request("GET", "/.*")
                .accept("header-value");

        assertThat(response.getHeaders()).contains(entry("Accept", "header-value"));
    }

    @Test
    void contentTypeAddsContentTypeHeader() {
        final AppRequest response = request("GET", "/.*")
                .contentType("header-value");

        assertThat(response.getHeaders()).contains(entry("Content-Type", "header-value"));
    }

    @Test
    void requestWithMapBodyReturnsRequestWithObjectBody() {
        final AppRequest request = request("GET", "/")
                .withBody(singletonMap("key", "val"));

        assertThat(request.getBody()).isInstanceOf(ObjectBodyContent.class);
        assertThat(request.getBody().getContent()).isEqualTo(singletonMap("key", "val"));
    }

    @Test
    void requestWithStringBodyReturnsRequestWithLiteralBody() {
        final AppRequest request = request("GET", "/")
                .withBody("literal");

        assertThat(request.getBody()).isInstanceOf(LiteralBodyContent.class);
        assertThat(request.getBody().getContent()).isEqualTo("literal");
    }

    @Test
    void requestWithListBodyReturnsRequestWithArrayBody() {
        final AppRequest request = request("GET", "/")
                .withBody(singletonList("val"));

        assertThat(request.getBody()).isInstanceOf(ArrayBodyContent.class);
        assertThat(request.getBody().getContent()).isEqualTo(singletonList("val"));
    }

    @Test
    void requestWithNumberBodyReturnsRequestWithLiteralBody() {
        final AppRequest request = request("GET", "/")
                .withBody(1);

        assertThat(request.getBody()).isInstanceOf(LiteralBodyContent.class);
        assertThat(request.getBody().getContent()).isEqualTo("1");
    }
}