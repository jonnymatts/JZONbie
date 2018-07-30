package com.jonnymatts.jzonbie.templating;

public class TransformResponseException extends RuntimeException {

    public TransformResponseException(Exception e) {
        super("Failed to transform response", e);
    }

    public TransformResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}