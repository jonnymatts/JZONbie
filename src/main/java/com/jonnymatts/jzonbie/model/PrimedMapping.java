package com.jonnymatts.jzonbie.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class PrimedMapping {

    public PrimedMapping() {}

    public PrimedMapping(AppRequest appRequest, List<AppResponse> appResponses) {
        this.appRequest = appRequest;
        this.appResponses = appResponses;
    }

    @JsonProperty("request")
    private AppRequest appRequest;

    @JsonProperty("responses")
    private List<AppResponse> appResponses;

    public AppRequest getAppRequest() {
        return appRequest;
    }

    public void setAppRequest(AppRequest appRequest) {
        this.appRequest = appRequest;
    }

    public List<AppResponse> getAppResponses() {
        return appResponses;
    }

    public void setAppResponses(List<AppResponse> appResponses) {
        this.appResponses = appResponses;
    }
}
