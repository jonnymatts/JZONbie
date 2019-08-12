package com.jonnymatts.jzonbie.defaults;

import com.jonnymatts.jzonbie.requests.AppRequest;
import com.jonnymatts.jzonbie.responses.defaults.DefaultAppResponse;

/**
 * Default priming added on Jzonbie and reset.
 */
public class DefaultResponsePriming extends Priming {

    private final AppRequest request;
    private final DefaultAppResponse response;

    private DefaultResponsePriming(AppRequest request, DefaultAppResponse response) {
        this.request = request;
        this.response = response;
    }

    /**
     * Returns a default priming to customize a Jzonbie.
     *
     * @param request  the request to match against
     * @param response the response this Jzonbie will return
     * @return default priming
     */
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
