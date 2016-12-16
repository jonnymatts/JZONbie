package com.jonnymatts.jzonbie.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class PrimedMapping {

    public PrimedMapping() {}

    public PrimedMapping(ZombieRequest zombieRequest, List<ZombieResponse> zombieResponses) {
        this.zombieRequest = zombieRequest;
        this.zombieResponses = zombieResponses;
    }

    @JsonProperty("request")
    private ZombieRequest zombieRequest;

    @JsonProperty("responses")
    private List<ZombieResponse> zombieResponses;

    public ZombieRequest getZombieRequest() {
        return zombieRequest;
    }

    public void setZombieRequest(ZombieRequest zombieRequest) {
        this.zombieRequest = zombieRequest;
    }

    public List<ZombieResponse> getZombieResponses() {
        return zombieResponses;
    }

    public void setZombieResponses(List<ZombieResponse> zombieResponses) {
        this.zombieResponses = zombieResponses;
    }
}
