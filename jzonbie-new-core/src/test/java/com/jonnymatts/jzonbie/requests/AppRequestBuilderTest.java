package com.jonnymatts.jzonbie.requests;

import com.jonnymatts.jzonbie.testing.StringBody;
import org.junit.Test;

import static com.jonnymatts.jzonbie.requests.AppRequest.builder;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

public class AppRequestBuilderTest {

    @Test
    public void builderCanConstructInstances() {
        final StringBody body = new StringBody("test");
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
    public void acceptAddsAcceptHeader() {
        final AppRequest response = builder("GET", "/.*")
                .accept("header-value")
                .build();

        assertThat(response.getHeaders()).contains(entry("Accept", "header-value"));
    }

    @Test
    public void contentTypeAddsContentTypeHeader() {
        final AppRequest response = builder("GET", "/.*")
                .contentType("header-value")
                .build();

        assertThat(response.getHeaders()).contains(entry("Content-Type", "header-value"));
    }

    @Test
    public void builderCanBeReused() {
        final AppRequestBuilder builder = builder("GET", "/");

        final AppRequest request1 = builder.withBody(new StringBody("test1")).build();
        final AppRequest request2 = builder.withBody(new StringBody("test2")).build();

        assertThat(request1.getBody().getContent()).isEqualTo("test1");
        assertThat(request2.getBody().getContent()).isEqualTo("test2");
    }


}