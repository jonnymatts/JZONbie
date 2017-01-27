package com.jonnymatts.jzonbie.model;

import org.junit.Test;

import static java.util.Collections.singletonMap;
import static org.apache.http.HttpStatus.SC_OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

public class AppResponseBuilderTest {

    @Test
    public void builderCanConstructInstances() {
        final AppResponse response = AppResponse.builder(SC_OK)
                .withBody(singletonMap("key", "value"))
                .withHeader("header-name", "header-value")
                .build();

        assertThat(response.getStatusCode()).isEqualTo(SC_OK);
        assertThat(response.getBody()).contains(entry("key", "value"));
        assertThat(response.getHeaders()).contains(entry("header-name", "header-value"));
    }
}