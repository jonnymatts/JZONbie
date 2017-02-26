package com.jonnymatts.jzonbie.model;

import com.jonnymatts.jzonbie.response.DefaultingQueue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PrimingContext {
    private List<PrimedMapping> primedMappings;

    public PrimingContext() {
        this.primedMappings = new ArrayList<>();
    }

    public PrimingContext(List<PrimedMapping> primedMappings) {
        this.primedMappings = primedMappings;
    }

    public List<PrimedMapping> getCurrentPriming() {
        return primedMappings;
    }

    public PrimingContext add(ZombiePriming zombiePriming) {
        final Optional<PrimedMapping> existingPrimedMappingForAppRequest = findPrimedMappingForRequest(zombiePriming.getAppRequest());

        if (existingPrimedMappingForAppRequest.isPresent()) {
            existingPrimedMappingForAppRequest.get().getAppResponses().add(zombiePriming.getAppResponse());
            return this;
        }

        primedMappings.add(
                new PrimedMapping(
                        zombiePriming.getAppRequest(),
                        new DefaultingQueue<AppResponse>(){{
                            add(zombiePriming.getAppResponse());
                        }}
                )
        );
        return this;
    }

    public PrimingContext add(AppRequest appRequest, AppResponse appResponse) {
        return add(new ZombiePriming(appRequest, appResponse));
    }

    public PrimingContext addDefault(ZombiePriming zombiePriming) {
        final Optional<PrimedMapping> existingPrimedMappingForAppRequest = findPrimedMappingForRequest(zombiePriming.getAppRequest());

        if (existingPrimedMappingForAppRequest.isPresent()) {
            existingPrimedMappingForAppRequest.get().getAppResponses().setDefault(zombiePriming.getAppResponse());
            return this;
        }

        primedMappings.add(
                new PrimedMapping(
                        zombiePriming.getAppRequest(),
                        new DefaultingQueue<AppResponse>(){{
                            setDefault(zombiePriming.getAppResponse());
                        }}
                )
        );
        return this;
    }

    public PrimingContext addDefault(AppRequest appRequest, AppResponse appResponse) {
        return addDefault(new ZombiePriming(appRequest, appResponse));
    }

    public Optional<AppResponse> getResponse(AppRequest appRequest) {
        final Optional<PrimedMapping> primedMappingOpt = findPrimedMappingForRequest(appRequest);
        return primedMappingOpt.map(primedMapping -> {
            final DefaultingQueue<AppResponse> appResponses = primedMapping.getAppResponses();
            final AppResponse appResponse = appResponses.poll();
            if(appResponses.hasSize() == 0 && !appResponses.getDefault().isPresent()) primedMappings.remove(primedMapping);
            return appResponse;
        });
    }

    private Optional<PrimedMapping> findPrimedMappingForRequest(AppRequest appRequest) {
        return primedMappings.stream()
                .filter(priming -> priming.getAppRequest().matches(appRequest))
                .findFirst();
    }

    public void clear() {
        primedMappings.clear();
    }
}