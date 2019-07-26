package com.jonnymatts.jzonbie.util;

import com.jonnymatts.jzonbie.requests.AppRequest;
import com.jonnymatts.jzonbie.responses.AppResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static com.jonnymatts.jzonbie.body.StringBodyContent.stringBody;
import static com.jonnymatts.jzonbie.requests.AppRequest.get;
import static com.jonnymatts.jzonbie.responses.AppResponse.ok;
import static com.jonnymatts.jzonbie.util.Cloner.cloneRequest;
import static com.jonnymatts.jzonbie.util.Cloner.cloneResponse;
import static org.assertj.core.api.Assertions.assertThat;

class ClonerTest {

    private AppRequest request;
    private AppResponse response;

    @BeforeEach
    void setUp() throws Exception {
        response = ok().withHeader("header", "value")
                .withDelay(Duration.ofSeconds(1))
                .withBody(stringBody("body"))
                .build();

        request = get("/")
                .withHeader("header", "value")
                .withBody(stringBody("body"))
                .withQueryParam("param", "value")
                .build();
    }

    @Test
    void cloneResponseClonesEveryFieldSuccessfully() {
        final AppResponse got = cloneResponse(response);

        assertThat(got).isEqualTo(response);
    }

    @Test
    void cloneRequestClonesEveryFieldSuccessfully() {
        final AppRequest got = cloneRequest(request);

        assertThat(got).isEqualTo(request);
    }

    @Test
    void cloneResponseClonesTemplatedField() {
        final AppResponse templatedResponse = ok().templated().build();

        final AppResponse got = cloneResponse(templatedResponse);

        assertThat(got).isEqualTo(templatedResponse);
    }
}