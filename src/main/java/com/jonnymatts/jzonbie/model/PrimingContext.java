package com.jonnymatts.jzonbie.model;

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
                        new ArrayList<AppResponse>(){{
                            add(zombiePriming.getAppResponse());
                        }}
                )
        );
        return this;
    }

    public PrimingContext add(AppRequest appRequest, AppResponse appResponse) {
        return add(new ZombiePriming(appRequest, appResponse));
    }

    public Optional<AppResponse> getResponse(AppRequest appRequest) {
        return findPrimedMappingForRequest(appRequest).flatMap(primedMapping -> {
            final List<AppResponse> appResponses = primedMapping.getAppResponses();
            return appResponses.stream()
                    .findFirst()
                    .map(response -> removeResponseFromPriming(response, primedMapping));
        });
    }

    private AppResponse removeResponseFromPriming(AppResponse appResponse, PrimedMapping primedMapping) {
        final List<AppResponse> appResponses = primedMapping.getAppResponses();

        appResponses.remove(appResponse);

        if(appResponses.size() < 1)
            primedMappings.remove(primedMapping);

        return appResponse;
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