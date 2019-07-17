package com.jonnymatts.jzonbie.client;

import com.jonnymatts.jzonbie.JzonbieClient;
import com.jonnymatts.jzonbie.jackson.Deserializer;
import com.jonnymatts.jzonbie.jackson.JzonbieObjectMapper;
import com.jonnymatts.jzonbie.priming.PrimedMapping;
import com.jonnymatts.jzonbie.priming.ZombiePriming;
import com.jonnymatts.jzonbie.requests.AppRequest;
import com.jonnymatts.jzonbie.responses.AppResponse;
import com.jonnymatts.jzonbie.responses.defaults.DefaultAppResponse;
import com.jonnymatts.jzonbie.responses.defaults.DynamicDefaultAppResponse;
import com.jonnymatts.jzonbie.verification.CountResult;
import com.jonnymatts.jzonbie.verification.InvocationVerificationCriteria;
import com.jonnymatts.jzonbie.verification.VerificationException;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.security.KeyStore;
import java.util.List;
import java.util.function.Function;

import static java.lang.String.format;
import static java.util.Base64.getDecoder;
import static java.util.function.Function.identity;
import static org.apache.http.HttpStatus.SC_OK;

public class ApacheJzonbieHttpClient implements JzonbieClient {

    private final ApacheJzonbieRequestFactory apacheJzonbieRequestFactory;
    private final CloseableHttpClient httpClient;
    private final Deserializer deserializer;

    public ApacheJzonbieHttpClient(String zombieBaseUrl) {
        final JzonbieObjectMapper objectMapper = new JzonbieObjectMapper();

        this.apacheJzonbieRequestFactory = new ApacheJzonbieRequestFactory(zombieBaseUrl, objectMapper);
        this.httpClient = HttpClientBuilder.create().build();
        this.deserializer = new Deserializer();
    }

    public ApacheJzonbieHttpClient(String zombieBaseUrl,
                                   String zombieHeaderName) {
        final JzonbieObjectMapper objectMapper = new JzonbieObjectMapper();

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
        return execute(
                primeZombieRequest,
                httpResponse -> deserializer.deserialize(getHttpResponseBody(httpResponse), ZombiePriming.class),
                format("Failed to prime. %s, %s", request, response)
        );
    }

    @Override
    public List<PrimedMapping> prime(File file) {
        final HttpUriRequest primeZombieRequest = apacheJzonbieRequestFactory.createPrimeZombieWithFileRequest(file);
        return execute(
                primeZombieRequest, httpResponse -> deserializer.deserializeCollection(getHttpResponseBody(httpResponse), PrimedMapping.class),
                format("Failed to prime with file %s.", file.getAbsolutePath())
        );
    }

    @Override
    public ZombiePriming prime(AppRequest request, DefaultAppResponse defaultAppResponse) {
        if(defaultAppResponse instanceof DynamicDefaultAppResponse) throw new UnsupportedOperationException("Priming dynamic default for zombie over HTTP not supported");
        final HttpUriRequest primeZombieRequest = apacheJzonbieRequestFactory.createPrimeZombieForDefaultRequest(request, defaultAppResponse.getResponse());
        return execute(
                primeZombieRequest,
                httpResponse -> deserializer.deserialize(getHttpResponseBody(httpResponse), ZombiePriming.class),
                format("Failed to prime. %s, %s", request, defaultAppResponse)
        );
    }

    @Override
    public List<PrimedMapping> getCurrentPriming() {
        final HttpUriRequest getCurrentPrimingRequest = apacheJzonbieRequestFactory.createGetCurrentPrimingRequest();
        return execute(
                getCurrentPrimingRequest,
                httpResponse -> deserializer.deserializeCollection(getHttpResponseBody(httpResponse), PrimedMapping.class),
                "Failed to get current priming."
        );
    }

    @Override
    public List<ZombiePriming> getHistory() {
        final HttpUriRequest getHistoryRequest = apacheJzonbieRequestFactory.createGetHistoryRequest();
        return execute(
                getHistoryRequest,
                httpResponse -> deserializer.deserializeCollection(getHttpResponseBody(httpResponse), ZombiePriming.class),
                "Failed to get history."
        );
    }

    @Override
    public List<AppRequest> getFailedRequests() {
        final HttpUriRequest getFailedRequestsRequest = apacheJzonbieRequestFactory.createGetFailedRequestsRequest();
        return execute(
                getFailedRequestsRequest,
                httpResponse -> deserializer.deserializeCollection(getHttpResponseBody(httpResponse), AppRequest.class),
                "Failed to get failed requests."
        );
    }

    @Override
    public void verify(AppRequest appRequest, InvocationVerificationCriteria criteria) throws VerificationException {
        final HttpUriRequest verifyRequest = apacheJzonbieRequestFactory.createVerifyRequest(appRequest);
        final CountResult count = execute(
                verifyRequest,
                httpResponse -> deserializer.deserialize(getHttpResponseBody(httpResponse), CountResult.class),
                "Failed to get app request count."
        );
        criteria.verify(count.getCount());
    }

    @Override
    public void reset() {
        final HttpUriRequest resetRequest = apacheJzonbieRequestFactory.createResetRequest();
        execute(
                resetRequest,
                identity(),
                "Failed to reset."
        );
    }

    @Override
    public KeyStore getTruststore() {
        final HttpUriRequest truststoreRequest = apacheJzonbieRequestFactory.createTruststoreRequest();

        return execute(
                truststoreRequest,
                this::convertBytesToKeystore,
                "Failed to obtain truststore."
        );
    }

    private <T> T execute(HttpUriRequest request, Function<HttpResponse, T> mapper, String messageIfFailureOccurs) {
        try(CloseableHttpResponse response = httpClient.execute(request)) {
            return mapper.apply(response);
        } catch (Exception e) {
            throw new JzonbieClientException(messageIfFailureOccurs, e);
        }
    }

    private String getHttpResponseBody(HttpResponse response) {
        try {
            return EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            throw new JzonbieClientException("Could not get body from HTTP response.");
        }
    }

    private KeyStore convertBytesToKeystore(HttpResponse response) {
        if(response.getStatusLine().getStatusCode() != SC_OK) {
            throw new IllegalStateException("Failed to obtain truststore from server");
        }
        try {
            final KeyStore keyStore = KeyStore.getInstance("jks");
            final String content = EntityUtils.toString(response.getEntity());
            final String deserializedContent = deserializer.deserialize(content, String.class);
            final byte[] bytes = getDecoder().decode(deserializedContent);
            keyStore.load(new ByteArrayInputStream(bytes), new char[0]);
            return keyStore;
        } catch(Exception e) {
            throw new RuntimeException("Failed to convert response to keystore", e);
        }
    }
}