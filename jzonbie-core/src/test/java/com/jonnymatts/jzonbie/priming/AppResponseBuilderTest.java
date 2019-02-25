package com.jonnymatts.jzonbie.priming;

import com.jonnymatts.jzonbie.priming.content.ObjectBodyContent;
import org.junit.Test;

import static com.jonnymatts.jzonbie.priming.content.LiteralBodyContent.literalBody;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

public class AppResponseBuilderTest {

    @Test
    public void builderCanConstructInstances() {
        final AppResponse response = AppResponse.builder(200)
                .withBody(singletonMap("key", "value"))
                .withHeader("header-name", "header-value")
                .build();

        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(((ObjectBodyContent)response.getBody()).getContent()).contains(entry("key", "value"));
        assertThat(response.getHeaders()).contains(entry("header-name", "header-value"));
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

        final AppResponse response1 = builder.withBody(1).build();
        final AppResponse response2 = builder.withBody(2).build();

        assertThat(response1.getBody()).isEqualTo(literalBody(1));
        assertThat(response2.getBody()).isEqualTo(literalBody(2));
    }
}