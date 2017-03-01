package com.jonnymatts.jzonbie.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppRequestBuilder {
    private final AppRequest request;

    public AppRequestBuilder(String method, String path) {
        this.request = new AppRequest();
        request.setPath(path);
        request.setMethod(method);
    }

    public AppRequest build() {
        return request;
    }

    public AppRequestBuilder withHeader(String name, String value) {
        if(request.getHeaders() == null)
            request.setHeaders(new HashMap<>());
        request.getHeaders().put(name, value);
        return this;
    }

    public AppRequestBuilder withBody(Map<String, ?> body) {
        request.setBody(body);
        return this;
    }

    public AppRequestBuilder withQueryParam(String name, String value) {
        Map<String, List<String>> queryParams = request.getQueryParams();
        if (queryParams == null) {
            queryParams = new HashMap<>();
            request.setQueryParams(queryParams);
        }

        if (!queryParams.containsKey(name)) {
            queryParams.put(name, new ArrayList<>());
        }

        queryParams.get(name).add(value);

        return this;
    }

    public AppRequestBuilder withBasicAuth(String username, String password) {
        request.setBasicAuth(username, password);
        return this;
    }
}