package com.jonnymatts.jzonbie.responses.defaults;

import com.jonnymatts.jzonbie.responses.AppResponse;

import static com.jonnymatts.jzonbie.responses.defaults.DefaultAppResponseType.STATIC;

public class StaticDefaultAppResponse extends DefaultAppResponse {
    private AppResponse response;

    public StaticDefaultAppResponse(AppResponse response) {
        this.response = response;
    }

    @Override
    public AppResponse getResponse() {
        return response;
    }

    @Override
    public DefaultAppResponseType getType() {
        return STATIC;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StaticDefaultAppResponse that = (StaticDefaultAppResponse) o;

        return response != null ? response.equals(that.response) : that.response == null;
    }

    @Override
    public int hashCode() {
        return response != null ? response.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "StaticDefaultAppResponse{" +
                "response=" + response +
                '}';
    }
}