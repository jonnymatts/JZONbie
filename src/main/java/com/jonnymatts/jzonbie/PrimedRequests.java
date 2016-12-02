package com.jonnymatts.jzonbie;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class PrimedRequests {

    public PrimedRequests(PrimedRequest primedRequest, List<PrimedResponse> primedResponses) {
        this.primedRequest = primedRequest;
        this.primedResponses = primedResponses;
    }

    @JsonProperty("request")
    private PrimedRequest primedRequest;

    @JsonProperty("responses")
    private List<PrimedResponse> primedResponses;

    public PrimedRequest getPrimedRequest() {
        return primedRequest;
    }

    public void setPrimedRequest(PrimedRequest primedRequest) {
        this.primedRequest = primedRequest;
    }

    public List<PrimedResponse> getPrimedResponses() {
        return primedResponses;
    }

    public void setPrimedResponses(List<PrimedResponse> primedResponses) {
        this.primedResponses = primedResponses;
    }
}
