package com.jonnymatts.jzonbie.defaults;

import com.jonnymatts.jzonbie.requests.AppRequest;
import com.jonnymatts.jzonbie.responses.DefaultAppResponse;

public class DefaultResponseDefaultPriming extends DefaultPriming {

    private final AppRequest request;
    private final DefaultAppResponse response;

    public DefaultResponseDefaultPriming(AppRequest request, DefaultAppResponse response) {
        this.request = request;
        this.response = response;
    }

    public static DefaultResponseDefaultPriming defaultResponseDefaultPriming(AppRequest request, DefaultAppResponse response) {
        return new DefaultResponseDefaultPriming(request, response);
    }

    public AppRequest getRequest() {
        return request;
    }

    public DefaultAppResponse getResponse() {
        return response;
    }
}
