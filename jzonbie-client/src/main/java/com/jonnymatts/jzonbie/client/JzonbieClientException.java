package com.jonnymatts.jzonbie.client;

public class JzonbieClientException extends RuntimeException {

    public JzonbieClientException(String message) {
        super(message);
    }

    public JzonbieClientException(String message, Exception e) {
        super(message, e);
    }
}