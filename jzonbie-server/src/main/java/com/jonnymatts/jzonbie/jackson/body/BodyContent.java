package com.jonnymatts.jzonbie.jackson.body;

import com.jonnymatts.jzonbie.Body;

public abstract class BodyContent<T> implements Body<T> {

    public static final String TYPE_IDENTIFIER = "JZONBIE_CONTENT_TYPE";

    protected BodyContent() {}

    public abstract T getContent();
    public abstract BodyContentType getType();

}