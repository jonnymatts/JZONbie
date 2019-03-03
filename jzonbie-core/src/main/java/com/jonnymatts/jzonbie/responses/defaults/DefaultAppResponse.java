package com.jonnymatts.jzonbie.responses.defaults;

import com.jonnymatts.jzonbie.responses.AppResponse;

import java.util.function.Supplier;

public abstract class DefaultAppResponse {

    DefaultAppResponse() {}

    public abstract AppResponse getResponse();

    public static StaticDefaultAppResponse staticDefault(AppResponse response) {
        return new StaticDefaultAppResponse(response);
    }

    public static DynamicDefaultAppResponse dynamicDefault(Supplier<AppResponse> supplier) {
        return new DynamicDefaultAppResponse(supplier);
    }

    public abstract DefaultAppResponseType getType();
}