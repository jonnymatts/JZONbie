package com.jonnymatts.jzonbie.requests;

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
