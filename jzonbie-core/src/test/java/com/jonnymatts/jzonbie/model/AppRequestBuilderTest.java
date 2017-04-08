package com.jonnymatts.jzonbie.model;

import com.jonnymatts.jzonbie.model.content.ObjectBodyContent;
import org.junit.Test;

import static com.jonnymatts.jzonbie.model.content.LiteralBodyContent.literalBody;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

public class AppRequestBuilderTest {

    @Test
    public void builderCanConstructInstances() {
        final AppRequest request = AppRequest.builder("GET", "/.*")
                .withBody(singletonMap("key", "value"))
                .withBasicAuth("username", "password")
                .withHeader("header-name", "header-value")
                .withQueryParam("param-name", "param-value")
                .build();

        assertThat(request.getMethod()).isEqualTo("GET");
        assertThat(request.getPath()).isEqualTo("/.*");
        assertThat(request.getQueryParams()).contains(entry("param-name", singletonList("param-value")));
        assertThat(((ObjectBodyContent)request.getBody()).getContent()).contains(entry("key", "value"));
        assertThat(request.getHeaders()).contains(entry("header-name", "header-value"));
        assertThat(request.getHeaders()).containsKey("Authorization");
    }

    @Test
    public void acceptAddsAcceptHeader() throws Exception {
        final AppRequest response = AppRequest.builder("GET", "/.*")
                .accept("header-value")
                .build();

        assertThat(response.getHeaders()).contains(entry("Accept", "header-value"));
    }

    @Test
    public void contentTypeAddsContentTypeHeader() throws Exception {
        final AppRequest response = AppRequest.builder("GET", "/.*")
                .contentType("header-value")
                .build();

        assertThat(response.getHeaders()).contains(entry("Content-Type", "header-value"));
    }

    @Test
    public void builderCanBeReused() throws Exception {
        final AppRequestBuilder builder = AppRequest.builder("GET", "/");

        final AppRequest request1 = builder.withBody(1).build();
        final AppRequest request2 = builder.withBody(2).build();

        assertThat(request1.getBody()).isEqualTo(literalBody(1));
        assertThat(request2.getBody()).isEqualTo(literalBody(2));
    }
}