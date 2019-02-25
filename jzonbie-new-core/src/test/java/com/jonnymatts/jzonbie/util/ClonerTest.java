package com.jonnymatts.jzonbie.util;

import com.flextrade.jfixture.rules.FixtureRule;
import com.jonnymatts.jzonbie.requests.AppRequest;
import com.jonnymatts.jzonbie.responses.AppResponse;
import com.jonnymatts.jzonbie.testing.StringBody;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.time.Duration;

import static com.jonnymatts.jzonbie.requests.AppRequest.get;
import static com.jonnymatts.jzonbie.responses.AppResponse.ok;
import static com.jonnymatts.jzonbie.util.Cloner.cloneRequest;
import static com.jonnymatts.jzonbie.util.Cloner.cloneResponse;
import static org.assertj.core.api.Assertions.assertThat;

public class ClonerTest {

    @Rule public FixtureRule fixtureRule = FixtureRule.initFixtures();

    private AppRequest request;
    private AppResponse response;

    @Before
    public void setUp() throws Exception {
        response = ok().withHeader("header", "value")
                .withDelay(Duration.ofSeconds(1))
                .withBody(new StringBody("body"))
                .build();

        request = get("/")
                .withHeader("header", "value")
                .withBody(new StringBody("body"))
                .withQueryParam("param", "value")
                .build();
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
}