package com.jonnymatts.jzonbie.util;

import com.jonnymatts.jzonbie.body.BodyContent;
import com.jonnymatts.jzonbie.requests.AppRequest;
import com.jonnymatts.jzonbie.responses.AppResponse;

import java.util.HashMap;
import java.util.Map;


public class Cloner {

    public static AppRequest cloneRequest(AppRequest appRequest) {
        final AppRequest copy = new AppRequest();
        copy.setPath(appRequest.getPath());
        copy.setMethod(appRequest.getMethod());
        copy.setQueryParams(copyMap(appRequest.getQueryParams()));
        copy.setHeaders(copyMap(appRequest.getHeaders()));
        copy.setBody(copyBodyContent(appRequest.getBody()));
        return copy;
    }

    public static AppResponse cloneResponse(AppResponse appResponse) {
        final AppResponse copy = new AppResponse();
        copy.setStatusCode(appResponse.getStatusCode());
        copy.setHeaders(copyMap(appResponse.getHeaders()));
        copy.setBody(copyBodyContent(appResponse.getBody()));
        appResponse.getDelay().ifPresent(copy::setDelay);
        copy.setTemplated(appResponse.isTemplated());
        return copy;
    }

    private static <K, V> HashMap<K, V> copyMap(Map<K, V> map) {
        return map == null ? null : new HashMap<>(map);
    }

    private static BodyContent<?> copyBodyContent(BodyContent<?> body) {
        if(body == null) return null;
        return body.copy();
    }
}