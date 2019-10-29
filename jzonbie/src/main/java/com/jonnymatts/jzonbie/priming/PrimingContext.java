package com.jonnymatts.jzonbie.priming;

import com.jonnymatts.jzonbie.defaults.DefaultResponsePriming;
import com.jonnymatts.jzonbie.defaults.Priming;
import com.jonnymatts.jzonbie.defaults.StandardPriming;
import com.jonnymatts.jzonbie.requests.AppRequest;
import com.jonnymatts.jzonbie.responses.AppResponse;
import com.jonnymatts.jzonbie.responses.defaults.DefaultAppResponse;
import com.jonnymatts.jzonbie.responses.defaults.DefaultingQueue;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Optional.empty;

public class PrimingContext {
    private final List<Priming> priming;
    private Map<AppRequest, DefaultingQueue> primedMappings;

    public PrimingContext(List<Priming> priming) {
        this.priming = priming;
        this.primedMappings = new ConcurrentHashMap<>();
        addDefaultPriming();
    }

    public PrimingContext() {
        this(emptyList());
    }

    public List<PrimedMapping> getCurrentPriming() {
        return primedMappings.entrySet().stream()
                .map(e -> new PrimedMapping(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    public PrimingContext add(ZombiePriming zombiePriming) {
        return add(zombiePriming.getRequest(), zombiePriming.getResponse());
    }

    public PrimingContext add(AppRequest appRequest, AppResponse appResponse) {
        getAppResponseQueueForAdd(appRequest).add(appResponse);

        return this;
    }

    public PrimingContext addDefault(AppRequest appRequest, DefaultAppResponse defaultAppResponse) {
        getAppResponseQueueForAdd(appRequest).setDefault(defaultAppResponse);

        return this;
    }

    public Optional<AppRequest> getPrimedRequest(AppRequest appRequest) {
        return primedMappings.entrySet().parallelStream()
                .filter(priming -> priming.getKey().matches(appRequest))
                .map(Map.Entry::getKey)
                .findFirst();
    }

    public Optional<AppResponse> getResponse(AppRequest appRequest) {
        final DefaultingQueue primedResponsesQueue = primedMappings.get(appRequest);

        if (primedResponsesQueue == null) {
            return empty();
        }

        final AppResponse appResponse = primedResponsesQueue.poll();

        if (primedResponsesQueue.hasSize() == 0 && !primedResponsesQueue.getDefault().isPresent()) {
            primedMappings.remove(appRequest);
        }

        return Optional.ofNullable(appResponse);
    }

    public void reset() {
        primedMappings.clear();
        addDefaultPriming();
    }

    private void addDefaultPriming() {
        for (Priming priming : priming) {
            if (priming instanceof StandardPriming) {
                final StandardPriming defaultPriming = (StandardPriming) priming;
                add(defaultPriming.getRequest(), defaultPriming.getResponse());
            } else {
                final DefaultResponsePriming defaultPriming = (DefaultResponsePriming) priming;
                addDefault(defaultPriming.getRequest(), defaultPriming.getResponse());
            }
        }
    }

    private DefaultingQueue getAppResponseQueueForAdd(AppRequest appRequest) {
        if (primedMappings.get(appRequest) == null) {
            primedMappings.put(appRequest, new DefaultingQueue());
            return primedMappings.get(appRequest);
        } else {
            return primedMappings.get(appRequest);
        }
    }
}