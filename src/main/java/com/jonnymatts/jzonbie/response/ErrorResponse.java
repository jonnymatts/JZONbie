package com.jonnymatts.jzonbie.response;

public class ErrorResponse {
    protected final String message;

    public ErrorResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
