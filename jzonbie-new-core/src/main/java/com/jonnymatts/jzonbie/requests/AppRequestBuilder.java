package com.jonnymatts.jzonbie.requests;

import com.jonnymatts.jzonbie.Body;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jonnymatts.jzonbie.util.Cloner.cloneRequest;


public class AppRequestBuilder {
    private AppRequest request;

    public AppRequestBuilder(String method, String path) {
        this.request = new AppRequest();
        request.setPath(path);
        request.setMethod(method);
    }

    public AppRequest build() {
        return request;
    }

    public AppRequestBuilder withHeader(String name, String value) {
        request = cloneRequest(request);
        if(request.getHeaders() == null)
            request.setHeaders(new HashMap<>());
        request.getHeaders().put(name, value);
        return this;
    }

    public AppRequestBuilder withBody(Body<?> body) {
        request = cloneRequest(request);
        request.setBody(body);
        return this;
    }

    public AppRequestBuilder withQueryParam(String name, String value) {
        request = cloneRequest(request);
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
        request = cloneRequest(request);
        request.setBasicAuth(username, password);
        return this;
    }

    public AppRequestBuilder accept(String contentType) {
        return withHeader("Accept", contentType);
    }

    public AppRequestBuilder contentType(String contentType) {
        return withHeader("Content-Type", contentType);
    }
}