package com.jonnymatts.jzonbie.history;

import com.jonnymatts.jzonbie.requests.AppRequest;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class CallHistory {

    private final FixedCapacityCache<Exchange> exchangeCache;
    private final Map<AppRequest, Integer> successfulRequestCounter;

    public CallHistory(int capacity) {
        exchangeCache = new FixedCapacityCache<>(capacity);
        successfulRequestCounter = new ConcurrentHashMap<>();
    }

    public void add(AppRequest foundAppRequest, Exchange exchange) {
        incrementCounter(foundAppRequest);
        exchangeCache.add(exchange);
    }

    public int count(AppRequest appRequest) {
        return successfulRequestCounter.getOrDefault(appRequest, 0);
    }

    public List<Exchange> getValues() {
        return exchangeCache.getValues();
    }

    private void incrementCounter(AppRequest request) {
            if(successfulRequestCounter.get(request) == null) {
                successfulRequestCounter.put(request, 1);
            } else {
                successfulRequestCounter.compute(request, (request1, size) -> size + 1);
            }
    }

    public void clear() {
        successfulRequestCounter.clear();
        exchangeCache.clear();
    }
}