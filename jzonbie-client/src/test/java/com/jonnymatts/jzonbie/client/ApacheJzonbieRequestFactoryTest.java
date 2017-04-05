package com.jonnymatts.jzonbie.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flextrade.jfixture.annotations.Fixture;
import com.flextrade.jfixture.rules.FixtureRule;
import com.jonnymatts.jzonbie.model.AppRequest;
import com.jonnymatts.jzonbie.model.AppResponse;
import com.jonnymatts.jzonbie.model.VerificationRequest;
import com.jonnymatts.jzonbie.model.ZombiePriming;
import com.jonnymatts.jzonbie.util.AppRequestBuilderUtil;
import com.jonnymatts.jzonbie.util.AppResponseBuilderUtil;
import com.jonnymatts.jzonbie.verification.InvocationVerificationCriteria;
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

import static com.jonnymatts.jzonbie.verification.InvocationVerificationCriteria.equalTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ApacheJzonbieRequestFactoryTest {

    @Rule public FixtureRule fixtureRule = FixtureRule.initFixtures();
    @Rule public ExpectedException expectedException = ExpectedException.none();

    @Mock private ObjectMapper objectMapper;

    @Fixture private String zombieBaseUrl;
    @Fixture private AppResponse appResponse;
    @Fixture private String entityString;

    private AppRequest appRequest;
    private InvocationVerificationCriteria criteria;
    private ApacheJzonbieRequestFactory requestFactory;
    private RuntimeException runtimeException;

    @Before
    public void setUp() throws Exception {
        appRequest = AppRequestBuilderUtil.getFixturedAppRequest();

        criteria = equalTo(2);

        appResponse = AppResponseBuilderUtil.getFixturedAppResponse();

        requestFactory = new ApacheJzonbieRequestFactory(zombieBaseUrl, objectMapper);

        runtimeException = new RuntimeException();
    }

    @Test
    public void createPrimeZombieRequest() throws Exception {
        when(objectMapper.writeValueAsString(new ZombiePriming(appRequest, appResponse))).thenReturn(entityString);

        final HttpUriRequest primeZombieRequest = requestFactory.createPrimeZombieRequest(appRequest, appResponse);

        assertThat(primeZombieRequest.getMethod()).isEqualTo("POST");
        assertThat(primeZombieRequest.getURI().toString()).isEqualTo(zombieBaseUrl);
        assertZombieHeader(primeZombieRequest, "zombie", "priming");
        assertRequestBodyIsEqualTo(primeZombieRequest, entityString);
    }

    @Test
    public void createPrimeZombieRequestThrowsRuntimeExceptionIfAnyExceptionIsThrown() throws Exception {
        when(objectMapper.writeValueAsString(new ZombiePriming(appRequest, appResponse))).thenThrow(runtimeException);

        expectedException.expect(RuntimeException.class);
        expectedException.expectCause(is(runtimeException));

        requestFactory.createPrimeZombieRequest(appRequest, appResponse);
    }

    @Test
    public void createPrimeZombieForDefaultRequest() throws Exception {
        when(objectMapper.writeValueAsString(new ZombiePriming(appRequest, appResponse))).thenReturn(entityString);

        final HttpUriRequest primeZombieRequest = requestFactory.createPrimeZombieForDefaultRequest(appRequest, appResponse);

        assertThat(primeZombieRequest.getMethod()).isEqualTo("POST");
        assertThat(primeZombieRequest.getURI().toString()).isEqualTo(zombieBaseUrl);
        assertZombieHeader(primeZombieRequest, "zombie", "priming-default");
        assertRequestBodyIsEqualTo(primeZombieRequest, entityString);
    }

    @Test
    public void createGetCurrentPrimingRequest() throws Exception {
        final HttpUriRequest getCurrentPrimingRequest = requestFactory.createGetCurrentPrimingRequest();

        assertThat(getCurrentPrimingRequest.getMethod()).isEqualTo("GET");
        assertThat(getCurrentPrimingRequest.getURI().toString()).isEqualTo(zombieBaseUrl);
        assertZombieHeader(getCurrentPrimingRequest, "zombie", "current");
    }

    @Test
    public void createGetHistoryRequest() throws Exception {
        final HttpUriRequest getHistoryRequest = requestFactory.createGetHistoryRequest();

        assertThat(getHistoryRequest.getMethod()).isEqualTo("GET");
        assertThat(getHistoryRequest.getURI().toString()).isEqualTo(zombieBaseUrl);
        assertZombieHeader(getHistoryRequest, "zombie", "history");
    }

    @Test
    public void createResetRequest() throws Exception {
        final HttpUriRequest resetRequest = requestFactory.createResetRequest();

        assertThat(resetRequest.getMethod()).isEqualTo("DELETE");
        assertThat(resetRequest.getURI().toString()).isEqualTo(zombieBaseUrl);
        assertZombieHeader(resetRequest, "zombie", "reset");
    }

    @Test
    public void createVerifyRequest() throws Exception {
        final VerificationRequest verificationRequest = new VerificationRequest(appRequest, criteria);
        when(objectMapper.writeValueAsString(verificationRequest)).thenReturn(entityString);

        final HttpUriRequest primeZombieRequest = requestFactory.createVerifyRequest(appRequest, criteria);

        assertThat(primeZombieRequest.getMethod()).isEqualTo("POST");
        assertThat(primeZombieRequest.getURI().toString()).isEqualTo(zombieBaseUrl);
        assertZombieHeader(primeZombieRequest, "zombie", "verify");
        assertRequestBodyIsEqualTo(primeZombieRequest, entityString);
    }

    @Test
    public void zombieHeaderNameCanBeSetByConstructor() throws Exception {
        final String zombieHeaderName = "jzonbie";
        requestFactory = new ApacheJzonbieRequestFactory(zombieBaseUrl, zombieHeaderName, objectMapper);

        final HttpUriRequest resetRequest = requestFactory.createResetRequest();

        assertThat(resetRequest.getMethod()).isEqualTo("DELETE");
        assertThat(resetRequest.getURI().toString()).isEqualTo(zombieBaseUrl);
        assertZombieHeader(resetRequest, zombieHeaderName, "reset");
    }

    private void assertRequestBodyIsEqualTo(HttpUriRequest primeZombieRequest, String entityString) throws IOException {
        final HttpEntityEnclosingRequest request = (HttpEntityEnclosingRequest) primeZombieRequest;
        assertThat(EntityUtils.toString(request.getEntity())).isEqualTo(entityString);
    }

    private void assertZombieHeader(HttpUriRequest primeZombieRequest, String zombieHeaderName, String zombieHeaderValue) {
        final Header[] zombieHeaders = primeZombieRequest.getHeaders(zombieHeaderName);
        assertThat(zombieHeaders).hasSize(1);
        final Header zombieHeader = zombieHeaders[0];
        assertThat(zombieHeader.getValue()).isEqualTo(zombieHeaderValue);
    }
}