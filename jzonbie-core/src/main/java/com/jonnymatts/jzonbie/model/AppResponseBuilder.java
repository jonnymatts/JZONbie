package com.jonnymatts.jzonbie.model;

import com.jonnymatts.jzonbie.model.content.BodyContent;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jonnymatts.jzonbie.model.Cloner.cloneResponse;
import static com.jonnymatts.jzonbie.model.content.ArrayBodyContent.arrayBody;
import static com.jonnymatts.jzonbie.model.content.LiteralBodyContent.literalBody;
import static com.jonnymatts.jzonbie.model.content.ObjectBodyContent.objectBody;

public class AppResponseBuilder {
    private AppResponse response;

    AppResponseBuilder(int statusCode) {
        this.response = new AppResponse();
        response.setStatusCode(statusCode);
    }

    public AppResponse build() {
        return response;
    }

    public AppResponseBuilder withHeader(String name, String value) {
        response = cloneResponse(response);
        if(response.getHeaders() == null)
            response.setHeaders(new HashMap<>());
        response.getHeaders().put(name, value);
        return this;
    }

    public AppResponseBuilder withBody(Map<String, ?> body) {
        response = cloneResponse(response);
        response.setBody(objectBody(body));
        return this;
    }

    public AppResponseBuilder withBody(String body) {
        response = cloneResponse(response);
        response.setBody(literalBody(body));
        return this;
    }

    public AppResponseBuilder withBody(List<?> body) {
        response = cloneResponse(response);
        response.setBody(arrayBody(body));
        return this;
    }


    public AppResponseBuilder withBody(Number body) {
        response = cloneResponse(response);
        response.setBody(literalBody(new BigDecimal(body.doubleValue())));
        return this;
    }

    public AppResponseBuilder withBody(BodyContent body) {
        response = cloneResponse(response);
        response.setBody(body);
        return this;
    }

    public AppResponseBuilder withDelay(Duration delay) {
        response = cloneResponse(response);
        response.setDelay(delay);
        return this;
    }

    public AppResponseBuilder contentType(String contentType) {
        return withHeader("Content-Type", contentType);
    }
}