package com.jonnymatts.jzonbie.model;

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

public class ZombieRequestTest {

    @Rule public FixtureRule fixtureRule = FixtureRule.initFixtures();

    @Fixture private String username;

    @Fixture private String password;

    private ZombieRequest zombieRequest;

    @Before
    public void setUp() throws Exception {
        zombieRequest = new ZombieRequest();
        zombieRequest.setHeaders(new HashMap<>());
    }

    @Test
    public void setBasicAuthAddsAuthorizationHeaderToHeaders() {
        final String authValue = String.format("%s:%s", username, password);
        final String encodedAuthValue = Base64.getEncoder().encodeToString(authValue.getBytes());

        zombieRequest.setBasicAuth(singletonMap(username, password));

        final Map<String, String> headers = zombieRequest.getHeaders();

        assertThat(headers).containsEntry("Authorization", encodedAuthValue);
    }
}