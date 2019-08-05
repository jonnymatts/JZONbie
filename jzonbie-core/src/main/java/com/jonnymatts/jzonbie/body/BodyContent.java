package com.jonnymatts.jzonbie.body;

import com.jonnymatts.jzonbie.Body;

/**
 * Abstract class to define for request and response bodies.
 *
 * @param <T> the type of the body content
 */
public abstract class BodyContent<T> implements Body<T> {

    protected BodyContent() {}

    public abstract T getContent();
    public abstract BodyContentType getType();
    public abstract BodyContent<T> copy();

}