package com.jonnymatts.jzonbie.testing;

import com.jonnymatts.jzonbie.Body;

public class StringBody implements Body<String> {
    private String content;

    public StringBody(String content) {
        this.content = content;
    }

    @Override
    public String getContent() {
        return content;
    }

    @Override
    public boolean matches(Body<?> other) {
        return content.equals(other.getContent());
    }

    @Override
    public Body<String> copy() {
        return this;
    }
}