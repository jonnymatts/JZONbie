package com.jonnymatts.jzonbie.requests;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.jonnymatts.jzonbie.model.*;
import com.jonnymatts.jzonbie.response.Response;

import java.util.List;
import java.util.Optional;

public class AppRequestHandler implements RequestHandler {

    private final PrimingContext primingContext;
    private final CallHistory callHistory;
    private final List<AppRequest> failedRequests;
    private final AppRequestFactory appRequestFactory;

    public AppRequestHandler(PrimingContext primingContext,
                             CallHistory callHistory,
                             List<AppRequest> failedRequests,
                             AppRequestFactory appRequestFactory) {
        this.primingContext = primingContext;
        this.callHistory = callHistory;
        this.failedRequests = failedRequests;
        this.appRequestFactory = appRequestFactory;
    }

    @Override
    public Response handle(Request request) throws JsonProcessingException {
        final AppRequest appRequest = appRequestFactory.create(request);

        final Optional<AppResponse> primedResponseOpt = primingContext.getResponse(appRequest);

        if(!primedResponseOpt.isPresent()) {
            failedRequests.add(appRequest);
            throw new PrimingNotFoundException(appRequest);
        }

        final AppResponse zombieResponse = primedResponseOpt.get();

        callHistory.add(new ZombiePriming(appRequest, zombieResponse));

        return zombieResponse;
    }
}