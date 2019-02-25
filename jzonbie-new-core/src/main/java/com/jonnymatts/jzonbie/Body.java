package com.jonnymatts.jzonbie;

public interface Body<T> {

    T getContent();

    boolean matches(Body<?> other);

    Body<T> copy();

}