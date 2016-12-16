package com.jonnymatts.jzonbie.client;

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

public class ApacheJzonbieHttpClient implements JzonbieHttpClient {

    private final JzonbieRequestFactory jzonbieRequestFactory;
    private final HttpClient httpClient;
    private final Deserializer deserializer;

    public ApacheJzonbieHttpClient(JzonbieRequestFactory jzonbieRequestFactory,
                                   Deserializer deserializer) {
        this.httpClient = HttpClientBuilder.create().build();
        this.jzonbieRequestFactory = jzonbieRequestFactory;
        this.deserializer = deserializer;
    }

    public ApacheJzonbieHttpClient(HttpClient httpClient,
                                   JzonbieRequestFactory jzonbieRequestFactory,
                                   Deserializer deserializer) {
        this.httpClient = httpClient;
        this.jzonbieRequestFactory = jzonbieRequestFactory;
        this.deserializer = deserializer;
    }

    @Override
    public ZombiePriming primeZombie(ZombieRequest request, ZombieResponse response) {
        final HttpUriRequest primeZombieRequest = jzonbieRequestFactory.createPrimeZombieRequest(request, response);
        final HttpResponse httpResponse = execute(primeZombieRequest);
        return deserializer.deserialize(httpResponse, ZombiePriming.class);
    }

    @Override
    public List<PrimedMapping> getCurrentPriming() {
        final HttpUriRequest getCurrentPrimingRequest = jzonbieRequestFactory.createGetCurrentPrimingRequest();
        final HttpResponse httpResponse = execute(getCurrentPrimingRequest);
        return deserializer.deserializeCollection(httpResponse, PrimedMapping.class);
    }

    @Override
    public List<ZombiePriming> getHistory() {
        final HttpUriRequest getHistoryRequest = jzonbieRequestFactory.createGetHistoryRequest();
        final HttpResponse httpResponse = execute(getHistoryRequest);
        return deserializer.deserializeCollection(httpResponse, ZombiePriming.class);
    }

    @Override
    public void reset() {
        final HttpUriRequest resetRequest = jzonbieRequestFactory.createResetRequest();
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