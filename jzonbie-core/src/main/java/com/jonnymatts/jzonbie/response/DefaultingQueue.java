package com.jonnymatts.jzonbie.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.Lists;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;

@JsonSerialize(using = DefaultingQueueSerializer.class)
@JsonDeserialize(using = DefaultingQueueDeserializer.class)
public class DefaultingQueue<T> {

    private final ArrayDeque<T> deque;
    private DefaultResponse<T> defaultResponse;

    public DefaultingQueue() {
        deque = new ArrayDeque<>();
    }

    public T poll() {
        final T dequeElement = deque.poll();
        return (dequeElement == null && defaultResponse != null) ? defaultResponse.getResponse() : dequeElement;
    }

    public void add(T element) {
        deque.add(element);
    }

    public void add(Collection<T> elements) {
        elements.forEach(deque::add);
    }

    public int hasSize() {
        return deque.size();
    }

    public List<T> getEntries() {
        return Lists.newArrayList(deque.iterator());
    }

    public void setDefault(DefaultResponse<T> defaultResponse) {
        this.defaultResponse = defaultResponse;
    }

    public Optional<DefaultResponse<T>> getDefault() {
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

        DefaultingQueue<?> that = (DefaultingQueue<?>) o;

        if (deque != null ? !deque.equals(that.deque) : that.deque != null) return false;
        return defaultResponse != null ? defaultResponse.equals(that.defaultResponse) : that.defaultResponse == null;
    }

    @Override
    public int hashCode() {
        int result = deque != null ? deque.hashCode() : 0;
        result = 31 * result + (defaultResponse != null ? defaultResponse.hashCode() : 0);
        return result;
    }
}