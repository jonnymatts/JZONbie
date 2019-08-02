package com.jonnymatts.jzonbie.requests;


import com.jonnymatts.jzonbie.Request;
import com.jonnymatts.jzonbie.Response;
import com.jonnymatts.jzonbie.history.CallHistory;
import com.jonnymatts.jzonbie.history.Exchange;
import com.jonnymatts.jzonbie.history.FixedCapacityCache;
import com.jonnymatts.jzonbie.priming.AppRequestFactory;
import com.jonnymatts.jzonbie.priming.PrimingContext;
import com.jonnymatts.jzonbie.responses.AppResponse;

import java.util.Optional;

public class AppRequestHandler implements RequestHandler {

    private final PrimingContext primingContext;
    private final CallHistory callHistory;
    private final FixedCapacityCache<AppRequest> failedRequests;
    private final AppRequestFactory appRequestFactory;

    public AppRequestHandler(PrimingContext primingContext,
                             CallHistory callHistory,
                             FixedCapacityCache<AppRequest> failedRequests,
                             AppRequestFactory appRequestFactory) {
        this.primingContext = primingContext;
        this.callHistory = callHistory;
        this.failedRequests = failedRequests;
        this.appRequestFactory = appRequestFactory;
    }

    @Override
    public Response handle(Request request) {
        final AppRequest appRequest = appRequestFactory.create(request);

        final Optional<AppResponse> primedResponseOpt = primingContext.getResponse(appRequest);

        if(!primedResponseOpt.isPresent()) {
            failedRequests.add(appRequest);
            throw new PrimingNotFoundException(appRequest);
        }

        final AppResponse zombieResponse = primedResponseOpt.get();

        callHistory.add(new Exchange(appRequest, zombieResponse));

        return zombieResponse;
    }
}