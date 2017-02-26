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
    private T defaultElement;

    public DefaultingQueue() {
        deque = new ArrayDeque<>();
    }

    public T poll() {
        final T dequeElement = deque.poll();
        return dequeElement == null ? defaultElement : dequeElement;
    }

    public void add(T element) {
        deque.add(element);
    }

    public void add(Collection<T> elements) {
        elements.forEach(element -> deque.add(element));
    }

    public int hasSize() {
        return deque.size();
    }

    public List<T> getEntries() {
        return Lists.newArrayList(deque.iterator());
    }

    public void setDefault(T defaultElement) {
        this.defaultElement = defaultElement;
    }

    public Optional<T> getDefault() {
        return ofNullable(defaultElement);
    }

    public void reset() {
        deque.clear();
        defaultElement = null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DefaultingQueue<?> that = (DefaultingQueue<?>) o;

        if (deque != null ? !deque.equals(that.deque) : that.deque != null) return false;
        return defaultElement != null ? defaultElement.equals(that.defaultElement) : that.defaultElement == null;
    }

    @Override
    public int hashCode() {
        int result = deque != null ? deque.hashCode() : 0;
        result = 31 * result + (defaultElement != null ? defaultElement.hashCode() : 0);
        return result;
    }
}