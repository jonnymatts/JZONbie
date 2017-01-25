package com.jonnymatts.jzonbie.requests;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.jonnymatts.jzonbie.model.*;
import com.jonnymatts.jzonbie.response.Response;

import java.util.List;
import java.util.Optional;

public class AppRequestHandler implements RequestHandler {

    private final PrimingContext primingContext;
    private final List<ZombiePriming> callHistory;
    private final AppRequestFactory appRequestFactory;

    public AppRequestHandler(PrimingContext primingContext,
                             List<ZombiePriming> callHistory,
                             AppRequestFactory appRequestFactory) {
        this.primingContext = primingContext;
        this.callHistory = callHistory;
        this.appRequestFactory = appRequestFactory;
    }

    @Override
    public Response handle(Request request) throws JsonProcessingException {
        final AppRequest zombieRequest = appRequestFactory.create(request);

        final Optional<AppResponse> primedResponseOpt = primingContext.getResponse(zombieRequest);

        if(!primedResponseOpt.isPresent()) {
            throw new PrimingNotFoundException(zombieRequest);
        }

        final AppResponse zombieResponse = primedResponseOpt.get();

        callHistory.add(new ZombiePriming(zombieRequest, zombieResponse));

        return zombieResponse;
    }
}