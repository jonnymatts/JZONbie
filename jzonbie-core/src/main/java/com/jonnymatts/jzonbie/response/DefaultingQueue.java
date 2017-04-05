package com.jonnymatts.jzonbie.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.Lists;
import com.jonnymatts.jzonbie.model.AppResponse;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;

@JsonSerialize(using = DefaultingQueueSerializer.class)
@JsonDeserialize(using = DefaultingQueueDeserializer.class)
public class DefaultingQueue {

    private final ArrayDeque<AppResponse> deque;
    private DefaultAppResponse defaultAppResponse;

    public DefaultingQueue() {
        deque = new ArrayDeque<>();
    }

    public AppResponse poll() {
        final AppResponse dequeElement = deque.poll();
        return (dequeElement == null && defaultAppResponse != null) ? defaultAppResponse.getResponse() : dequeElement;
    }

    public void add(AppResponse element) {
        deque.add(element);
    }

    public void add(Collection<AppResponse> elements) {
        elements.forEach(deque::add);
    }

    public int hasSize() {
        return deque.size();
    }

    public List<AppResponse> getEntries() {
        return Lists.newArrayList(deque.iterator());
    }

    public void setDefault(DefaultAppResponse defaultAppResponse) {
        this.defaultAppResponse = defaultAppResponse;
    }

    public Optional<DefaultAppResponse> getDefault() {
        return ofNullable(defaultAppResponse);
    }

    public void reset() {
        deque.clear();
        defaultAppResponse = null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DefaultingQueue that = (DefaultingQueue) o;

        if (deque != null ? !deque.equals(that.deque) : that.deque != null) return false;
        return defaultAppResponse != null ? defaultAppResponse.equals(that.defaultAppResponse) : that.defaultAppResponse == null;
    }

    @Override
    public int hashCode() {
        int result = deque != null ? deque.hashCode() : 0;
        result = 31 * result + (defaultAppResponse != null ? defaultAppResponse.hashCode() : 0);
        return result;
    }
}