package com.jonnymatts.jzonbie.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jonnymatts.jzonbie.model.AppRequest;
import com.jonnymatts.jzonbie.model.AppResponse;
import com.jonnymatts.jzonbie.model.PrimedMapping;
import com.jonnymatts.jzonbie.model.ZombiePriming;
import com.jonnymatts.jzonbie.response.DefaultAppResponse;
import com.jonnymatts.jzonbie.util.Deserializer;
import com.jonnymatts.jzonbie.verification.InvocationVerificationCriteria;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.File;
import java.util.List;
import java.util.function.Function;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.jonnymatts.jzonbie.verification.InvocationVerificationCriteria.equalTo;
import static java.util.function.Function.identity;

public class ApacheJzonbieHttpClient implements JzonbieClient {

    private final ApacheJzonbieRequestFactory apacheJzonbieRequestFactory;
    private final CloseableHttpClient httpClient;
    private final Deserializer deserializer;

    public ApacheJzonbieHttpClient(String zombieBaseUrl) {
        final ObjectMapper objectMapper = new ObjectMapper().setSerializationInclusion(NON_NULL);

        this.apacheJzonbieRequestFactory = new ApacheJzonbieRequestFactory(zombieBaseUrl, objectMapper);
        this.httpClient = HttpClientBuilder.create().build();
        this.deserializer = new Deserializer(objectMapper);
    }

    public ApacheJzonbieHttpClient(String zombieBaseUrl,
                                   String zombieHeaderName) {
        final ObjectMapper objectMapper = new ObjectMapper().setSerializationInclusion(NON_NULL);

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
    public ZombiePriming primeZombie(AppRequest request, AppResponse response) {
        final HttpUriRequest primeZombieRequest = apacheJzonbieRequestFactory.createPrimeZombieRequest(request, response);
        return execute(primeZombieRequest, httpResponse -> deserializer.deserialize(httpResponse, ZombiePriming.class));
    }

    @Override
    public List<PrimedMapping> primeZombie(File file) {
        final HttpUriRequest primeZombieRequest = apacheJzonbieRequestFactory.createPrimeZombieWithFileRequest(file);
        return execute(primeZombieRequest, httpResponse -> deserializer.deserializeCollection(httpResponse, PrimedMapping.class));
    }

    @Override
    public ZombiePriming primeZombieForDefault(AppRequest request, DefaultAppResponse defaultAppResponse) {
        if(defaultAppResponse.isDynamic()) throw new UnsupportedOperationException("Priming dynamic default for zombie over HTTP not supported");
        final HttpUriRequest primeZombieRequest = apacheJzonbieRequestFactory.createPrimeZombieForDefaultRequest(request, defaultAppResponse.getResponse());
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
    public boolean verify(AppRequest appRequest) {
        return verify(appRequest, equalTo(1));
    }

    @Override
    public boolean verify(AppRequest appRequest, InvocationVerificationCriteria criteria) {
        final HttpUriRequest verifyRequest = apacheJzonbieRequestFactory.createVerifyRequest(appRequest, criteria);
        return execute(verifyRequest, response -> deserializer.deserialize(response, Boolean.class));
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