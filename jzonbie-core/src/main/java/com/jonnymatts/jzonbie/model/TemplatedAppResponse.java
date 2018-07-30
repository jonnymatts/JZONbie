package com.jonnymatts.jzonbie.model;

public class TemplatedAppResponse extends AppResponse {

    public static TemplatedAppResponse templated(AppResponse appResponse) {
        return Cloner.createTemplatedResponse(appResponse);
    }

    @Override
    public String toString() {
        return "TemplatedAppResponse{" +
                "statusCode=" + getStatusCode() +
                ", headers=" + getHeaders() +
                ", delay=" + getDelay().orElse(null) +
                ", body=" + getBody() +
                '}';
    }
}