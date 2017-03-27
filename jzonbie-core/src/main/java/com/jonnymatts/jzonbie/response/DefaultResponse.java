package com.jonnymatts.jzonbie.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.function.Supplier;

public abstract class DefaultResponse<T> {

    private DefaultResponse() {}

    public abstract T getResponse();

    @JsonIgnore
    public abstract boolean isDynamic();

    public static class StaticDefaultResponse<T> extends DefaultResponse<T> {
        private T response;

        public StaticDefaultResponse(@JsonProperty("response") T response) {
            this.response = response;
        }

        @Override
        public T getResponse() {
            return response;
        }

        @Override
        public boolean isDynamic() {
            return false;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            StaticDefaultResponse<?> that = (StaticDefaultResponse<?>) o;

            return response != null ? response.equals(that.response) : that.response == null;
        }

        @Override
        public int hashCode() {
            return response != null ? response.hashCode() : 0;
        }
    }

    public static class DynamicDefaultResponse<T> extends DefaultResponse<T> {
        private Supplier<T> supplier;

        public DynamicDefaultResponse(Supplier<T> supplier) {
            this.supplier = supplier;
        }

        @Override
        public T getResponse() {
            return supplier.get();
        }

        @Override
        public boolean isDynamic() {
            return true;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            DynamicDefaultResponse<?> that = (DynamicDefaultResponse<?>) o;

            return supplier != null ? supplier.equals(that.supplier) : that.supplier == null;
        }

        @Override
        public int hashCode() {
            return supplier != null ? supplier.hashCode() : 0;
        }
    }
}