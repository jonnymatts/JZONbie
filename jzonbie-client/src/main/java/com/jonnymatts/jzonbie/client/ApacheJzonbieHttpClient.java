package com.jonnymatts.jzonbie.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jonnymatts.jzonbie.jackson.JzonbieObjectMapper;
import com.jonnymatts.jzonbie.model.*;
import com.jonnymatts.jzonbie.response.DefaultAppResponse;
import com.jonnymatts.jzonbie.util.Deserializer;
import com.jonnymatts.jzonbie.verification.CountResult;
import com.jonnymatts.jzonbie.verification.InvocationVerificationCriteria;
import com.jonnymatts.jzonbie.verification.VerificationException;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.File;
import java.util.List;
import java.util.function.Function;

import static java.util.function.Function.identity;

public class ApacheJzonbieHttpClient implements JzonbieClient {

    private final ApacheJzonbieRequestFactory apacheJzonbieRequestFactory;
    private final CloseableHttpClient httpClient;
    private final Deserializer deserializer;

    public ApacheJzonbieHttpClient(String zombieBaseUrl) {
        final ObjectMapper objectMapper = new JzonbieObjectMapper();

        this.apacheJzonbieRequestFactory = new ApacheJzonbieRequestFactory(zombieBaseUrl, objectMapper);
        this.httpClient = HttpClientBuilder.create().build();
        this.deserializer = new Deserializer();
    }

    public ApacheJzonbieHttpClient(String zombieBaseUrl,
                                   String zombieHeaderName) {
        final ObjectMapper objectMapper = new JzonbieObjectMapper();

        this.apacheJzonbieRequestFactory = new ApacheJzonbieRequestFactory(zombieBaseUrl, zombieHeaderName, objectMapper);
        this.httpClient = HttpClientBuilder.create().build();
        this.deserializer = new Deserializer(objectMapper);
    }

    public ApacheJzonbieHttpClient(CloseableHttpClient httpClient,
                                   ApacheJzonbieRequestFactory apacheJzonbieRequestFactory,
                                   Deserializer deserializer) {
        this.httpClient = httpClient;
        this.apacheJzonbieRequestFactory = apacheJzonbieRequestFactory;
        this.deserializer = deserializer;
    }

    @Override
    public ZombiePriming prime(AppRequest request, AppResponse response) {
        final HttpUriRequest primeZombieRequest = apacheJzonbieRequestFactory.createPrimeZombieRequest(request, response);
        return execute(primeZombieRequest, httpResponse -> deserializer.deserialize(httpResponse, ZombiePriming.class));
    }

    @Override
    public ZombiePriming prime(AppRequest request, TemplatedAppResponse response) {
        final HttpUriRequest primeZombieRequest = apacheJzonbieRequestFactory.createPrimeZombieForTemplateRequest(request, response);
        return execute(primeZombieRequest, httpResponse -> deserializer.deserialize(httpResponse, ZombiePriming.class));
    }

    @Override
    public List<PrimedMapping> prime(File file) {
        final HttpUriRequest primeZombieRequest = apacheJzonbieRequestFactory.createPrimeZombieWithFileRequest(file);
        return execute(primeZombieRequest, httpResponse -> deserializer.deserializeCollection(httpResponse, PrimedMapping.class));
    }

    @Override
    public ZombiePriming prime(AppRequest request, DefaultAppResponse defaultAppResponse) {
        if(defaultAppResponse.isDynamic()) throw new UnsupportedOperationException("Priming dynamic default for zombie over HTTP not supported");
        final HttpUriRequest primeZombieRequest = defaultAppResponse.isTemplated() ? apacheJzonbieRequestFactory.createPrimeZombieForDefaultTemplateRequest(request, (TemplatedAppResponse)defaultAppResponse.getResponse())
                : apacheJzonbieRequestFactory.createPrimeZombieForDefaultRequest(request, defaultAppResponse.getResponse());
        return execute(primeZombieRequest, response -> deserializer.deserialize(response, ZombiePriming.class));
    }

    @Override
    public List<PrimedMapping> getCurrentPriming() {
        final HttpUriRequest getCurrentPrimingRequest = apacheJzonbieRequestFactory.createGetCurrentPrimingRequest();
        return execute(getCurrentPrimingRequest, response -> deserializer.deserializeCollection(response, PrimedMapping.class));
    }

    @Override
    public List<ZombiePriming> getHistory() {
        final HttpUriRequest getHistoryRequest = apacheJzonbieRequestFactory.createGetHistoryRequest();
        return execute(getHistoryRequest, response -> deserializer.deserializeCollection(response, ZombiePriming.class));
    }

    @Override
    public List<AppRequest> getFailedRequests() {
        final HttpUriRequest getFailedRequestsRequest = apacheJzonbieRequestFactory.createGetFailedRequestsRequest();
        return execute(getFailedRequestsRequest, response -> deserializer.deserializeCollection(response, AppRequest.class));
    }

    @Override
    public void verify(AppRequest appRequest, InvocationVerificationCriteria criteria) throws VerificationException {
        final HttpUriRequest verifyRequest = apacheJzonbieRequestFactory.createVerifyRequest(appRequest);
        final CountResult count = execute(verifyRequest, response -> deserializer.deserialize(response, CountResult.class));
        criteria.verify(count.getCount());
    }

    @Override
    public void reset() {
        final HttpUriRequest resetRequest = apacheJzonbieRequestFactory.createResetRequest();
        execute(resetRequest, identity());
    }

    private <T> T execute(HttpUriRequest request, Function<HttpResponse, T> mapper) {
        try(CloseableHttpResponse response = httpClient.execute(request)) {
            return mapper.apply(response);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}