package com.jonnymatts.jzonbie.requests;

import com.jonnymatts.jzonbie.body.BodyContent;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jonnymatts.jzonbie.body.ArrayBodyContent.arrayBody;
import static com.jonnymatts.jzonbie.body.LiteralBodyContent.literalBody;
import static com.jonnymatts.jzonbie.body.ObjectBodyContent.objectBody;
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

    public AppRequestBuilder withBody(BodyContent<?> body) {
        request = cloneRequest(request);
        request.setBody(body);
        return this;
    }

    public AppRequestBuilder withBody(Map<String, ?> body) {
        request = cloneRequest(request);
        request.setBody(objectBody(body));
        return this;
    }

    public AppRequestBuilder withBody(String body) {
        request = cloneRequest(request);
        request.setBody(literalBody(body));
        return this;
    }

    public AppRequestBuilder withBody(List<?> body) {
        request = cloneRequest(request);
        request.setBody(arrayBody(body));
        return this;
    }

    public AppRequestBuilder withBody(Number body) {
        request = cloneRequest(request);
        request.setBody(literalBody(new BigDecimal(body.doubleValue())));
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