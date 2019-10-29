package com.jonnymatts.jzonbie.history;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

public class FixedCapacityCache<T> {

    private final int capacity;
    private ConcurrentLinkedDeque<T> values;

    public FixedCapacityCache(int capacity) {
        this.capacity = capacity;
        this.values = new ConcurrentLinkedDeque<>();
    }

    @JsonValue
    public List<T> getValues() {
        return new ArrayList<>(values);
    }

    public void add(T value) {
        if(values.size() == capacity) {
            values.removeFirst();
        }
        values.add(value);
    }

    public void clear() {
        values.clear();
    }
}
