package com.jonnymatts.jzonbie.body;

import com.jonnymatts.jzonbie.Body;

public abstract class BodyContent<T> implements Body<T> {

    protected BodyContent() {}

    public abstract T getContent();
    public abstract BodyContentType getType();
    public abstract BodyContent<T> copy();

}