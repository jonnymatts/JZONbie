package com.jonnymatts.jzonbie.defaults;


import com.jonnymatts.jzonbie.requests.AppRequest;
import com.jonnymatts.jzonbie.responses.AppResponse;

public class StandardPriming extends Priming {

    private final AppRequest request;
    private final AppResponse response;

    public StandardPriming(AppRequest request, AppResponse response) {
        this.request = request;
        this.response = response;
    }

    public static StandardPriming priming(AppRequest request, AppResponse response) {
        return new StandardPriming(request, response);
    }

    public AppRequest getRequest() {
        return request;
    }

    public AppResponse getResponse() {
        return response;
    }
}
