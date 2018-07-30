package com.jonnymatts.jzonbie.model;

import com.flextrade.jfixture.rules.FixtureRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.time.Duration;

import static com.jonnymatts.jzonbie.model.Cloner.*;
import static com.jonnymatts.jzonbie.model.content.StringBodyContent.stringBody;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

public class ClonerTest {

    @Rule public FixtureRule fixtureRule = FixtureRule.initFixtures();

    private AppRequest request;
    private AppResponse response;

    @Before
    public void setUp() throws Exception {
        response = new AppResponse();
        response.setStatusCode(200);
        response.setHeaders(singletonMap("header", "value"));
        response.setDelay(Duration.ofSeconds(1));
        response.setBody(stringBody("body"));

        request = new AppRequest();
        request.setPath("/");
        request.setHeaders(singletonMap("header", "value"));
        request.setMethod("GET");
        request.setBody(stringBody("body"));
        request.setQueryParams(singletonMap("param", singletonList("value")));
    }

    @Test
    public void cloneResponseClonesEveryFieldSuccessfully() {
        final AppResponse got = cloneResponse(response);

        assertThat(got).isEqualTo(response);
    }

    @Test
    public void cloneRequestClonesEveryFieldSuccessfully() {
        final AppRequest got = cloneRequest(request);

        assertThat(got).isEqualTo(request);
    }


    @Test
    public void createTemplatedResponseClonesResponseSuccessfully() {
        final TemplatedAppResponse got = createTemplatedResponse(response);

        assertThat(got.getStatusCode()).isEqualTo(response.getStatusCode());
        assertThat(got.getBody()).isEqualTo(response.getBody());
        assertThat(got.getHeaders()).isEqualTo(response.getHeaders());
        assertThat(got.getDelay()).isEqualTo(response.getDelay());
    }
}