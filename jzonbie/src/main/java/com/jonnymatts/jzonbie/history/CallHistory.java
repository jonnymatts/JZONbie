package com.jonnymatts.jzonbie.history;

import com.jonnymatts.jzonbie.requests.AppRequest;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.String.valueOf;


public class CallHistory {

    private final FixedCapacityCache<Exchange> messageHistoryCache;
    private final Map<AppRequest, Integer> successfulRequestCounter;
    private final PersistentRequestCount persistentRequestCount;

    public CallHistory(int capacity, File persistedFile) {
        messageHistoryCache = new FixedCapacityCache<>(capacity);
        successfulRequestCounter = new ConcurrentHashMap<>();
        persistentRequestCount = new PersistentRequestCount(persistedFile);
    }

    public CallHistorySnapshot add(AppRequest foundAppRequest, Exchange exchange) {
        int count = incrementCounter(foundAppRequest);
        int persistedCount = persistentRequestCount.incrementCounter(valueOf(foundAppRequest.hashCode()));
        messageHistoryCache.add(exchange);
        return CallHistorySnapshot.snapshot(count, persistedCount);
    }

    public int count(AppRequest appRequest) {
        return successfulRequestCounter.getOrDefault(appRequest, 0);
    }

    public List<Exchange> getValues() {
        return messageHistoryCache.getValues();
    }

    public int getPersistedCount(AppRequest request) {
        return persistentRequestCount.getCount(valueOf(request.hashCode())).orElse(0);
    }

    public void clear() {
        successfulRequestCounter.clear();
        messageHistoryCache.clear();
    }

    private int incrementCounter(AppRequest request) {
        if (successfulRequestCounter.get(request) == null) {
            successfulRequestCounter.put(request, 1);
            return 1;
        } else {
            return successfulRequestCounter.compute(request, (request1, size) -> size + 1);
        }
    }
}