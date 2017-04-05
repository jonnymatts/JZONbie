package com.jonnymatts.jzonbie.client;

import com.jonnymatts.jzonbie.model.AppRequest;
import com.jonnymatts.jzonbie.model.AppResponse;
import com.jonnymatts.jzonbie.model.PrimedMapping;
import com.jonnymatts.jzonbie.model.ZombiePriming;
import com.jonnymatts.jzonbie.response.DefaultAppResponse;

import java.util.List;

import static com.jonnymatts.jzonbie.response.DefaultAppResponse.StaticDefaultAppResponse.staticDefault;
import static java.util.Collections.singletonMap;

public class JzonbieHttpClient implements JzonbieClient {

    private final JzonbieClient httpClient;

    public JzonbieHttpClient(String zombieBaseUrl) {
        this.httpClient = new ApacheJzonbieHttpClient(zombieBaseUrl);
    }

    public JzonbieHttpClient(String zombieBaseUrl,
                             String zombieHeaderName) {
        this.httpClient = new ApacheJzonbieHttpClient(zombieBaseUrl, zombieHeaderName);
    }

    public JzonbieHttpClient(JzonbieClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public ZombiePriming primeZombie(AppRequest request, AppResponse response) {
        return httpClient.primeZombie(request, response);
    }

    @Override
    public ZombiePriming primeZombieForDefault(AppRequest request, DefaultAppResponse response) {
        return httpClient.primeZombieForDefault(request, response);
    }

    public ZombiePriming primeZombieForDefault(AppRequest request, AppResponse response) {
        return httpClient.primeZombieForDefault(request, staticDefault(response));
    }

    @Override
    public List<PrimedMapping> getCurrentPriming() {
        return httpClient.getCurrentPriming();
    }

    @Override
    public List<ZombiePriming> getHistory() {
        return httpClient.getHistory();
    }

    @Override
    public void reset() {
        httpClient.reset();
    }

    public static void main(String[] args) {
        JzonbieHttpClient client = new JzonbieHttpClient("http://localhost:8080");

        final AppRequest zombieRequest = AppRequest.builder("POST", "/blah")
                .withBody(singletonMap("one", 1))
                .build();

        final AppResponse zombieResponse = AppResponse.builder(200)
                .withHeader("Content-Type", "application/json")
                .withBody(singletonMap("message", "Well done!"))
                .build();

        final List<PrimedMapping> currentPriming = client.getCurrentPriming();

        final PrimedMapping mapping = currentPriming.get(0);

        mapping.getAppRequest();

        currentPriming.clear();
    }
}