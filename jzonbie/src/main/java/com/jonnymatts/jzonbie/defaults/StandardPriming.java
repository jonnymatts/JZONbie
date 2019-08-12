package com.jonnymatts.jzonbie.defaults;


import com.jonnymatts.jzonbie.requests.AppRequest;
import com.jonnymatts.jzonbie.responses.AppResponse;

/**
 * One-time priming added on Jzonbie and reset.
 */
public class StandardPriming extends Priming {

    private final AppRequest request;
    private final AppResponse response;

    private StandardPriming(AppRequest request, AppResponse response) {
        this.request = request;
        this.response = response;
    }

    /**
     * Returns a one-time priming to customize a Jzonbie.
     *
     * @param request  the request to match against
     * @param response the response this Jzonbie will return
     * @return one time priming
     */
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
