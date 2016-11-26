package com.jonnymatts.jzonbie;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PrimingRequest {

    @JsonProperty("request")
    private PrimedRequest primedRequest;

    @JsonProperty("response")
    private PrimedResponse primedResponse;

    public PrimedRequest getPrimedRequest() {
        return primedRequest;
    }

    public void setPrimedRequest(PrimedRequest primedRequest) {
        this.primedRequest = primedRequest;
    }

    public PrimedResponse getPrimedResponse() {
        return primedResponse;
    }

    public void setPrimedResponse(PrimedResponse primedResponse) {
        this.primedResponse = primedResponse;
    }
}
