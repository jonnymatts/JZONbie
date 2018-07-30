package com.jonnymatts.jzonbie.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.jonnymatts.jzonbie.model.AppResponse;
import com.jonnymatts.jzonbie.model.TemplatedAppResponse;

import java.util.function.Supplier;

public abstract class DefaultAppResponse {

    private DefaultAppResponse() {}

    public abstract AppResponse getResponse();

    @JsonIgnore
    public abstract boolean isDynamic();

    @JsonIgnore
    public boolean isTemplated() {
        return getResponse() instanceof TemplatedAppResponse;
    }

    public static class StaticDefaultAppResponse extends DefaultAppResponse {
        private AppResponse response;

        public StaticDefaultAppResponse(@JsonProperty("response") AppResponse response) {
            this.response = response;
        }
        
        public static StaticDefaultAppResponse staticDefault(AppResponse response) {
            return new StaticDefaultAppResponse(response);
        }

        @Override
        public AppResponse getResponse() {
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

            StaticDefaultAppResponse that = (StaticDefaultAppResponse) o;

            return response != null ? response.equals(that.response) : that.response == null;
        }

        @Override
        public int hashCode() {
            return response != null ? response.hashCode() : 0;
        }
    }

    public static class DynamicDefaultAppResponse extends DefaultAppResponse {
        private Supplier<AppResponse> supplier;

        public DynamicDefaultAppResponse(Supplier<AppResponse> supplier) {
            this.supplier = supplier;
        }

        public static DynamicDefaultAppResponse dynamicDefault(Supplier<AppResponse> supplier) {
            return new DynamicDefaultAppResponse(supplier);
        }

        @Override
        public AppResponse getResponse() {
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

            DynamicDefaultAppResponse that = (DynamicDefaultAppResponse) o;

            return supplier != null ? supplier.equals(that.supplier) : that.supplier == null;
        }

        @Override
        public int hashCode() {
            return supplier != null ? supplier.hashCode() : 0;
        }
    }
}