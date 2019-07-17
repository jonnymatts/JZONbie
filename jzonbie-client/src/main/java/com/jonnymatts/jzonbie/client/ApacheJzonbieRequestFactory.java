package com.jonnymatts.jzonbie.client;

import com.jonnymatts.jzonbie.jackson.JzonbieObjectMapper;
import com.jonnymatts.jzonbie.priming.ZombiePriming;
import com.jonnymatts.jzonbie.requests.AppRequest;
import com.jonnymatts.jzonbie.responses.AppResponse;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;

import java.io.File;

public class ApacheJzonbieRequestFactory {

    private static final String DEFAULT_ZOMBIE_HEADER_NAME = "zombie";

    private final String zombieBaseUrl;
    private final String zombieHeaderName;
    private final JzonbieObjectMapper objectMapper;

    public ApacheJzonbieRequestFactory(String zombieBaseUrl) {
        this(zombieBaseUrl, DEFAULT_ZOMBIE_HEADER_NAME);
    }

    public ApacheJzonbieRequestFactory(String zombieBaseUrl,
                                       String zombieHeaderName) {
        this.zombieBaseUrl = zombieBaseUrl;
        this.zombieHeaderName = zombieHeaderName;
        this.objectMapper = new JzonbieObjectMapper();
    }

    public ApacheJzonbieRequestFactory(String zombieBaseUrl,
                                       JzonbieObjectMapper objectMapper) {
        this(zombieBaseUrl, DEFAULT_ZOMBIE_HEADER_NAME, objectMapper);
    }

    public ApacheJzonbieRequestFactory(String zombieBaseUrl,
                                       String zombieHeaderName,
                                       JzonbieObjectMapper objectMapper) {
        this.zombieBaseUrl = zombieBaseUrl;
        this.zombieHeaderName = zombieHeaderName;
        this.objectMapper = objectMapper;
    }

    public HttpUriRequest createPrimeZombieRequest(AppRequest appRequest, AppResponse appResponse) {
        return createPostRequest(new ZombiePriming(appRequest, appResponse), "priming");
    }

    public HttpUriRequest createPrimeZombieForDefaultRequest(AppRequest appRequest, AppResponse appResponse) {
        return createPostRequest(new ZombiePriming(appRequest, appResponse), "priming-default");
    }

    public HttpUriRequest createPrimeZombieWithFileRequest(File file) {
        final HttpEntity entity = MultipartEntityBuilder.create()
                .addPart("priming", new FileBody(file))
                .build();
        return RequestBuilder.post(zombieBaseUrl)
                .addHeader(zombieHeaderName, "priming-file")
                .setEntity(entity)
                .build();
    }

    public HttpUriRequest createVerifyRequest(AppRequest appRequest) {
        return createPostRequest(appRequest, "count");
    }

    public HttpUriRequest createGetCurrentPrimingRequest() {
        return RequestBuilder.get(zombieBaseUrl)
                .addHeader(zombieHeaderName, "current")
                .build();
    }

    public HttpUriRequest createGetHistoryRequest() {
        return RequestBuilder.get(zombieBaseUrl)
                .addHeader(zombieHeaderName, "history")
                .build();
    }

    public HttpUriRequest createGetFailedRequestsRequest() {
        return RequestBuilder.get(zombieBaseUrl)
                .addHeader(zombieHeaderName, "failed")
                .build();
    }

    public HttpUriRequest createResetRequest() {
        return RequestBuilder.delete(zombieBaseUrl)
                .addHeader(zombieHeaderName, "reset")
                .build();
    }

    public HttpUriRequest createTruststoreRequest() {
        return RequestBuilder.get(zombieBaseUrl)
                .addHeader(zombieHeaderName, "truststore")
                .build();
    }

    private HttpUriRequest createPostRequest(Object requestBody, String zombieHeader) {
        try {
            return RequestBuilder.post(zombieBaseUrl)
                    .addHeader(zombieHeaderName, zombieHeader)
                    .setEntity(new StringEntity(objectMapper.writeValueAsString(requestBody)))
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
