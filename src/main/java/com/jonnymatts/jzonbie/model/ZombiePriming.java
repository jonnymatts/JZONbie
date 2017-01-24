package com.jonnymatts.jzonbie.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ZombiePriming {

    @JsonProperty("request")
    private AppRequest appRequest;

    @JsonProperty("response")
    private AppResponse appResponse;

    public ZombiePriming() {}

    public ZombiePriming(AppRequest appRequest, AppResponse appResponse) {
        this.appRequest = appRequest;
        this.appResponse = appResponse;
    }

    public AppRequest getAppRequest() {
        return appRequest;
    }

    public void setAppRequest(AppRequest appRequest) {
        this.appRequest = appRequest;
    }

    public AppResponse getAppResponse() {
        return appResponse;
    }

    public void setAppResponse(AppResponse appResponse) {
        this.appResponse = appResponse;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ZombiePriming that = (ZombiePriming) o;

        if (appRequest != null ? !appRequest.equals(that.appRequest) : that.appRequest != null)
            return false;
        return appResponse != null ? appResponse.equals(that.appResponse) : that.appResponse == null;
    }

    @Override
    public int hashCode() {
        int result = appRequest != null ? appRequest.hashCode() : 0;
        result = 31 * result + (appResponse != null ? appResponse.hashCode() : 0);
        return result;
    }
}
