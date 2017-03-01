package com.jonnymatts.jzonbie.requests;

import com.jonnymatts.jzonbie.model.AppRequest;

public class PrimingNotFoundException extends RuntimeException {
    private final AppRequest request;

    public PrimingNotFoundException(AppRequest request) {
        super(request.toString());
        this.request = request;
    }

    public AppRequest getRequest() {
        return request;
    }
}
