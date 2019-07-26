package com.jonnymatts.jzonbie.defaults;

import com.jonnymatts.jzonbie.requests.AppRequest;
import com.jonnymatts.jzonbie.responses.defaults.DefaultAppResponse;

public class DefaultResponsePriming extends Priming {

    private final AppRequest request;
    private final DefaultAppResponse response;

    public DefaultResponsePriming(AppRequest request, DefaultAppResponse response) {
        this.request = request;
        this.response = response;
    }

    public static DefaultResponsePriming defaultPriming(AppRequest request, DefaultAppResponse response) {
        return new DefaultResponsePriming(request, response);
    }

    public AppRequest getRequest() {
        return request;
    }

    public DefaultAppResponse getResponse() {
        return response;
    }
}
