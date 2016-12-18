package com.jonnymatts.jzonbie.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jonnymatts.jzonbie.model.PrimedMapping;
import com.jonnymatts.jzonbie.model.ZombiePriming;
import com.jonnymatts.jzonbie.model.ZombieRequest;
import com.jonnymatts.jzonbie.model.ZombieResponse;
import com.jonnymatts.jzonbie.util.Deserializer;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;

import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

public class ApacheJzonbieHttpClient implements JzonbieHttpClient {

    private final ApacheJzonbieRequestFactory apacheJzonbieRequestFactory;
    private final HttpClient httpClient;
    private final Deserializer deserializer;

    public ApacheJzonbieHttpClient(String zombieBaseUrl) {
        final ObjectMapper objectMapper = new ObjectMapper().setSerializationInclusion(NON_NULL);

        this.apacheJzonbieRequestFactory = new ApacheJzonbieRequestFactory(zombieBaseUrl, objectMapper);
        this.httpClient = HttpClientBuilder.create().build();
        this.deserializer = new Deserializer(objectMapper);
    }

    public ApacheJzonbieHttpClient(HttpClient httpClient,
                                   ApacheJzonbieRequestFactory apacheJzonbieRequestFactory,
                                   Deserializer deserializer) {
        this.httpClient = httpClient;
        this.apacheJzonbieRequestFactory = apacheJzonbieRequestFactory;
        this.deserializer = deserializer;
    }

    @Override
    public ZombiePriming primeZombie(ZombieRequest request, ZombieResponse response) {
        final HttpUriRequest primeZombieRequest = apacheJzonbieRequestFactory.createPrimeZombieRequest(request, response);
        final HttpResponse httpResponse = execute(primeZombieRequest);
        return deserializer.deserialize(httpResponse, ZombiePriming.class);
    }

    @Override
    public List<PrimedMapping> getCurrentPriming() {
        final HttpUriRequest getCurrentPrimingRequest = apacheJzonbieRequestFactory.createGetCurrentPrimingRequest();
        final HttpResponse httpResponse = execute(getCurrentPrimingRequest);
        return deserializer.deserializeCollection(httpResponse, PrimedMapping.class);
    }

    @Override
    public List<ZombiePriming> getHistory() {
        final HttpUriRequest getHistoryRequest = apacheJzonbieRequestFactory.createGetHistoryRequest();
        final HttpResponse httpResponse = execute(getHistoryRequest);
        return deserializer.deserializeCollection(httpResponse, ZombiePriming.class);
    }

    @Override
    public void reset() {
        final HttpUriRequest resetRequest = apacheJzonbieRequestFactory.createResetRequest();
        execute(resetRequest);
    }

    private HttpResponse execute(HttpUriRequest request) {
        try {
            return httpClient.execute(request);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}