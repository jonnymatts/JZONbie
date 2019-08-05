package com.jonnymatts.jzonbie.responses.defaults;

import com.jonnymatts.jzonbie.responses.AppResponse;

import java.util.function.Supplier;

/**
 * Base class for responses that Jzonbie can be primed to respond with by default.
 */
public abstract class DefaultAppResponse {

    DefaultAppResponse() {}

    public abstract AppResponse getResponse();

    /**
     * Returns a default response that always returns the input response.
     *
     * @param response response to always return
     * @return default response that will never change
     */
    public static StaticDefaultAppResponse staticDefault(AppResponse response) {
        return new StaticDefaultAppResponse(response);
    }

    /**
     * Returns a default response that returns a response defined by the supplier.
     * <p>
     * Cannot be used when communicating with Jzonbie over HTTP.
     *
     * @param supplier response provider
     * @return default response that can change
     */
    public static DynamicDefaultAppResponse dynamicDefault(Supplier<AppResponse> supplier) {
        return new DynamicDefaultAppResponse(supplier);
    }

    public abstract DefaultAppResponseType getType();
}