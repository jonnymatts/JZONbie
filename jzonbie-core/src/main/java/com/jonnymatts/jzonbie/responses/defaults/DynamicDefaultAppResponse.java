package com.jonnymatts.jzonbie.responses.defaults;

import com.jonnymatts.jzonbie.responses.AppResponse;

import java.util.function.Supplier;

import static com.jonnymatts.jzonbie.responses.defaults.DefaultAppResponseType.DYNAMIC;

public class DynamicDefaultAppResponse extends DefaultAppResponse {
    private Supplier<AppResponse> supplier;

    public DynamicDefaultAppResponse(Supplier<AppResponse> supplier) {
        this.supplier = supplier;
    }

    @Override
    public AppResponse getResponse() {
        return supplier.get();
    }

    @Override
    public DefaultAppResponseType getType() {
        return DYNAMIC;
    }

    private String serialize() {
        throw new UnsupportedOperationException("Cannot serialize dynamic default app responses.");
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

    @Override
    public String toString() {
        return "DynamicDefaultAppResponse{" +
                "supplier=" + supplier +
                '}';
    }
}