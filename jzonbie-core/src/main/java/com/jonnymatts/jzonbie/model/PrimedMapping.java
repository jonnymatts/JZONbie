package com.jonnymatts.jzonbie.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jonnymatts.jzonbie.response.DefaultingQueue;

public class PrimedMapping {

    public PrimedMapping() {}

    public PrimedMapping(AppRequest appRequest, DefaultingQueue appResponses) {
        this.appRequest = appRequest;
        this.appResponses = appResponses;
    }

    @JsonProperty("request")
    private AppRequest appRequest;

    @JsonProperty("responses")
    private DefaultingQueue appResponses;

    public AppRequest getAppRequest() {
        return appRequest;
    }

    public void setAppRequest(AppRequest appRequest) {
        this.appRequest = appRequest;
    }

    public DefaultingQueue getAppResponses() {
        return appResponses;
    }

    public void setAppResponses(DefaultingQueue appResponses) {
        this.appResponses = appResponses;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        PrimedMapping that = (PrimedMapping) o;

        if(appRequest != null ? !appRequest.equals(that.appRequest) : that.appRequest != null) return false;
        return appResponses != null ? appResponses.equals(that.appResponses) : that.appResponses == null;

    }

    @Override
    public int hashCode() {
        int result = appRequest != null ? appRequest.hashCode() : 0;
        result = 31 * result + (appResponses != null ? appResponses.hashCode() : 0);
        return result;
    }
}
