package com.jonnymatts.jzonbie.requests;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Multimap;
import com.jonnymatts.jzonbie.model.AppRequest;
import com.jonnymatts.jzonbie.model.AppRequestFactory;
import com.jonnymatts.jzonbie.model.AppResponse;
import com.jonnymatts.jzonbie.model.ZombiePriming;
import com.jonnymatts.jzonbie.response.Response;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class AppRequestHandler implements RequestHandler {

    private final Multimap<AppRequest, AppResponse> primingContext;
    private final List<ZombiePriming> callHistory;
    private final AppRequestFactory appRequestFactory;

    public AppRequestHandler(Multimap<AppRequest, AppResponse> primingContext,
                             List<ZombiePriming> callHistory,
                             AppRequestFactory appRequestFactory) {
        this.primingContext = primingContext;
        this.callHistory = callHistory;
        this.appRequestFactory = appRequestFactory;
    }

    @Override
    public Response handle(Request request) throws JsonProcessingException {
        final AppRequest zombieRequest = appRequestFactory.create(request);

        final Collection<AppResponse> zombieResponses = primingContext.get(zombieRequest);
        final Optional<AppResponse> primedResponseOpt = zombieResponses.stream().findFirst();

        if(!primedResponseOpt.isPresent()) {
            throw new PrimingNotFoundException(zombieRequest);
        }

        final AppResponse zombieResponse = primedResponseOpt.get();

        primingContext.remove(zombieRequest, zombieResponse);

        callHistory.add(new ZombiePriming(zombieRequest, zombieResponse));

        return zombieResponse;
    }
}