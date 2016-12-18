package com.jonnymatts.jzonbie.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flextrade.jfixture.annotations.Fixture;
import com.flextrade.jfixture.rules.FixtureRule;
import com.jonnymatts.jzonbie.model.ZombiePriming;
import com.jonnymatts.jzonbie.model.ZombieRequest;
import com.jonnymatts.jzonbie.model.ZombieResponse;
import org.apache.http.Header;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.util.EntityUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ApacheJzonbieRequestFactoryTest {

    @Rule public FixtureRule fixtureRule = FixtureRule.initFixtures();
    @Rule public ExpectedException expectedException = ExpectedException.none();

    @Mock private ObjectMapper objectMapper;

    @Fixture private String zombieBaseUrl;
    @Fixture private ZombieRequest zombieRequest;
    @Fixture private ZombieResponse zombieResponse;
    @Fixture private String entityString;

    private ApacheJzonbieRequestFactory requestFactory;
    private RuntimeException runtimeException;

    @Before
    public void setUp() throws Exception {
        requestFactory = new ApacheJzonbieRequestFactory(zombieBaseUrl, objectMapper);

        runtimeException = new RuntimeException();

        zombieRequest.setBody(singletonMap("key", "value"));
        zombieResponse.setBody(singletonMap("key", "value"));
    }

    @Test
    public void createPrimeZombieRequest() throws Exception {
        when(objectMapper.writeValueAsString(new ZombiePriming(zombieRequest, zombieResponse))).thenReturn(entityString);

        final HttpUriRequest primeZombieRequest = requestFactory.createPrimeZombieRequest(zombieRequest, zombieResponse);

        assertThat(primeZombieRequest.getMethod()).isEqualTo("POST");
        assertThat(primeZombieRequest.getURI().toString()).isEqualTo(zombieBaseUrl);
        assertZombieHeader(primeZombieRequest, "priming");
        assertRequestBodyIsEqualTo(primeZombieRequest, entityString);
    }

    @Test
    public void createPrimeZombieRequestThrowsRuntimeExceptionIfAnyExceptionIsThrown() throws Exception {
        when(objectMapper.writeValueAsString(new ZombiePriming(zombieRequest, zombieResponse))).thenThrow(runtimeException);

        expectedException.expect(RuntimeException.class);
        expectedException.expectCause(is(runtimeException));

        requestFactory.createPrimeZombieRequest(zombieRequest, zombieResponse);
    }

    @Test
    public void createGetCurrentPrimingRequest() throws Exception {
        final HttpUriRequest getCurrentPrimingRequest = requestFactory.createGetCurrentPrimingRequest();

        assertThat(getCurrentPrimingRequest.getMethod()).isEqualTo("GET");
        assertThat(getCurrentPrimingRequest.getURI().toString()).isEqualTo(zombieBaseUrl);
        assertZombieHeader(getCurrentPrimingRequest, "list");
    }

    @Test
    public void createGetHistoryRequest() throws Exception {
        final HttpUriRequest getHistoryRequest = requestFactory.createGetHistoryRequest();

        assertThat(getHistoryRequest.getMethod()).isEqualTo("GET");
        assertThat(getHistoryRequest.getURI().toString()).isEqualTo(zombieBaseUrl);
        assertZombieHeader(getHistoryRequest, "history");
    }


    @Test
    public void createResetRequest() throws Exception {
        final HttpUriRequest resetRequest = requestFactory.createResetRequest();

        assertThat(resetRequest.getMethod()).isEqualTo("DELETE");
        assertThat(resetRequest.getURI().toString()).isEqualTo(zombieBaseUrl);
        assertZombieHeader(resetRequest, "reset");
    }

    private void assertRequestBodyIsEqualTo(HttpUriRequest primeZombieRequest, String entityString) throws IOException {
        final HttpEntityEnclosingRequest request = (HttpEntityEnclosingRequest) primeZombieRequest;
        assertThat(EntityUtils.toString(request.getEntity())).isEqualTo(entityString);
    }

    private void assertZombieHeader(HttpUriRequest primeZombieRequest, String zombieHeaderValue) {
        final Header[] zombieHeaders = primeZombieRequest.getHeaders("zombie");
        assertThat(zombieHeaders).hasSize(1);
        final Header zombieHeader = zombieHeaders[0];
        assertThat(zombieHeader.getValue()).isEqualTo(zombieHeaderValue);
    }
}