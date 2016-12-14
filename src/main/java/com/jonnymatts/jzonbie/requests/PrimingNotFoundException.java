package com.jonnymatts.jzonbie.requests;

import com.jonnymatts.jzonbie.model.ZombieRequest;

public class PrimingNotFoundException extends RuntimeException {
    private final ZombieRequest request;

    public PrimingNotFoundException(ZombieRequest request) {
        this.request = request;
    }

    public ZombieRequest getRequest() {
        return request;
    }
}
