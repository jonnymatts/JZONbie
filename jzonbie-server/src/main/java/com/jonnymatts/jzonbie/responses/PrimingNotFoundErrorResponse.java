package com.jonnymatts.jzonbie.responses;

import com.jonnymatts.jzonbie.priming.AppRequest;

public class PrimingNotFoundErrorResponse extends ErrorResponse {
    private final AppRequest request;

    public PrimingNotFoundErrorResponse(AppRequest incomingRequest) {
        super("Priming not found for request");
        this.request = incomingRequest;
    }

    public AppRequest getRequest() {
        return request;
    }
}
