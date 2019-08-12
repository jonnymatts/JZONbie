package com.jonnymatts.jzonbie.client;

/**
 * Thrown to indicate that an error has occurred when communicating
 * with the Jzonbie over HTTP.
 */
public class JzonbieClientException extends RuntimeException {

    public JzonbieClientException(String message) {
        super(message);
    }

    public JzonbieClientException(String message, Exception e) {
        super(message, e);
    }
}