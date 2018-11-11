package com.jonnymatts.jzonbie.defaults;

import com.jonnymatts.jzonbie.model.AppRequest;
import com.jonnymatts.jzonbie.model.AppResponse;

public class StandardDefaultPriming extends DefaultPriming {

    private final AppRequest request;
    private final AppResponse response;

    public StandardDefaultPriming(AppRequest request, AppResponse response) {
        this.request = request;
        this.response = response;
    }

    public static StandardDefaultPriming defaultPriming(AppRequest request, AppResponse response) {
        return new StandardDefaultPriming(request, response);
    }

    public AppRequest getRequest() {
        return request;
    }

    public AppResponse getResponse() {
        return response;
    }
}
