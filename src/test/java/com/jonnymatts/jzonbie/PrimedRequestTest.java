package com.jonnymatts.jzonbie;

import com.flextrade.jfixture.annotations.Fixture;
import com.flextrade.jfixture.rules.FixtureRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

public class PrimedRequestTest {

    @Rule public FixtureRule fixtureRule = FixtureRule.initFixtures();

    @Fixture private String username;

    @Fixture private String password;

    private PrimedRequest primedRequest;

    @Before
    public void setUp() throws Exception {
        primedRequest = new PrimedRequest();
        primedRequest.setHeaders(new HashMap<>());
    }

    @Test
    public void setBasicAuthAddsAuthorizationHeaderToHeaders() {
        final String authValue = String.format("%s:%s", username, password);
        final String encodedAuthValue = Base64.getEncoder().encodeToString(authValue.getBytes());

        primedRequest.setBasicAuth(singletonMap(username, password));

        final Map<String, String> headers = primedRequest.getHeaders();

        assertThat(headers).containsEntry("Authorization", encodedAuthValue);
    }
}