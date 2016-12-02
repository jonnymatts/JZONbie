package com.jonnymatts.jzonbie;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PrimingRequest {

    @JsonProperty("request")
    private PrimedRequest primedRequest;

    @JsonProperty("response")
    private PrimedResponse primedResponse;

    public PrimingRequest() {}

    public PrimingRequest(PrimedRequest primedRequest, PrimedResponse primedResponse) {
        this.primedRequest = primedRequest;
        this.primedResponse = primedResponse;
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PrimingRequest that = (PrimingRequest) o;

        if (primedRequest != null ? !primedRequest.equals(that.primedRequest) : that.primedRequest != null)
            return false;
        return primedResponse != null ? primedResponse.equals(that.primedResponse) : that.primedResponse == null;
    }

    @Override
    public int hashCode() {
        int result = primedRequest != null ? primedRequest.hashCode() : 0;
        result = 31 * result + (primedResponse != null ? primedResponse.hashCode() : 0);
        return result;
    }
}
