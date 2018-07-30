package com.jonnymatts.jzonbie.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jonnymatts.jzonbie.model.AppRequest;
import com.jonnymatts.jzonbie.model.AppResponse;
import com.jonnymatts.jzonbie.model.TemplatedAppResponse;
import com.jonnymatts.jzonbie.model.ZombiePriming;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;

import java.io.File;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

public class ApacheJzonbieRequestFactory {

    private static final String DEFAULT_ZOMBIE_HEADER_NAME = "zombie";

    private final String zombieBaseUrl;
    private final String zombieHeaderName;
    private final ObjectMapper objectMapper;

    public ApacheJzonbieRequestFactory(String zombieBaseUrl) {
        this(zombieBaseUrl, DEFAULT_ZOMBIE_HEADER_NAME);
    }

    public ApacheJzonbieRequestFactory(String zombieBaseUrl,
                                       String zombieHeaderName) {
        this.zombieBaseUrl = zombieBaseUrl;
        this.zombieHeaderName = zombieHeaderName;
        this.objectMapper = new ObjectMapper().setSerializationInclusion(NON_NULL);
    }

    public ApacheJzonbieRequestFactory(String zombieBaseUrl,
                                       ObjectMapper objectMapper) {
        this(zombieBaseUrl, DEFAULT_ZOMBIE_HEADER_NAME, objectMapper);
    }

    public ApacheJzonbieRequestFactory(String zombieBaseUrl,
                                       String zombieHeaderName,
                                       ObjectMapper objectMapper) {
        this.zombieBaseUrl = zombieBaseUrl;
        this.zombieHeaderName = zombieHeaderName;
        this.objectMapper = objectMapper;
    }

    public HttpUriRequest createPrimeZombieRequest(AppRequest appRequest, AppResponse appResponse) {
        return createPostRequest(new ZombiePriming(appRequest, appResponse), "priming");
    }

    public HttpUriRequest createPrimeZombieForTemplateRequest(AppRequest appRequest, TemplatedAppResponse appResponse) {
        return createPostRequest(new ZombiePriming(appRequest, appResponse), "priming-template");
    }

    public HttpUriRequest createPrimeZombieForDefaultRequest(AppRequest appRequest, AppResponse appResponse) {
        return createPostRequest(new ZombiePriming(appRequest, appResponse), "priming-default");
    }

    public HttpUriRequest createPrimeZombieForDefaultTemplateRequest(AppRequest appRequest, TemplatedAppResponse appResponse) {
        return createPostRequest(new ZombiePriming(appRequest, appResponse), "priming-default-template");
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
