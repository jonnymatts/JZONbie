package com.jonnymatts.jzonbie.responses;

import com.jonnymatts.jzonbie.testing.StringBody;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

public class AppResponseBuilderTest {

    @Test
    public void builderCanConstructInstances() {
        final AppResponse response = AppResponse.builder(200)
                .withBody(new StringBody("test"))
                .withHeader("header-name", "header-value")
                .build();

        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getBody().getContent()).isEqualTo("test");
        assertThat(response.getHeaders()).contains(entry("header-name", "header-value"));
        assertThat(response.isTemplated()).isFalse();
    }

    @Test
    public void builderCanConstructTemplatedInstances() {
        final AppResponse response = AppResponse.builder(200)
                .templated()
                .build();

        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.isTemplated()).isTrue();
    }

    @Test
    public void contentTypeAddsContentTypeHeader() throws Exception {
        final AppResponse response = AppResponse.builder(200)
                .contentType("header-value")
                .build();

        assertThat(response.getHeaders()).contains(entry("Content-Type", "header-value"));
    }

    @Test
    public void builderCanBeReused() throws Exception {
        final AppResponseBuilder builder = AppResponse.builder(201);

        final AppResponse response1 = builder.withBody(new StringBody("test1")).build();
        final AppResponse response2 = builder.withBody(new StringBody("test2")).build();

        assertThat(response1.getBody().getContent()).isEqualTo("test1");
        assertThat(response2.getBody().getContent()).isEqualTo("test2");
    }
}