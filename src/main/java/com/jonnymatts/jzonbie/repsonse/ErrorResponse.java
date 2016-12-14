package com.jonnymatts.jzonbie.repsonse;

import com.jonnymatts.jzonbie.model.PrimedRequest;

public class ErrorResponse {
    private final String message;
    private final PrimedRequest request;

    public ErrorResponse(String message, PrimedRequest request) {
        this.message = message;
        this.request = request;
    }

    public String getMessage() {
        return message;
    }

    public PrimedRequest getRequest() {
        return request;
    }
}
