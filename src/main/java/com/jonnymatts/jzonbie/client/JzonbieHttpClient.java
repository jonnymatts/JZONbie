package com.jonnymatts.jzonbie.client;

import com.jonnymatts.jzonbie.model.AppRequest;
import com.jonnymatts.jzonbie.model.AppResponse;
import com.jonnymatts.jzonbie.model.PrimedMapping;
import com.jonnymatts.jzonbie.model.ZombiePriming;

import java.util.List;

import static java.util.Collections.singletonMap;

public class JzonbieHttpClient {

    private final JzonbieClient httpClient;

    public JzonbieHttpClient(String zombieBaseUrl) {
        this.httpClient = new ApacheJzonbieHttpClient(zombieBaseUrl);
    }

    public JzonbieHttpClient(JzonbieClient httpClient) {
        this.httpClient = httpClient;
    }

    public ZombiePriming primeZombie(AppRequest request, AppResponse response) {
        return httpClient.primeZombie(request, response);
    }

    public List<PrimedMapping> getCurrentPriming() {
        return httpClient.getCurrentPriming();
    }

    public List<ZombiePriming> getHistory() {
        return httpClient.getHistory();
    }

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