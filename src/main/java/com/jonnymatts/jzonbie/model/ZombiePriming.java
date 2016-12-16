package com.jonnymatts.jzonbie.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ZombiePriming {

    @JsonProperty("request")
    private ZombieRequest zombieRequest;

    @JsonProperty("response")
    private ZombieResponse zombieResponse;

    public ZombiePriming() {}

    public ZombiePriming(ZombieRequest zombieRequest, ZombieResponse zombieResponse) {
        this.zombieRequest = zombieRequest;
        this.zombieResponse = zombieResponse;
    }

    public ZombieRequest getZombieRequest() {
        return zombieRequest;
    }

    public void setZombieRequest(ZombieRequest zombieRequest) {
        this.zombieRequest = zombieRequest;
    }

    public ZombieResponse getZombieResponse() {
        return zombieResponse;
    }

    public void setZombieResponse(ZombieResponse zombieResponse) {
        this.zombieResponse = zombieResponse;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ZombiePriming that = (ZombiePriming) o;

        if (zombieRequest != null ? !zombieRequest.equals(that.zombieRequest) : that.zombieRequest != null)
            return false;
        return zombieResponse != null ? zombieResponse.equals(that.zombieResponse) : that.zombieResponse == null;
    }

    @Override
    public int hashCode() {
        int result = zombieRequest != null ? zombieRequest.hashCode() : 0;
        result = 31 * result + (zombieResponse != null ? zombieResponse.hashCode() : 0);
        return result;
    }
}
