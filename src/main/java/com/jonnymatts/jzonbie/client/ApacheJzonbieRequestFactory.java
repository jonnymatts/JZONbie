package com.jonnymatts.jzonbie.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jonnymatts.jzonbie.model.AppRequest;
import com.jonnymatts.jzonbie.model.AppResponse;
import com.jonnymatts.jzonbie.model.ZombiePriming;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.StringEntity;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

public class ApacheJzonbieRequestFactory {

    private final String zombieBaseUrl;
    private final ObjectMapper objectMapper;

    public ApacheJzonbieRequestFactory(String zombieBaseUrl) {
        this.zombieBaseUrl = zombieBaseUrl;
        this.objectMapper = new ObjectMapper().setSerializationInclusion(NON_NULL);
    }

    public ApacheJzonbieRequestFactory(String zombieBaseUrl,
                                       ObjectMapper objectMapper) {
        this.zombieBaseUrl = zombieBaseUrl;
        this.objectMapper = objectMapper;
    }

    public HttpUriRequest createPrimeZombieRequest(AppRequest appRequest, AppResponse appResponse) {
        return createPrimingRequest(appRequest, appResponse, "priming");
    }

    public HttpUriRequest createPrimeZombieForDefaultRequest(AppRequest appRequest, AppResponse appResponse) {
        return createPrimingRequest(appRequest, appResponse, "priming-default");
    }

    public HttpUriRequest createGetCurrentPrimingRequest() {
        return RequestBuilder.get(zombieBaseUrl)
                .addHeader("zombie", "list")
                .build();
    }

    public HttpUriRequest createGetHistoryRequest() {
        return RequestBuilder.get(zombieBaseUrl)
                .addHeader("zombie", "history")
                .build();
    }

    public HttpUriRequest createResetRequest() {
        return RequestBuilder.delete(zombieBaseUrl)
                .addHeader("zombie", "reset")
                .build();
    }

    private HttpUriRequest createPrimingRequest(AppRequest appRequest, AppResponse appResponse, String zombieHeader) {
        final ZombiePriming zombiePriming = new ZombiePriming(appRequest, appResponse);
        try {
            return RequestBuilder.post(zombieBaseUrl)
                    .addHeader("zombie", zombieHeader)
                    .setEntity(new StringEntity(objectMapper.writeValueAsString(zombiePriming)))
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
