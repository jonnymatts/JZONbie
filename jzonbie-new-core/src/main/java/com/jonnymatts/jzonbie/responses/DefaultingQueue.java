package com.jonnymatts.jzonbie.responses;

import com.google.common.collect.Lists;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;

public class DefaultingQueue {

    private final ArrayDeque<AppResponse> deque;
    private DefaultAppResponse defaultResponse;

    public DefaultingQueue() {
        deque = new ArrayDeque<>();
    }

    public AppResponse poll() {
        final AppResponse dequeElement = deque.poll();
        return (dequeElement == null && defaultResponse != null) ? defaultResponse.getResponse() : dequeElement;
    }

    public void add(AppResponse element) {
        deque.add(element);
    }

    public void add(Collection<AppResponse> elements) {
        deque.addAll(elements);
    }

    public int hasSize() {
        return deque.size();
    }

    public List<AppResponse> getEntries() {
        return Lists.newArrayList(deque.iterator());
    }

    public void setDefault(DefaultAppResponse defaultAppResponse) {
        this.defaultResponse = defaultAppResponse;
    }

    public Optional<DefaultAppResponse> getDefault() {
        return ofNullable(defaultResponse);
    }

    public void reset() {
        deque.clear();
        defaultResponse = null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DefaultingQueue that = (DefaultingQueue) o;

        if (deque != null ? !dequesAreEqual(that) : that.deque != null) return false;
        return defaultResponse != null ? defaultResponse.equals(that.defaultResponse) : that.defaultResponse == null;
    }

    private boolean dequesAreEqual(DefaultingQueue that) {
        return asList(deque.toArray()).equals(asList(that.deque.toArray()));
    }

    @Override
    public int hashCode() {
        int result = deque != null ? deque.hashCode() : 0;
        result = 31 * result + (defaultResponse != null ? defaultResponse.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DefaultingQueue{" +
                "deque=" + deque +
                ", defaultResponse=" + defaultResponse +
                '}';
    }
}