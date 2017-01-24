package com.jonnymatts.jzonbie.response;

import com.jonnymatts.jzonbie.model.ZombieRequest;

public class PrimingNotFoundErrorResponse extends ErrorResponse {
    private final ZombieRequest request;

    public PrimingNotFoundErrorResponse(ZombieRequest incomingRequest) {
        super("Priming not found for request");
        this.request = incomingRequest;
    }

    public ZombieRequest getRequest() {
        return request;
    }
}
