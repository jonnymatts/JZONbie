package com.jonnymatts.jzonbie.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flextrade.jfixture.annotations.Fixture;
import com.flextrade.jfixture.rules.FixtureRule;
import com.jonnymatts.jzonbie.model.AppRequest;
import com.jonnymatts.jzonbie.model.AppResponse;
import com.jonnymatts.jzonbie.model.ZombiePriming;
import com.jonnymatts.jzonbie.util.AppRequestBuilderUtil;
import com.jonnymatts.jzonbie.util.AppResponseBuilderUtil;
import com.jonnymatts.jzonbie.verification.InvocationVerificationCriteria;
import org.apache.commons.io.IOUtils;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static com.jonnymatts.jzonbie.model.TemplatedAppResponse.templated;
import static com.jonnymatts.jzonbie.verification.InvocationVerificationCriteria.equalTo;
import static java.nio.charset.Charset.defaultCharset;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ApacheJzonbieRequestFactoryTest {

    @Rule public FixtureRule fixtureRule = FixtureRule.initFixtures();
    @Rule public ExpectedException expectedException = ExpectedException.none();

    @Mock private ObjectMapper objectMapper;

    @Fixture private String zombieBaseUrl;
    @Fixture private String entityString;

    private AppRequest appRequest;
    private AppResponse appResponse;
    private InvocationVerificationCriteria criteria;
    private ApacheJzonbieRequestFactory requestFactory;
    private RuntimeException runtimeException;

    @Before
    public void setUp() throws Exception {
        appRequest = AppRequestBuilderUtil.getFixturedAppRequest();
        appResponse = AppResponseBuilderUtil.getFixturedAppResponse();

        criteria = equalTo(2);

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
    public void createPrimeZombieForTemplateRequest() throws Exception {
        when(objectMapper.writeValueAsString(new ZombiePriming(appRequest, templated(appResponse)))).thenReturn(entityString);

        final HttpUriRequest primeZombieRequest = requestFactory.createPrimeZombieForTemplateRequest(appRequest, templated(appResponse));

        assertThat(primeZombieRequest.getMethod()).isEqualTo("POST");
        assertThat(primeZombieRequest.getURI().toString()).isEqualTo(zombieBaseUrl);
        assertZombieHeader(primeZombieRequest, "zombie", "priming-template");
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
    public void createPrimeZombieForDefaultTemplateRequest() throws Exception {
        when(objectMapper.writeValueAsString(new ZombiePriming(appRequest, templated(appResponse)))).thenReturn(entityString);

        final HttpUriRequest primeZombieRequest = requestFactory.createPrimeZombieForDefaultTemplateRequest(appRequest, templated(appResponse));

        assertThat(primeZombieRequest.getMethod()).isEqualTo("POST");
        assertThat(primeZombieRequest.getURI().toString()).isEqualTo(zombieBaseUrl);
        assertZombieHeader(primeZombieRequest, "zombie", "priming-default-template");
        assertRequestBodyIsEqualTo(primeZombieRequest, entityString);
    }

    @Test
    public void createPrimeZombieWithFileRequest() throws Exception {
        final File file = new File(getClass().getClassLoader().getResource("example-priming.json").getFile());

        final HttpUriRequest primeZombieRequest = requestFactory.createPrimeZombieWithFileRequest(file);

        assertThat(primeZombieRequest.getMethod()).isEqualTo("POST");
        assertThat(primeZombieRequest.getURI().toString()).isEqualTo(zombieBaseUrl);
        assertZombieHeader(primeZombieRequest, "zombie", "priming-file");

        final HttpEntityEnclosingRequest httpPost = (HttpEntityEnclosingRequest)primeZombieRequest;

        final String entityString = IOUtils.toString(httpPost.getEntity().getContent(), defaultCharset());
        final String fileString = IOUtils.toString(new FileInputStream(file), defaultCharset());

        assertThat(entityString).contains(fileString);
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
    public void createGetFailedRequestsRequest() throws Exception {
        final HttpUriRequest getFailedRequestsRequest = requestFactory.createGetFailedRequestsRequest();

        assertThat(getFailedRequestsRequest.getMethod()).isEqualTo("GET");
        assertThat(getFailedRequestsRequest.getURI().toString()).isEqualTo(zombieBaseUrl);
        assertZombieHeader(getFailedRequestsRequest, "zombie", "failed");
    }

    @Test
    public void createResetRequest() throws Exception {
        final HttpUriRequest resetRequest = requestFactory.createResetRequest();

        assertThat(resetRequest.getMethod()).isEqualTo("DELETE");
        assertThat(resetRequest.getURI().toString()).isEqualTo(zombieBaseUrl);
        assertZombieHeader(resetRequest, "zombie", "reset");
    }

    @Test
    public void createCountRequest() throws Exception {
        when(objectMapper.writeValueAsString(appRequest)).thenReturn(entityString);

        final HttpUriRequest primeZombieRequest = requestFactory.createVerifyRequest(appRequest);

        assertThat(primeZombieRequest.getMethod()).isEqualTo("POST");
        assertThat(primeZombieRequest.getURI().toString()).isEqualTo(zombieBaseUrl);
        assertZombieHeader(primeZombieRequest, "zombie", "count");
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